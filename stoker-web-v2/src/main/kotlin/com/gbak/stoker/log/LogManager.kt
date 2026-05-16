package com.gbak.stoker.log

import com.fasterxml.jackson.databind.ObjectMapper
import com.gbak.stoker.config.StokerDeviceConfig
import com.gbak.stoker.config.StokerProperties
import com.gbak.stoker.model.events.ConfigChangeEvent
import com.gbak.stoker.model.events.ConfigEventType
import com.gbak.stoker.model.events.DataPointEvent
import com.gbak.stoker.model.log.LogFileEntry
import com.gbak.stoker.model.log.LogItem
import com.gbak.stoker.model.log.LogNote
import com.gbak.stoker.monitor.StokerPitMonitor
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class LogExistsException(logName: String) : Exception("Log already exists: $logName")
class LogNotFoundException(logName: String) : Exception("Log not found: $logName")

@Service
class LogManager(
    private val pitMonitor: StokerPitMonitor,
    private val deviceConfig: StokerDeviceConfig,
    private val properties: StokerProperties,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(LogManager::class.java)
    private val activeLogs = ConcurrentHashMap<String, CookLog>()
    private val viewLogs = ConcurrentHashMap<String, CookLog>()

    fun getLogList(): List<LogItem> =
        (activeLogs.values + viewLogs.values)
            .map { it.logItem }
            .sortedWith(compareBy<LogItem> { !it.logName.startsWith("Default") }.thenBy { it.logName })

    fun isLogRunning(logName: String): Boolean = activeLogs.containsKey(logName)

    fun getLogFilePath(logName: String): String? = activeLogs[logName]?.filePath
    fun getLogFileName(logName: String): String? = activeLogs[logName]?.fileName

    fun startLog(cookerName: String, logName: String) {
        val item = LogItem(logName, cookerName, Instant.now())
        startLog(item)
    }

    fun startLog(logItem: LogItem) {
        if (activeLogs.containsKey(logItem.logName)) {
            throw LogExistsException(logItem.logName)
        }

        val cookLog = CookLog(
            logItem = logItem.let {
                if (it.devices.isEmpty()) {
                    // If no devices specified, log all known devices
                    val cooker = deviceConfig.cookerList.cookers
                        .firstOrNull { c -> c.cookerName == it.cookerName }
                    it.copy(devices = cooker?.allDevices() ?: emptyList())
                } else it
            },
            objectMapper = objectMapper,
            logDir = properties.dataDir
        )

        activeLogs[logItem.logName] = cookLog
        cookLog.start()
        log.info("Started log: {}", logItem.logName)
    }

    fun stopLog(logName: String): String {
        val cookLog = activeLogs.remove(logName)
            ?: throw LogNotFoundException(logName)
        cookLog.stop()
        log.info("Stopped log: {}", logName)
        return cookLog.fileName
    }

    fun stopAllLogs() {
        activeLogs.keys.toList().forEach { name ->
            try { stopLog(name) } catch (_: Exception) {}
        }
    }

    fun getAllDataPoints(logName: String): Map<String, List<com.gbak.stoker.model.SDataPoint>> {
        val cookLog = activeLogs[logName] ?: viewLogs[logName] ?: return emptyMap()
        return cookLog.readAllDataPoints()
    }

    fun getNotes(logName: String): List<LogNote> {
        return (activeLogs[logName] ?: viewLogs[logName])?.readAllNotes() ?: emptyList()
    }

    fun listLogFiles(): List<LogFileEntry> {
        val cookLogsDir = File("${properties.dataDir}/cookLogs")
        if (!cookLogsDir.exists()) return emptyList()

        return cookLogsDir.walkTopDown()
            .filter { it.isFile && it.extension == "jsonl" }
            .mapNotNull { file ->
                try {
                    val firstLine = file.useLines { it.firstOrNull() } ?: return@mapNotNull null
                    val header = objectMapper.readTree(firstLine)
                    if (header.get("type")?.asText() != "header") return@mapNotNull null

                    val cookerName = header.get("cookerName")?.asText() ?: return@mapNotNull null
                    val startTime = Instant.parse(header.get("startTime").asText())
                    val logName = file.nameWithoutExtension
                        .replaceFirst(Regex("^\\d{8}_\\d{2}_\\d{2}_"), "")
                        .replace("_", " ")
                    val relativePath = file.toRelativeString(cookLogsDir)

                    LogFileEntry(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        relativePath = relativePath,
                        cookerName = cookerName,
                        logName = logName,
                        startTime = startTime,
                        sizeBytes = file.length()
                    )
                } catch (e: Exception) {
                    log.warn("Failed to read log file header {}: {}", file.name, e.message)
                    null
                }
            }
            .sortedByDescending { it.startTime }
            .toList()
    }

    fun loadLogFromFile(filePath: String, resume: Boolean): LogItem {
        val file = File(filePath)
        require(file.exists()) { "Log file not found: $filePath" }

        val cookLog = CookLog.fromFile(file, objectMapper, properties.dataDir)
            ?: throw IllegalArgumentException("Cannot parse log file: ${file.name}")

        if (resume) {
            if (activeLogs.containsKey(cookLog.name)) throw LogExistsException(cookLog.name)
            activeLogs[cookLog.name] = cookLog
            cookLog.resume()
        } else {
            if (viewLogs.containsKey(cookLog.name)) throw LogExistsException(cookLog.name)
            viewLogs[cookLog.name] = cookLog
        }

        return cookLog.logItem.copy(viewOnly = !resume)
    }

    fun closeViewLog(logName: String) {
        viewLogs.remove(logName)
    }

    fun addNoteToLog(note: String, logNames: List<String>) {
        log.info("Adding note to logs: {}", logNames)
        logNames.forEach { logName ->
            activeLogs[logName]?.addNote(note)
        }
    }

    /**
     * Fires every 60 seconds to collect current temps and write them to all active logs.
     * This is the "timed event" that produces the minute-by-minute temperature record.
     */
    @Scheduled(fixedRate = 60_000, initialDelay = 10_000)
    fun periodicWrite() {
        val currentTemps = pitMonitor.getCurrentTemps()
        if (currentTemps.isEmpty()) return

        val event = DataPointEvent(currentTemps)
        activeLogs.values.forEach { cookLog ->
            cookLog.writeDataPoints(event.dataPoints, timedEvent = true)
        }
    }

    @EventListener
    fun onDataPoint(event: DataPointEvent) {
        // Real-time blower events are written immediately (not timed)
        activeLogs.values.forEach { cookLog ->
            cookLog.writeDataPoints(event.dataPoints, timedEvent = false)
        }
    }

    @EventListener
    fun onConfigChange(event: ConfigChangeEvent) {
        when (event.eventType) {
            ConfigEventType.CONFIG_LOADED -> startDefaultLogs()
            ConfigEventType.CONFIG_SAVED -> rectifyLogs()
            else -> {}
        }
    }

    private fun startDefaultLogs() {
        deviceConfig.cookerList.cookers.forEach { cooker ->
            startDefaultLog(cooker.cookerName, cooker.allDevices())
        }
    }

    private fun startDefaultLog(cookerName: String, devices: List<com.gbak.stoker.model.devices.SDevice>) {
        val defaultName = "Default_$cookerName"
        if (!isLogRunning(defaultName)) {
            try {
                startLog(LogItem(defaultName, cookerName, Instant.now(), devices))
            } catch (e: LogExistsException) {
                log.error("LogExistsException: {}", defaultName)
            }
        }
    }

    private fun rectifyLogs() {
        val cookerNames = deviceConfig.cookerList.cookers.map { it.cookerName }.toSet()

        // Stop logs for cookers that no longer exist
        activeLogs.values.toList().forEach { cookLog ->
            if (cookLog.cookerName !in cookerNames) {
                try { stopLog(cookLog.name) } catch (_: Exception) {}
            }
        }

        // Start default logs for any cooker that doesn't have one
        deviceConfig.cookerList.cookers.forEach { cooker ->
            startDefaultLog(cooker.cookerName, cooker.allDevices())
        }
    }
}
