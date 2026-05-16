package com.gbak.stoker.telnet

import com.gbak.stoker.config.StokerDeviceConfig
import com.gbak.stoker.config.StokerProperties
import com.gbak.stoker.model.events.ConnectionEventType
import com.gbak.stoker.model.events.StateChangeEvent
import com.gbak.stoker.parser.InvalidDataPointException
import com.gbak.stoker.parser.SDataPointParser
import com.gbak.stoker.stoker.StokerDeviceDiscovery
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.currentCoroutineContext
import org.apache.commons.net.telnet.TelnetClient
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages the telnet connection to the Stoker device.
 *
 * Flow:
 * 1. Opens telnet connection to the Stoker's IP/port
 * 2. Logs in with credentials (root/tini)
 * 3. Sends "bbq -t" to start temperature streaming
 * 4. Reads the stream character-by-character, detecting prompts, commands, and data lines
 * 5. Parses each data line via SDataPointParser and publishes events
 * 6. Monitors for inactivity and reconnects if no data for >1 minute
 */
@Service
class StokerTelnetController(
    private val properties: StokerProperties,
    private val deviceConfig: StokerDeviceConfig,
    private val parser: SDataPointParser,
    private val discovery: StokerDeviceDiscovery,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(StokerTelnetController::class.java)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // State enums matching the original state machine
    enum class ResponseState { NONE, TEMPS }
    enum class CmdState { ENTRY, CMD_IN_PROGRESS, STARTED, STOPPED, UNKNOWN }
    enum class TelnetState { ENTRY, DISCONNECTED, CONNECTED, ERROR }
    enum class LoginState { YES, NO }

    // Volatile state fields
    @Volatile private var responseState = ResponseState.NONE
    @Volatile private var cmdState = CmdState.ENTRY
    @Volatile private var telnetState = TelnetState.DISCONNECTED
    @Volatile private var loginState = LoginState.NO

    private var telnetClient: TelnetClient? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var lastMessageTime: Instant? = null

    private var readerJob: Job? = null
    private var connectionJob: Job? = null
    private var monitorJob: Job? = null

    private val startLock = AtomicBoolean(false)

    // Stoker protocol constants
    companion object {
        const val CMD_START = "bbq -t\n"
        const val CMD_STOP = "\nbbq -k\n"
        const val CMD_TEMPS = "\nbbq -temps\n"
        const val CMD_LOGIN_ID = "root\r\n"
        const val CMD_LOGIN_PASSWD = "tini\r\n"
        const val CMD_LOGOUT = "logout\n"

        const val PROMPT_LOGIN = "login:"
        const val PROMPT_PASSWORD = "password:"
        const val CONDITION_START = "stoker: start"
        const val CONDITION_STOP = "stkcmd: stop"
    }

    val isConnected: Boolean get() = telnetState == TelnetState.CONNECTED
    val isProcessing: Boolean get() = responseState == ResponseState.TEMPS && isConnected
    val isReady: Boolean get() = isProcessing

    /**
     * Starts the connection loop. Attempts to connect every 3 minutes if disconnected.
     */
    fun start() {
        connectionJob?.cancel()
        connectionJob = scope.launch {
            while (isActive) {
                if (telnetState != TelnetState.CONNECTED) {
                    log.debug("Connection loop: attempting connection")
                    startHelper()
                }
                delay(180_000) // retry every 3 minutes
            }
        }
    }

    fun stop() {
        connectionJob?.cancel()
        monitorJob?.cancel()
        stopInternal()
    }

    @PreDestroy
    fun shutdown() {
        stop()
        scope.cancel()
    }

    fun reconnect() {
        stopInternal()
        // The connection loop in start() will pick up the disconnected state
    }

    private fun startHelper() {
        if (!startLock.compareAndSet(false, true)) return

        try {
            cmdState = CmdState.ENTRY
            responseState = ResponseState.NONE

            try {
                createConnection()

                if (telnetState != TelnetState.CONNECTED) return

                // Wait for temps to start flowing
                waitForState({ responseState == ResponseState.TEMPS }, timeoutMs = 20_000)

                if (responseState != ResponseState.TEMPS) {
                    sendCommand(CMD_TEMPS)
                }

                waitForState({ responseState == ResponseState.TEMPS }, timeoutMs = 20_000)

                if (responseState != ResponseState.TEMPS) {
                    sendCommand(CMD_STOP)
                    waitForState({ cmdState == CmdState.STOPPED }, timeoutMs = 30_000)
                    sendCommand(CMD_START)
                    waitForState({ cmdState == CmdState.STARTED }, timeoutMs = 30_000)
                }

                // Fetch device list from the Stoker's HTTP endpoint to discover
                // the real fan device ID (not available from the telnet stream alone)
                discovery.refresh()

                eventPublisher.publishEvent(
                    StateChangeEvent(ConnectionEventType.CONNECTION_ESTABLISHED)
                )

                startMonitor()

            } catch (e: ConnectException) {
                log.warn("Could not connect to Stoker at {}:{}", properties.device.ip, properties.device.port)
                telnetState = TelnetState.DISCONNECTED
            } catch (e: Exception) {
                log.error("Error during connection: {}", e.message)
                stopInternal()
            }

            // Verify we're in a good state
            if (telnetState != TelnetState.CONNECTED || responseState != ResponseState.TEMPS) {
                log.error("Status not correct after starting, calling stopInternal()")
                stopInternal()
            }
        } finally {
            startLock.set(false)
        }
    }

    private fun createConnection() {
        log.info("Creating telnet connection to {}:{}", properties.device.ip, properties.device.port)

        val client = TelnetClient()
        client.connect(properties.device.ip, properties.device.port)
        telnetClient = client
        telnetState = TelnetState.CONNECTED

        inputStream = client.inputStream
        outputStream = client.outputStream

        // Start the stream reader coroutine
        readerJob?.cancel()
        readerJob = scope.launch {
            streamReader()
        }

        // Wait for login to complete (up to 30 seconds)
        waitForState({ loginState == LoginState.YES }, timeoutMs = 30_000)

        if (loginState == LoginState.NO) {
            log.warn("Unable to login, giving up")
            stopInternal()
        }
    }

    private fun sendCommand(command: String) {
        cmdState = CmdState.CMD_IN_PROGRESS
        try {
            outputStream?.let { out ->
                out.write(command.toByteArray())
                out.flush()
                log.debug("Sent: [{}]", command.trim())
            }
        } catch (e: Exception) {
            log.error("Error sending command: {}", e.message)
            reconnect()
        }
    }

    /**
     * Core stream reader -- reads the telnet stream character by character,
     * detects login prompts, command responses, and temperature data lines.
     *
     * This is a faithful port of the original Java state machine.
     */
    private suspend fun streamReader() {
        val stream = inputStream ?: return
        val buffer = ByteArray(1024)

        try {
            val sb = StringBuilder()
            var lastRead = 0
            var doubleLastRead = 0

            while (telnetState == TelnetState.CONNECTED && currentCoroutineContext().isActive) {
                if (stream.available() <= 0) {
                    delay(100)
                    continue
                }

                val bytesRead = stream.read(buffer)
                if (bytesRead < 0) break

                for (i in 0 until bytesRead) {
                    val intRead = buffer[i].toInt() and 0xFF

                    // Accumulate printable ASCII characters
                    if (intRead in 32..126) {
                        sb.append(intRead.toChar())
                    }

                    // Detect "login: " prompt
                    if (intRead == ' '.code && lastRead == ':'.code
                        && sb.contains(PROMPT_LOGIN)
                    ) {
                        log.info("Found login string")
                        sendCommand(CMD_LOGIN_ID)
                        sb.clear()
                    }

                    // Detect "password: " prompt
                    if (intRead == ' '.code && lastRead == ':'.code
                        && sb.contains(PROMPT_PASSWORD)
                    ) {
                        log.info("Found password string")
                        sendCommand(CMD_LOGIN_PASSWD)
                        sb.clear()
                    }

                    // Detect tini shell prompt " />"
                    if (intRead == ' '.code && lastRead == '>'.code && doubleLastRead == '/'.code
                        && sb.contains("tini") && sb.contains(" />")
                    ) {
                        log.info("Found tini prompt")
                        loginState = LoginState.YES
                    }

                    // Detect "stoker: start" response
                    if (intRead == 't'.code && lastRead == 'r'.code
                        && sb.contains(CONDITION_START)
                    ) {
                        log.info("Stoker start response detected")
                        cmdState = CmdState.STARTED
                    }

                    // Detect "stkcmd: stop" response
                    if (intRead == 'p'.code && lastRead == 'o'.code
                        && sb.contains(CONDITION_STOP)
                    ) {
                        log.info("Stoker stop response detected")
                        cmdState = CmdState.STOPPED
                    }

                    // Detect "not started" response
                    if (intRead == 'd'.code && lastRead == 'e'.code) {
                        log.debug("String: {}", sb)
                        log.info("Stoker not started response detected")
                        cmdState = CmdState.STOPPED
                    }

                    // Newline = end of a data line
                    if (intRead == 10 && sb.isNotEmpty()) {
                        val line = sb.toString()
                        sb.clear()

                        try {
                            lastMessageTime = Instant.now()
                            val dataPoints = parser.parse(line, deviceConfig)
                            dataPoints.forEach { dp ->
                                eventPublisher.publishEvent(dp)
                            }
                            responseState = ResponseState.TEMPS
                        } catch (e: InvalidDataPointException) {
                            log.warn("Invalid data point: [{}]", line)
                        }
                    }

                    doubleLastRead = lastRead
                    lastRead = intRead
                }
            }
        } catch (e: Exception) {
            if (telnetState == TelnetState.CONNECTED) {
                log.error("Exception while reading socket: {}", e.message, e)
                reconnect()
            }
        }

        log.info("Stream reader exiting")
    }

    private fun closeConnection() {
        log.debug("Closing connection")
        eventPublisher.publishEvent(
            StateChangeEvent(ConnectionEventType.LOST_CONNECTION)
        )

        telnetState = TelnetState.DISCONNECTED
        loginState = LoginState.NO
        cmdState = CmdState.UNKNOWN

        readerJob?.cancel()
        try {
            telnetClient?.takeIf { it.isConnected }?.disconnect()
        } catch (e: Exception) {
            log.debug("Error during disconnect: {}", e.message)
        }
        telnetClient = null
        inputStream = null
        outputStream = null
    }

    private fun stopInternal() {
        log.debug("stopInternal()")
        monitorJob?.cancel()
        closeConnection()
    }

    /**
     * Monitors for inactivity. If no data received for >1 minute, reconnects.
     */
    private fun startMonitor() {
        monitorJob?.cancel()
        monitorJob = scope.launch {
            while (isActive) {
                delay(10_000) // check every 10 seconds
                val lastMsg = lastMessageTime ?: continue
                if (Instant.now().isAfter(lastMsg.plusSeconds(60))) {
                    log.warn("No activity from Stoker in the last minute, attempting reconnect")
                    reconnect()
                    return@launch
                }
            }
        }
    }

    private fun waitForState(condition: () -> Boolean, timeoutMs: Long) {
        val start = System.currentTimeMillis()
        while (!condition() && System.currentTimeMillis() - start < timeoutMs) {
            Thread.sleep(500)
        }
    }
}
