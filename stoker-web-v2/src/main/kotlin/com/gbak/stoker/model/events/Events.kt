package com.gbak.stoker.model.events

import com.gbak.stoker.model.SDataPoint
import java.time.Instant

enum class ConnectionEventType {
    NONE,
    CONFIG_UPDATE,
    CONFIG_UPDATE_REFRESH,
    LOST_CONNECTION,
    CONNECTION_ESTABLISHED,
    EXTENDED_CONNECTION_LOSS
}

enum class HardwareStatus {
    UNKNOWN, CONNECTED, DISCONNECTED
}

data class HardwareDeviceState(
    val status: HardwareStatus = HardwareStatus.UNKNOWN,
    val timestamp: Instant = Instant.now()
) {
    val isConnected: Boolean get() = status == HardwareStatus.CONNECTED
}

// Spring application events

data class StateChangeEvent(
    val eventType: ConnectionEventType,
    val timestamp: Instant = Instant.now()
)

data class DataPointEvent(
    val dataPoints: List<SDataPoint>,
    val timestamp: Instant = Instant.now()
)

data class ConfigChangeEvent(
    val eventType: ConfigEventType,
    val timestamp: Instant = Instant.now()
)

enum class ConfigEventType {
    CONFIG_INIT,
    CONFIG_LOADED,
    CONFIG_SAVED,
    CONFIG_UPDATE_DETECTED
}

enum class LogEventType {
    NONE, NEW, UPDATED, DELETED
}

data class LogEvent(
    val eventType: LogEventType,
    val cookerName: String,
    val logName: String
)
