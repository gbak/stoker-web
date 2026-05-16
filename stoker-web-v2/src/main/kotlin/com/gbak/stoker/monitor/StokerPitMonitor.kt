package com.gbak.stoker.monitor

import com.gbak.stoker.model.SBlowerDataPoint
import com.gbak.stoker.model.SDataPoint
import com.gbak.stoker.model.SProbeDataPoint
import com.gbak.stoker.model.events.*
import com.gbak.stoker.telnet.StokerTelnetController
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Maintains a cache of the latest data point for each device.
 * Listens for raw SDataPoint events from the telnet parser, detects changes,
 * tracks blower runtime, and publishes DataPointEvent when values change.
 */
@Service
class StokerPitMonitor(
    private val telnetController: StokerTelnetController,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(StokerPitMonitor::class.java)

    private val latestData = ConcurrentHashMap<String, SDataPoint>()

    @Volatile
    var hardwareState = HardwareDeviceState()
        internal set

    val isActive: Boolean get() = telnetController.isReady
    val isConnected: Boolean get() = telnetController.isConnected

    fun getCurrentTemp(deviceId: String): SDataPoint? = latestData[deviceId]

    fun getCurrentTemps(): List<SDataPoint> =
        if (isActive) latestData.values.toList() else emptyList()

    @EventListener
    fun onDataPoint(dp: SProbeDataPoint) = addDataPoint(dp)

    @EventListener
    fun onBlowerDataPoint(dp: SBlowerDataPoint) = addDataPoint(dp)

    private fun addDataPoint(dp: SDataPoint) {
        log.trace("addDataPoint: {}", dp.deviceId)

        val existing = latestData[dp.deviceId]

        if (existing != null) {
            // Force update if cached data is older than 1 minute
            val forceUpdate = Duration.between(existing.collectedTime, Instant.now()).toMinutes() >= 1
            val hasChanged = !existing.hasChanged(dp).not() || forceUpdate
            // Note: hasChanged returns true when values differ

            if (existing.hasChanged(dp) || forceUpdate) {
                // Track blower runtime
                val updatedDp = if (dp is SBlowerDataPoint && existing is SBlowerDataPoint) {
                    trackBlowerRuntime(dp, existing)
                } else {
                    dp
                }

                latestData[dp.deviceId] = updatedDp
                eventPublisher.publishEvent(DataPointEvent(listOf(updatedDp)))
            } else {
                // Update timestamp even if value hasn't changed
                latestData[dp.deviceId] = dp
            }

            log.trace("Updated: {}", dp.displayValue())
        } else {
            // First reading for this device
            val initialDp = if (dp is SBlowerDataPoint && dp.fanOn) {
                // If blower is already on when we start, record the start time
                dp.copy(blowerOnTime = dp.collectedTime)
            } else {
                dp
            }
            latestData[dp.deviceId] = initialDp
        }
    }

    /**
     * When the fan turns off, calculate how long it was on and add to total runtime.
     * When the fan turns on, record the start time.
     */
    private fun trackBlowerRuntime(current: SBlowerDataPoint, previous: SBlowerDataPoint): SBlowerDataPoint {
        return if (!current.fanOn && previous.fanOn) {
            // Fan just turned off -- calculate elapsed runtime
            val onTime = previous.blowerOnTime
            val elapsed = if (onTime != null) {
                Duration.between(onTime, current.collectedTime).toMillis()
            } else 0L
            val newTotal = previous.totalRuntime + elapsed
            log.debug("Fan off event, total runtime: {}ms", newTotal)
            current.copy(totalRuntime = newTotal)
        } else if (current.fanOn && !previous.fanOn) {
            // Fan just turned on -- record start time
            current.copy(blowerOnTime = current.collectedTime, totalRuntime = previous.totalRuntime)
        } else {
            // No state change, carry forward runtime tracking
            current.copy(blowerOnTime = previous.blowerOnTime, totalRuntime = previous.totalRuntime)
        }
    }

    @EventListener
    fun onStateChange(event: StateChangeEvent) {
        hardwareState = when (event.eventType) {
            ConnectionEventType.CONNECTION_ESTABLISHED ->
                HardwareDeviceState(HardwareStatus.CONNECTED)
            ConnectionEventType.LOST_CONNECTION,
            ConnectionEventType.EXTENDED_CONNECTION_LOSS ->
                HardwareDeviceState(HardwareStatus.DISCONNECTED)
            else ->
                HardwareDeviceState(HardwareStatus.UNKNOWN)
        }
    }

    fun start() {
        telnetController.start()
    }

    fun stop() {
        if (hardwareState.isConnected) {
            telnetController.stop()
        }
    }

    fun shutdown() {
        stop()
    }
}
