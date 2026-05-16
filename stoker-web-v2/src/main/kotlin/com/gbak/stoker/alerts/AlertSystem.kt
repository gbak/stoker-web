package com.gbak.stoker.alerts

import com.gbak.stoker.config.StokerDeviceConfig
import com.gbak.stoker.config.StokerProperties
import com.gbak.stoker.model.SProbeDataPoint
import com.gbak.stoker.model.alerts.*
import com.gbak.stoker.model.devices.AlarmType
import com.gbak.stoker.model.devices.StokerPitProbe
import com.gbak.stoker.model.devices.StokerProbe
import com.gbak.stoker.model.events.ConnectionEventType
import com.gbak.stoker.model.events.DataPointEvent
import com.gbak.stoker.model.events.StateChangeEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

/**
 * Evaluates alert conditions and routes notifications to delivery channels.
 *
 * Alert types:
 * - StokerAlarm: fires when a probe exceeds its configured alarm thresholds
 * - ConnectionChange: fires on connection state changes
 * - TempAlert / TimedAlert: placeholders for future expansion
 */
@Service
class AlertManager(
    private val deviceConfig: StokerDeviceConfig,
    private val properties: StokerProperties,
    private val deliveryRouter: DeliveryRouter,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(AlertManager::class.java)

    private val lastAlertTime = mutableMapOf<String, Instant>()

    private var stokerAlarmConfig = StokerAlarmAlert()
    private var connectionAlertConfig = ConnectionChangeAlert()

    val availableDeliveryMethods: Set<String>
        get() = deliveryRouter.availableChannels

    fun getConfiguration(): List<AlertModel> =
        listOf(stokerAlarmConfig, connectionAlertConfig)

    fun setConfiguration(alerts: List<AlertModel>) {
        alerts.forEach { alert ->
            when (alert) {
                is StokerAlarmAlert -> stokerAlarmConfig = alert
                is ConnectionChangeAlert -> connectionAlertConfig = alert
                is TempAlarmAlert -> {} // future
                is TimeAlert -> {} // future
            }
        }
    }

    @EventListener
    fun onDataPoint(event: DataPointEvent) {
        if (!stokerAlarmConfig.enabled) return

        event.dataPoints.filterIsInstance<SProbeDataPoint>().forEach { dp ->
            checkTemperatureAlarm(dp)
        }
    }

    @EventListener
    fun onStateChange(event: StateChangeEvent) {
        if (!connectionAlertConfig.enabled) return

        val message = when (event.eventType) {
            ConnectionEventType.LOST_CONNECTION -> "Stoker connection lost"
            ConnectionEventType.EXTENDED_CONNECTION_LOSS -> "Extended Stoker connection loss"
            ConnectionEventType.CONNECTION_ESTABLISHED -> "Stoker connection established"
            else -> return
        }

        deliveryRouter.deliver(
            connectionAlertConfig.configuredDeliveryMethods,
            subject = "Stoker Connection Alert",
            message = message
        )
    }

    private fun checkTemperatureAlarm(dp: SProbeDataPoint) {
        val device = deviceConfig.findDeviceById(dp.deviceId) ?: return

        val (alarmType, targetTemp, lowerAlarm, upperAlarm) = when (device) {
            is StokerProbe -> AlarmInfo(device.alarmType, device.targetTemp, device.lowerTempAlarm, device.upperTempAlarm)
            is StokerPitProbe -> AlarmInfo(device.alarmType, device.targetTemp, device.lowerTempAlarm, device.upperTempAlarm)
            else -> return
        }

        if (alarmType == AlarmType.NONE) return

        val tempF = dp.tempF
        var alarmMessage: String? = null

        when (alarmType) {
            AlarmType.ALARM_FOOD -> {
                if (tempF >= targetTemp) {
                    alarmMessage = "Food probe ${device.name} reached target: ${tempF}°F (target: ${targetTemp}°F)"
                }
            }
            AlarmType.ALARM_FIRE -> {
                if (upperAlarm > 0 && tempF >= upperAlarm) {
                    alarmMessage = "High temp alarm on ${device.name}: ${tempF}°F (max: ${upperAlarm}°F)"
                } else if (lowerAlarm > 0 && tempF <= lowerAlarm) {
                    alarmMessage = "Low temp alarm on ${device.name}: ${tempF}°F (min: ${lowerAlarm}°F)"
                }
            }
            AlarmType.NONE -> {}
        }

        if (alarmMessage != null) {
            fireAlarmIfNotThrottled(dp.deviceId, alarmMessage)
        }
    }

    private fun fireAlarmIfNotThrottled(deviceId: String, message: String) {
        val repeatMinutes = properties.alarm.repeatIntervalMinutes.toLong()
        val now = Instant.now()
        val lastTime = lastAlertTime[deviceId]

        if (lastTime != null && Duration.between(lastTime, now).toMinutes() < repeatMinutes) {
            return // throttled
        }

        lastAlertTime[deviceId] = now
        log.info("Firing alarm: {}", message)

        // Push to browser
        eventPublisher.publishEvent(BrowserAlarm(
            alarmName = "Temperature Alarm",
            message = message
        ))

        // Deliver via configured channels
        deliveryRouter.deliver(
            stokerAlarmConfig.configuredDeliveryMethods,
            subject = "Stoker Temperature Alarm",
            message = message
        )
    }

    private data class AlarmInfo(
        val alarmType: AlarmType,
        val targetTemp: Int,
        val lowerAlarm: Int,
        val upperAlarm: Int
    )
}
