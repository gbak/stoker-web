package com.gbak.stoker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stoker")
data class StokerProperties(
    val device: DeviceProperties = DeviceProperties(),
    val timeout: TimeoutProperties = TimeoutProperties(),
    val alarm: AlarmProperties = AlarmProperties(),
    val display: DisplayProperties = DisplayProperties(),
    val dataDir: String = "./data"
)

data class DeviceProperties(
    val ip: String = "192.168.10.139",
    val port: Int = 23,
    val login: String = "root",
    val password: String = "tini"
)

data class TimeoutProperties(
    val extendedDisconnectMinutes: Int = 30,
    val reconnectIntervalMinutes: Int = 15
)

data class AlarmProperties(
    val source: String = "local",
    val repeatIntervalMinutes: Int = 10
)

data class DisplayProperties(
    val tempType: String = "gauge",
    val gaugeMin: Int = 0,
    val gaugeMax: Int = 400,
    val gaugeStepping: Int = 50
)
