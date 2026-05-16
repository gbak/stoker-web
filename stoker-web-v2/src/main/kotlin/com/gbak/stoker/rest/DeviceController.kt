package com.gbak.stoker.rest

import com.gbak.stoker.config.StokerDeviceConfig
import com.gbak.stoker.model.ConfigurationSettings
import com.gbak.stoker.model.CookerList
import com.gbak.stoker.model.SDataPoint
import com.gbak.stoker.monitor.StokerPitMonitor
import com.gbak.stoker.stoker.StokerDeviceDiscovery
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class DeviceController(
    private val pitMonitor: StokerPitMonitor,
    private val deviceConfig: StokerDeviceConfig,
    private val discovery: StokerDeviceDiscovery
) {
    @GetMapping("/status")
    fun getStatus(): Map<String, Any> = mapOf(
        "connected" to pitMonitor.isConnected,
        "active" to pitMonitor.isActive,
        "hardwareState" to pitMonitor.hardwareState.status.name
    )

    @GetMapping("/devices")
    fun getAllDevices(): List<SDataPoint> =
        pitMonitor.getCurrentTemps()

    @GetMapping("/devices/{id}")
    fun getDevice(@PathVariable id: String): ResponseEntity<SDataPoint> {
        val dp = pitMonitor.getCurrentTemp(id)
        return if (dp != null) ResponseEntity.ok(dp) else ResponseEntity.notFound().build()
    }

    /**
     * Returns the current cooker configuration plus any devices seen on the wire
     * that are not yet assigned to a cooker (availableDevices). This drives the
     * device-configuration dialog in the frontend.
     */
    @GetMapping("/configuration")
    fun getConfiguration(): ConfigurationSettings {
        val cookerList = deviceConfig.cookerList
        val assignedIds = cookerList.cookers
            .flatMap { it.allDevices() }
            .map { it.id }
            .toSet()

        // Use hardware-discovered devices (real IDs from stoker.json) as the
        // authoritative source. Fall back to an empty list if discovery hasn't
        // run yet (e.g. not yet connected).
        val availableDevices = discovery.getDiscoveredDevices()
            .filter { it.id !in assignedIds }

        return ConfigurationSettings(cookerList = cookerList, availableDevices = availableDevices)
    }

    @PutMapping("/configuration")
    fun updateConfiguration(@RequestBody cookerList: CookerList): ResponseEntity<Void> {
        deviceConfig.updateConfig(cookerList)
        return ResponseEntity.ok().build()
    }
}
