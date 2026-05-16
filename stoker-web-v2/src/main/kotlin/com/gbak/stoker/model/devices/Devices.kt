package com.gbak.stoker.model.devices

enum class DeviceType {
    UNKNOWN, PIT, FOOD, BLOWER;

    companion object {
        fun fromString(s: String): DeviceType =
            entries.firstOrNull { it.name.equals(s, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class AlarmType {
    NONE, ALARM_FOOD, ALARM_FIRE;

    companion object {
        fun fromString(s: String): AlarmType =
            entries.firstOrNull { it.name.equals(s, ignoreCase = true) } ?: NONE
    }
}

@com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, property = "type")
@com.fasterxml.jackson.annotation.JsonSubTypes(
    com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = StokerProbe::class, name = "probe"),
    com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = StokerPitProbe::class, name = "pitProbe"),
    com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = StokerFan::class, name = "fan")
)
sealed class SDevice {
    abstract val id: String
    abstract val name: String
    abstract val deviceType: DeviceType
}

data class StokerProbe(
    override val id: String,
    override val name: String = "",
    override val deviceType: DeviceType = DeviceType.FOOD,
    val targetTemp: Int = 0,
    val lowerTempAlarm: Int = 0,
    val upperTempAlarm: Int = 0,
    val alarmType: AlarmType = AlarmType.NONE,
    val currentTemp: Float = 0f
) : SDevice()

data class StokerFan(
    override val id: String,
    override val name: String = "",
    override val deviceType: DeviceType = DeviceType.BLOWER,
    val fanOn: Boolean = false,
    val totalRuntime: Long = 0L
) : SDevice()

data class StokerPitProbe(
    override val id: String,
    override val name: String = "",
    override val deviceType: DeviceType = DeviceType.PIT,
    val targetTemp: Int = 0,
    val lowerTempAlarm: Int = 0,
    val upperTempAlarm: Int = 0,
    val alarmType: AlarmType = AlarmType.NONE,
    val currentTemp: Float = 0f,
    val fan: StokerFan? = null
) : SDevice()
