package com.gbak.stoker.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant

/**
 * Base data point from a Stoker device sensor.
 * Each reading has a device ID, optional name, timestamp, and whether it was a timed (periodic) event.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SProbeDataPoint::class, name = "probe"),
    JsonSubTypes.Type(value = SBlowerDataPoint::class, name = "blower")
)
sealed class SDataPoint {
    abstract val deviceId: String
    abstract val deviceName: String?
    abstract val collectedTime: Instant
    abstract val timedEvent: Boolean

    abstract fun hasChanged(other: SDataPoint): Boolean
    abstract fun displayValue(): String
}

data class SProbeDataPoint(
    override val deviceId: String,
    override val deviceName: String? = null,
    override val collectedTime: Instant = Instant.now(),
    override val timedEvent: Boolean = false,
    val tempC: Float,
    val tempF: Float
) : SDataPoint() {

    override fun hasChanged(other: SDataPoint): Boolean {
        if (other !is SProbeDataPoint) return true
        return tempF != other.tempF || tempC != other.tempC
    }

    override fun displayValue(): String = "%.1f°F / %.1f°C".format(tempF, tempC)
}

data class SBlowerDataPoint(
    override val deviceId: String,
    override val deviceName: String? = null,
    override val collectedTime: Instant = Instant.now(),
    override val timedEvent: Boolean = false,
    val fanOn: Boolean,
    val totalRuntime: Long = 0L,
    val blowerOnTime: Instant? = null
) : SDataPoint() {

    override fun hasChanged(other: SDataPoint): Boolean {
        if (other !is SBlowerDataPoint) return true
        return fanOn != other.fanOn
    }

    override fun displayValue(): String = if (fanOn) "ON" else "OFF"
}
