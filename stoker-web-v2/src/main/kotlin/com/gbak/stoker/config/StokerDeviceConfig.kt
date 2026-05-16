package com.gbak.stoker.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.gbak.stoker.model.Cooker
import com.gbak.stoker.model.CookerList
import com.gbak.stoker.model.devices.SDevice
import com.gbak.stoker.model.events.ConfigChangeEvent
import com.gbak.stoker.model.events.ConfigEventType
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import jakarta.annotation.PostConstruct

@Service
class StokerDeviceConfig(
    private val properties: StokerProperties,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(StokerDeviceConfig::class.java)

    private val deviceCache = ConcurrentHashMap<String, SDevice>()
    private val cookerNameCache = ConcurrentHashMap<String, String>()

    @Volatile
    var cookerList: CookerList = CookerList()
        internal set

    private val configPath: Path
        get() = Path.of(properties.dataDir, "CookerConfig.json")

    @PostConstruct
    fun init() {
        loadConfig()
        eventPublisher.publishEvent(ConfigChangeEvent(ConfigEventType.CONFIG_INIT))
    }

    fun findDeviceById(deviceId: String): SDevice? = deviceCache[deviceId]

    fun getCookerNameForDeviceId(deviceId: String): String? = cookerNameCache[deviceId]

    fun updateConfig(newCookerList: CookerList) {
        this.cookerList = newCookerList
        rebuildCaches()
        saveConfig()
        eventPublisher.publishEvent(ConfigChangeEvent(ConfigEventType.CONFIG_SAVED))
    }

    fun loadConfig() {
        val path = configPath
        if (Files.exists(path)) {
            try {
                cookerList = objectMapper.readValue(path.toFile(), CookerList::class.java)
                rebuildCaches()
                log.info("Loaded cooker config from {}", path)
                eventPublisher.publishEvent(ConfigChangeEvent(ConfigEventType.CONFIG_LOADED))
            } catch (e: Exception) {
                log.warn("Failed to load cooker config: {}", e.message)
            }
        } else {
            log.info("No cooker config found at {}, starting fresh", path)
        }
    }

    fun saveConfig() {
        val path = configPath
        try {
            Files.createDirectories(path.parent)
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), cookerList)
            log.info("Saved cooker config to {}", path)
        } catch (e: Exception) {
            log.error("Failed to save cooker config: {}", e.message)
        }
    }

    private fun rebuildCaches() {
        deviceCache.clear()
        cookerNameCache.clear()
        cookerList.cookers.forEach { cooker ->
            cooker.allDevices().forEach { device ->
                deviceCache[device.id] = device
                cookerNameCache[device.id] = cooker.cookerName
            }
        }
    }
}
