package com.gbak.stoker.model.alerts

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ConnectionChangeAlert::class, name = "connectionChange"),
    JsonSubTypes.Type(value = StokerAlarmAlert::class,     name = "stokerAlarm"),
    JsonSubTypes.Type(value = TempAlarmAlert::class,       name = "tempAlarm"),
    JsonSubTypes.Type(value = TimeAlert::class,            name = "timeAlert"),
)
sealed class AlertModel {
    abstract val name: String
    abstract val enabled: Boolean
    abstract val availableDeliveryMethods: Set<String>
    abstract val configuredDeliveryMethods: Set<String>
}

data class ConnectionChangeAlert(
    override val enabled: Boolean = false,
    override val availableDeliveryMethods: Set<String> = emptySet(),
    override val configuredDeliveryMethods: Set<String> = emptySet()
) : AlertModel() {
    override val name: String = "Connection Change Alarm"
}

data class StokerAlarmAlert(
    override val enabled: Boolean = false,
    override val availableDeliveryMethods: Set<String> = emptySet(),
    override val configuredDeliveryMethods: Set<String> = emptySet()
) : AlertModel() {
    override val name: String = "Stoker Alarm"
}

data class TempAlarmAlert(
    override val enabled: Boolean = false,
    override val availableDeliveryMethods: Set<String> = emptySet(),
    override val configuredDeliveryMethods: Set<String> = emptySet()
) : AlertModel() {
    override val name: String = "Temp Alarm"
}

data class TimeAlert(
    override val enabled: Boolean = false,
    override val availableDeliveryMethods: Set<String> = emptySet(),
    override val configuredDeliveryMethods: Set<String> = emptySet()
) : AlertModel() {
    override val name: String = "Time Alert"
}

data class BrowserAlarm(
    val alarmName: String,
    val message: String,
    val suppressDuration: String? = null
)
