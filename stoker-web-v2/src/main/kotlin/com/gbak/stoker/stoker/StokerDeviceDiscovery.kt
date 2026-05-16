package com.gbak.stoker.stoker

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.gbak.stoker.config.StokerProperties
import com.gbak.stoker.model.devices.AlarmType
import com.gbak.stoker.model.devices.DeviceType
import com.gbak.stoker.model.devices.SDevice
import com.gbak.stoker.model.devices.StokerFan
import com.gbak.stoker.model.devices.StokerPitProbe
import com.gbak.stoker.model.devices.StokerProbe
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL

// ─── stoker.json response model ───────────────────────────────────────────────

@JsonIgnoreProperties(ignoreUnknown = true)
data class StokerJsonResponse(val stoker: StokerJson? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StokerJson(
    val sensors: List<StokerSensorJson> = emptyList(),
    val blowers: List<StokerBlowerJson> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StokerSensorJson(
    val id: String = "",
    val name: String = "",
    val al: String? = null,
    val ta: Int = 0,
    val th: Int = 0,
    val tl: Int = 0,
    val tc: Double = 0.0,
    val blower: String? = null  // blower device ID associated with this pit probe
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StokerBlowerJson(
    val id: String = "",
    val name: String = "",
    val on: String? = null
)

// ─── Discovery service ────────────────────────────────────────────────────────

/**
 * Fetches the Stoker device's hardware configuration from its built-in HTTP JSON
 * endpoint (http://<ip>/stoker.json). This is the authoritative source for real
 * device IDs, especially for the blower/fan which doesn't carry its ID in the
 * telnet data stream.
 */
@Service
class StokerDeviceDiscovery(
    private val properties: StokerProperties,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(StokerDeviceDiscovery::class.java)

    /** pit probe ID (uppercase) → fan device ID (uppercase) */
    @Volatile private var pitToFanMap: Map<String, String> = emptyMap()

    /** All hardware devices discovered from stoker.json */
    @Volatile private var discoveredDevices: List<SDevice> = emptyList()

    /** Returns the fan device ID paired with the given pit probe ID, or null if none. */
    fun getFanIdForPit(pitId: String): String? = pitToFanMap[pitId.uppercase()]

    /** All devices discovered from the Stoker hardware (sensors + blowers with real IDs). */
    fun getDiscoveredDevices(): List<SDevice> = discoveredDevices

    fun refresh() {
        val url = "http://${properties.device.ip}/stoker.json"
        try {
            log.info("Fetching hardware config from {}", url)
            val json = URL(url).readText(Charsets.UTF_8)
            val response = objectMapper.readValue(json, StokerJsonResponse::class.java)
            val stoker = response.stoker ?: return

            val blowerMap: Map<String, StokerBlowerJson> = stoker.blowers
                .associateBy { it.id.uppercase() }

            val newPitToFan = mutableMapOf<String, String>()
            val devices = mutableListOf<SDevice>()

            for (sensor in stoker.sensors) {
                val id = sensor.id.uppercase()
                val blowerId = sensor.blower?.uppercase()
                val alarmType = when (sensor.al) {
                    "1"  -> AlarmType.ALARM_FOOD
                    "2"  -> AlarmType.ALARM_FIRE
                    else -> AlarmType.NONE
                }

                if (blowerId != null && blowerMap.containsKey(blowerId)) {
                    val blower = blowerMap[blowerId]!!
                    val fan = StokerFan(
                        id = blowerId,
                        name = blower.name,
                        deviceType = DeviceType.BLOWER
                    )
                    val pitProbe = StokerPitProbe(
                        id = id,
                        name = sensor.name,
                        deviceType = DeviceType.PIT,
                        targetTemp = sensor.ta,
                        lowerTempAlarm = sensor.tl,
                        upperTempAlarm = sensor.th,
                        alarmType = alarmType,
                        currentTemp = sensor.tc.toFloat(),
                        fan = fan
                    )
                    newPitToFan[id] = blowerId
                    devices.add(pitProbe)
                    devices.add(fan)
                } else {
                    devices.add(StokerProbe(
                        id = id,
                        name = sensor.name,
                        deviceType = DeviceType.FOOD,
                        targetTemp = sensor.ta,
                        lowerTempAlarm = sensor.tl,
                        upperTempAlarm = sensor.th,
                        alarmType = alarmType,
                        currentTemp = sensor.tc.toFloat()
                    ))
                }
            }

            pitToFanMap = newPitToFan
            discoveredDevices = devices
            log.info("Discovered {} devices ({} pit-to-fan mappings) from Stoker",
                devices.size, newPitToFan.size)

        } catch (e: Exception) {
            log.warn("Failed to fetch Stoker hardware config from {}: {}", url, e.message)
        }
    }
}
