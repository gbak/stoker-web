package com.gbak.stoker.log

import com.fasterxml.jackson.databind.ObjectMapper
import com.gbak.stoker.model.SBlowerDataPoint
import com.gbak.stoker.model.SDataPoint
import com.gbak.stoker.model.SProbeDataPoint
import com.gbak.stoker.model.devices.SDevice
import com.gbak.stoker.model.devices.StokerFan
import com.gbak.stoker.model.devices.StokerPitProbe
import com.gbak.stoker.model.devices.StokerProbe
import com.gbak.stoker.model.log.LogItem
import com.gbak.stoker.model.log.LogNote
import com.gbak.stoker.model.weather.WeatherData
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * A single cook log file in JSON-lines format.
 * Each line is a self-contained JSON object with a "type" discriminator.
 *
 * Line types:
 *   {"type":"header", "cookerName":"...", "devices":[...], "startTime":"..."}
 *   {"type":"probe",  "deviceId":"...", "tempF":..., "tempC":..., "timestamp":"..."}
 *   {"type":"blower", "deviceId":"...", "fanOn":..., "timestamp":"..."}
 *   {"type":"note",   "note":"...", "timestamp":"..."}
 *   {"type":"weather","data":{...}, "timestamp":"..."}
 */
class CookLog(
    val logItem: LogItem,
    private val objectMapper: ObjectMapper,
    private val logDir: String,
    private val existingFilePath: String? = null
) {
    private val log = LoggerFactory.getLogger(CookLog::class.java)

    private val deviceIndex: Map<String, SDevice> =
        logItem.devices.associateBy { it.id }

    val name: String get() = logItem.logName
    val cookerName: String get() = logItem.cookerName
    val startTime: Instant get() = logItem.startTime
    val devices: List<SDevice> get() = logItem.devices

    val filePath: String by lazy { existingFilePath ?: buildFilePath() }
    val fileName: String get() = File(filePath).name

    private val file: File by lazy {
        File(filePath).also { it.parentFile?.mkdirs() }
    }

    fun start() {
        writeJsonLine(LogHeader(cookerName = cookerName, devices = devices, startTime = startTime.toString()))
        log.info("Started log: {} -> {}", name, filePath)
    }

    fun stop() {
        log.info("Stopped log: {}", name)
    }

    /** Resume an existing log — no header is written (file already has one). */
    fun resume() {
        log.info("Resumed log: {} -> {}", name, filePath)
    }

    fun writeDataPoints(dataPoints: List<SDataPoint>, timedEvent: Boolean) {
        for (dp in dataPoints) {
            if (deviceIndex[dp.deviceId] == null) continue

            when {
                dp is SProbeDataPoint && timedEvent -> {
                    writeJsonLine(mapOf(
                        "type" to "probe",
                        "deviceId" to dp.deviceId,
                        "deviceName" to dp.deviceName,
                        "tempF" to dp.tempF,
                        "tempC" to dp.tempC,
                        "timestamp" to dp.collectedTime.toString()
                    ))
                }
                dp is SBlowerDataPoint && !timedEvent -> {
                    writeJsonLine(mapOf(
                        "type" to "blower",
                        "deviceId" to dp.deviceId,
                        "deviceName" to dp.deviceName,
                        "fanOn" to dp.fanOn,
                        "timestamp" to dp.collectedTime.toString()
                    ))
                }
            }
        }
    }

    fun addNote(note: String) {
        writeJsonLine(mapOf(
            "type" to "note",
            "note" to note,
            "timestamp" to Instant.now().toString()
        ))
    }

    fun writeWeather(weather: WeatherData) {
        writeJsonLine(mapOf(
            "type" to "weather",
            "data" to weather,
            "timestamp" to Instant.now().toString()
        ))
    }

    fun readAllDataPoints(): Map<String, List<SDataPoint>> {
        val result = mutableMapOf<String, MutableList<SDataPoint>>()
        readLines { node ->
            val type = node.get("type")?.asText()
            when (type) {
                "probe" -> {
                    val dp = SProbeDataPoint(
                        deviceId = node.get("deviceId").asText(),
                        deviceName = node.get("deviceName")?.asText(),
                        tempF = node.get("tempF").floatValue(),
                        tempC = node.get("tempC").floatValue(),
                        collectedTime = Instant.parse(node.get("timestamp").asText())
                    )
                    result.getOrPut(dp.deviceId) { mutableListOf() }.add(dp)
                }
                "blower" -> {
                    val dp = SBlowerDataPoint(
                        deviceId = node.get("deviceId").asText(),
                        deviceName = node.get("deviceName")?.asText(),
                        fanOn = node.get("fanOn").asBoolean(),
                        collectedTime = Instant.parse(node.get("timestamp").asText())
                    )
                    result.getOrPut(dp.deviceId) { mutableListOf() }.add(dp)
                }
            }
        }
        return result
    }

    fun readAllNotes(): List<LogNote> {
        val notes = mutableListOf<LogNote>()
        readLines { node ->
            if (node.get("type")?.asText() == "note") {
                notes += LogNote(
                    noteDate = Instant.parse(node.get("timestamp").asText()),
                    note = node.get("note").asText()
                )
            }
        }
        return notes
    }

    @Synchronized
    private fun writeJsonLine(data: Any) {
        try {
            file.appendText(objectMapper.writeValueAsString(data) + "\n")
        } catch (e: Exception) {
            log.error("Error writing to log file {}: {}", filePath, e.message)
        }
    }

    private fun readLines(handler: (com.fasterxml.jackson.databind.JsonNode) -> Unit) {
        if (!file.exists()) return
        file.useLines { lines ->
            lines.forEach { line ->
                if (line.isNotBlank()) {
                    try {
                        handler(objectMapper.readTree(line))
                    } catch (e: Exception) {
                        log.warn("Error parsing log line: {}", e.message)
                    }
                }
            }
        }
    }

    private fun buildFilePath(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_")
            .withZone(ZoneId.systemDefault())
        val dirFormatter = DateTimeFormatter.ofPattern("yyyy/MM")
            .withZone(ZoneId.systemDefault())

        val timestamp = formatter.format(startTime)
        val dirPath = dirFormatter.format(startTime)
        val safeName = name.replace(" ", "_")

        return "$logDir/cookLogs/$dirPath/${timestamp}${safeName}.jsonl"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CookLog::class.java)
        private val TIMESTAMP_PREFIX = Regex("^\\d{8}_\\d{2}_\\d{2}_")

        /**
         * Parse a .jsonl log file header and construct a CookLog pointing at that file.
         * Returns null if the file cannot be parsed as a valid cook log.
         *
         * Handles both old files (no @JsonTypeInfo discriminator in devices array) and
         * new files (with discriminator) by falling back to deviceType field inference.
         */
        fun fromFile(file: File, objectMapper: ObjectMapper, logDir: String): CookLog? {
            return try {
                val firstLine = file.useLines { it.firstOrNull() } ?: return null
                val header = objectMapper.readTree(firstLine)
                if (header.get("type")?.asText() != "header") return null

                val cookerName = header.get("cookerName")?.asText() ?: return null
                val startTime = Instant.parse(header.get("startTime").asText())

                val devicesNode = header.get("devices")
                val devices: List<SDevice> = if (devicesNode != null && devicesNode.isArray) {
                    devicesNode.mapNotNull { node ->
                        try {
                            // New files include the @JsonTypeInfo "type" discriminator
                            if (node.has("type")) {
                                val listType = objectMapper.typeFactory
                                    .constructCollectionType(List::class.java, SDevice::class.java)
                                val list: List<SDevice> = objectMapper.readValue(
                                    objectMapper.writeValueAsString(listOf(node)), listType
                                )
                                list.firstOrNull()
                            } else {
                                // Old files: infer concrete type from deviceType field
                                when (node.get("deviceType")?.asText()) {
                                    "PIT"    -> objectMapper.treeToValue(node, StokerPitProbe::class.java)
                                    "BLOWER" -> objectMapper.treeToValue(node, StokerFan::class.java)
                                    else     -> objectMapper.treeToValue(node, StokerProbe::class.java)
                                }
                            }
                        } catch (e: Exception) {
                            logger.warn("Skipping unparseable device in {}: {}", file.name, e.message)
                            null
                        }
                    }
                } else emptyList()

                val logName = file.nameWithoutExtension
                    .replaceFirst(TIMESTAMP_PREFIX, "")
                    .replace("_", " ")

                val logItem = LogItem(logName, cookerName, startTime, devices)
                CookLog(logItem, objectMapper, logDir, file.absolutePath)
            } catch (e: Exception) {
                logger.warn("Failed to parse log file {}: {}", file.name, e.message)
                null
            }
        }
    }
}

/**
 * Typed header record so Jackson sees List<SDevice> as the declared type
 * and emits @JsonTypeInfo discriminators for each device entry.
 */
data class LogHeader(
    val type: String = "header",
    val cookerName: String,
    val devices: List<SDevice>,
    val startTime: String
)
