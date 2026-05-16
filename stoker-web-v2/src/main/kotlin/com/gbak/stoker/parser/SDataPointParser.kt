package com.gbak.stoker.parser

import com.gbak.stoker.config.StokerDeviceConfig
import com.gbak.stoker.model.SBlowerDataPoint
import com.gbak.stoker.model.SDataPoint
import com.gbak.stoker.model.SProbeDataPoint
import com.gbak.stoker.model.devices.StokerPitProbe
import com.gbak.stoker.stoker.StokerDeviceDiscovery
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

class InvalidDataPointException(message: String) : Exception(message)

/**
 * Parses raw telnet output from the Stoker device into data points.
 *
 * Example input line:
 * E70000116F279030: 2 28.1 82.6 -6.9 0.2 1.1 0.9 25.9 78.6 PID: NORM tgt:26.1 error:1.2 drive:0 istate:0 on:1 off:9 blwr:off
 *
 * Format: 16-char device ID followed by colon, then whitespace-separated tokens.
 * Tokens 9 and 10 (1-indexed) are tempC and tempF.
 * If tokens beyond 10 exist, this is a pit probe with a fan (look for "blwr:on" or "blwr:off").
 */
@Component
class SDataPointParser(private val discovery: StokerDeviceDiscovery) {

    private val log = LoggerFactory.getLogger(SDataPointParser::class.java)

    fun parse(rawLine: String, deviceConfig: StokerDeviceConfig): List<SDataPoint> {
        val line = rawLine.lowercase()
        val colonPos = line.indexOf(':')

        if (colonPos != 16) {
            throw InvalidDataPointException(line)
        }

        val deviceId = line.substring(0, colonPos).uppercase()
        val tokens = line.split("\\s+".toRegex())

        // Tokens are 0-indexed: token[0] is "deviceid:", tokens[8] and [9] are tempC and tempF
        val tempC = tokens.getOrNull(8)?.toFloatOrNull() ?: 0f
        val tempF = tokens.getOrNull(9)?.toFloatOrNull() ?: 0f

        val now = Instant.now()
        val results = mutableListOf<SDataPoint>()

        // Check for fan/blower data
        val blowerToken = tokens.firstOrNull { it.startsWith("blwr:") }
        if (blowerToken != null) {
            val fanOn = blowerToken.substringAfter("blwr:") == "on"

            // Resolve the fan device ID: prefer the user-saved config, then the
            // hardware discovery (stoker.json), skip if neither has it yet.
            val pitDevice = deviceConfig.findDeviceById(deviceId)
            val fanId = when {
                pitDevice is StokerPitProbe && pitDevice.fan != null -> pitDevice.fan.id
                else -> discovery.getFanIdForPit(deviceId)
            }

            if (fanId != null) {
                results += SBlowerDataPoint(
                    deviceId = fanId,
                    collectedTime = now,
                    fanOn = fanOn
                )
            }
        }

        results += SProbeDataPoint(
            deviceId = deviceId,
            collectedTime = now,
            tempF = tempF,
            tempC = tempC
        )

        return results
    }
}
