package com.gbak.stoker.model

import com.gbak.stoker.model.devices.SDevice
import com.gbak.stoker.model.devices.StokerPitProbe
import com.gbak.stoker.model.devices.StokerProbe

data class Cooker(
    val cookerName: String,
    val pitSensor: StokerPitProbe? = null,
    val probes: List<StokerProbe> = emptyList()
) {
    fun allDevices(): List<SDevice> = buildList {
        pitSensor?.let {
            add(it)
            it.fan?.let { fan -> add(fan) }
        }
        addAll(probes)
    }

    fun findDeviceById(deviceId: String): SDevice? =
        allDevices().firstOrNull { it.id == deviceId }
}

data class CookerList(
    val cookers: List<Cooker> = emptyList(),
    val configRequired: Boolean = false
) {
    fun findDeviceById(deviceId: String): SDevice? =
        cookers.firstNotNullOfOrNull { it.findDeviceById(deviceId) }
}

data class ConfigurationSettings(
    val cookerList: CookerList,
    val availableDevices: List<SDevice> = emptyList()
)
