package com.gbak.stoker.log

import com.fasterxml.jackson.databind.ObjectMapper
import com.gbak.stoker.model.log.LogNote
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * One-time converter: old Stoker-Web text log format -> new JSON-lines format.
 *
 * Old format line types (prefix before first |):
 *   c|<id>|<cooker_name>           - Cooker header
 *   d|<id>|<name>|<alarms>|<target>|<hi>|<lo>  - Device config
 *   p|<date>|<index>|<tempF>|<tempC>            - Probe data (within a data line)
 *   b|<date>|<index>|<state>                    - Blower data (within a data line)
 *   n|<date>|<note>                             - User note
 *   w|<date>|<weather_data>                     - Weather data
 *
 * Data lines actually look like: <date>|p|<index>|<id>|<tempF>|<tempC>|...|p|...
 * The date prefix applies to all data entries on the line.
 */
object LogConverter {

    private val log = LoggerFactory.getLogger(LogConverter::class.java)
    private val objectMapper = ObjectMapper()
    private val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    // Maps 2-digit index back to device ID (populated from 'd' config lines)
    data class DeviceInfo(val id: String, val name: String, val index: String)

    fun convert(inputFile: File, outputFile: File) {
        log.info("Converting {} -> {}", inputFile.path, outputFile.path)

        outputFile.parentFile?.mkdirs()

        val devicesByIndex = mutableMapOf<String, DeviceInfo>()
        val devicesById = mutableMapOf<String, DeviceInfo>()
        var cookerName = "Unknown"
        var lineCount = 0
        var convertedCount = 0

        inputFile.useLines { lines ->
            lines.forEach { line ->
                lineCount++
                if (line.length < 2) return@forEach

                val prefix = line.substring(0, 1)
                try {
                    when (prefix) {
                        "c" -> {
                            // c|<id>|<cooker_name>
                            val parts = line.split("|")
                            if (parts.size >= 3) {
                                cookerName = parts[2].trim()
                                val jsonLine = objectMapper.writeValueAsString(mapOf(
                                    "type" to "header",
                                    "cookerName" to cookerName,
                                    "startTime" to Instant.now().toString()
                                ))
                                outputFile.appendText(jsonLine + "\n")
                                convertedCount++
                            }
                        }
                        "d" -> {
                            // d|<index>|<name>|<id>|<alarms>|<target>|<hi>|<lo>
                            val parts = line.split("|")
                            if (parts.size >= 4) {
                                val index = parts[1].trim()
                                val name = parts[2].trim()
                                val id = parts[3].trim()
                                val info = DeviceInfo(id, name, index)
                                devicesByIndex[index] = info
                                devicesById[id] = info
                            }
                        }
                        "n" -> {
                            // n|<date>|<note>
                            val parts = line.split("|", limit = 3)
                            if (parts.size >= 3) {
                                val timestamp = parseDate(parts[1].trim())
                                val jsonLine = objectMapper.writeValueAsString(mapOf(
                                    "type" to "note",
                                    "note" to parts[2].trim(),
                                    "timestamp" to timestamp.toString()
                                ))
                                outputFile.appendText(jsonLine + "\n")
                                convertedCount++
                            }
                        }
                        "w" -> {
                            // w|<date>|<weather_json_or_text>
                            val parts = line.split("|", limit = 3)
                            if (parts.size >= 3) {
                                val timestamp = parseDate(parts[1].trim())
                                val jsonLine = objectMapper.writeValueAsString(mapOf(
                                    "type" to "weather",
                                    "data" to parts[2].trim(),
                                    "timestamp" to timestamp.toString()
                                ))
                                outputFile.appendText(jsonLine + "\n")
                                convertedCount++
                            }
                        }
                        else -> {
                            // Data lines: date followed by |p| or |b| entries
                            // Format: <date>|p|<index>|<id>|<tempF>|<tempC>|...|p|...
                            // Or: <date>|b|<index>|<id>|<state>|...
                            convertDataLine(line, devicesByIndex, outputFile)?.let {
                                convertedCount += it
                            }
                        }
                    }
                } catch (e: Exception) {
                    log.warn("Error converting line {}: {}", lineCount, e.message)
                }
            }
        }

        log.info("Conversion complete: {} lines read, {} entries written", lineCount, convertedCount)
    }

    private fun convertDataLine(
        line: String,
        devicesByIndex: Map<String, DeviceInfo>,
        outputFile: File
    ): Int? {
        val segments = line.split("|")
        if (segments.size < 4) return null

        var count = 0
        val dateStr = segments[0].trim()
        val timestamp = try { parseDate(dateStr) } catch (_: Exception) { return null }

        var i = 1
        while (i < segments.size) {
            val entryType = segments.getOrNull(i)?.trim() ?: break
            when (entryType) {
                "p" -> {
                    // p|<index>|<id>|<tempF>|<tempC>
                    val index = segments.getOrNull(i + 1)?.trim()
                    val id = segments.getOrNull(i + 2)?.trim() ?: ""
                    val tempF = segments.getOrNull(i + 3)?.trim()?.toFloatOrNull() ?: 0f
                    val tempC = segments.getOrNull(i + 4)?.trim()?.toFloatOrNull() ?: 0f
                    val deviceName = devicesByIndex[index]?.name

                    val jsonLine = objectMapper.writeValueAsString(mapOf(
                        "type" to "probe",
                        "deviceId" to id.ifEmpty { devicesByIndex[index]?.id ?: "unknown" },
                        "deviceName" to deviceName,
                        "tempF" to tempF,
                        "tempC" to tempC,
                        "timestamp" to timestamp.toString()
                    ))
                    outputFile.appendText(jsonLine + "\n")
                    count++
                    i += 5
                }
                "b" -> {
                    // b|<index>|<id>|<state>
                    val index = segments.getOrNull(i + 1)?.trim()
                    val id = segments.getOrNull(i + 2)?.trim() ?: ""
                    val state = segments.getOrNull(i + 3)?.trim()
                    val fanOn = state == "1" || state?.lowercase() == "on"
                    val deviceName = devicesByIndex[index]?.name

                    val jsonLine = objectMapper.writeValueAsString(mapOf(
                        "type" to "blower",
                        "deviceId" to id.ifEmpty { devicesByIndex[index]?.id ?: "unknown" },
                        "deviceName" to deviceName,
                        "fanOn" to fanOn,
                        "timestamp" to timestamp.toString()
                    ))
                    outputFile.appendText(jsonLine + "\n")
                    count++
                    i += 4
                }
                else -> i++
            }
        }
        return count
    }

    private fun parseDate(dateStr: String): Instant {
        val ldt = LocalDateTime.parse(dateStr, dateFormat)
        return ldt.atZone(ZoneId.systemDefault()).toInstant()
    }
}
