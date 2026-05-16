package com.gbak.stoker

import com.gbak.stoker.monitor.StokerPitMonitor
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
class StokerWebApplication {

    @Bean
    fun startStokerMonitor(pitMonitor: StokerPitMonitor) = ApplicationRunner {
        pitMonitor.start()
    }
}

fun main(args: Array<String>) {
    runApplication<StokerWebApplication>(*args)
}
