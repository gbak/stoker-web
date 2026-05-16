package com.gbak.stoker.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.gbak.stoker.model.SBlowerDataPoint
import com.gbak.stoker.model.SProbeDataPoint
import com.gbak.stoker.model.devices.StokerFan
import com.gbak.stoker.model.devices.StokerPitProbe
import com.gbak.stoker.model.devices.StokerProbe
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig(private val objectMapper: ObjectMapper) {

    @PostConstruct
    fun configurePolymorphicTypes() {
        // Register subtypes for sealed class hierarchies so Jackson can serialize/deserialize them
        objectMapper.registerSubtypes(
            NamedType(SProbeDataPoint::class.java, "probe"),
            NamedType(SBlowerDataPoint::class.java, "blower"),
            NamedType(StokerProbe::class.java, "probe"),
            NamedType(StokerPitProbe::class.java, "pitProbe"),
            NamedType(StokerFan::class.java, "fan")
        )
    }
}
