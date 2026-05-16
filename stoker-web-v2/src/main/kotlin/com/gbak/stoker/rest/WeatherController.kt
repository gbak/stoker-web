package com.gbak.stoker.rest

import com.gbak.stoker.model.weather.WeatherData
import com.gbak.stoker.weather.WeatherService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/weather")
class WeatherController(
    private val weatherService: WeatherService
) {
    @GetMapping
    fun getWeather(): ResponseEntity<WeatherData> {
        val weather = weatherService.currentWeather
        return if (weather != null) ResponseEntity.ok(weather) else ResponseEntity.noContent().build()
    }
}
