package com.gbak.stoker.weather

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.gbak.stoker.model.weather.WeatherData
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@ConfigurationProperties(prefix = "stoker.weather")
data class WeatherProperties(
    val enabled: Boolean = false,
    val apiKey: String = "",
    val zipCode: String = "",
    val units: String = "imperial" // "imperial" for Fahrenheit, "metric" for Celsius
)

data class WeatherChangeEvent(val weatherData: WeatherData)

@Service
class WeatherService(
    private val weatherProperties: WeatherProperties,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(WeatherService::class.java)
    private val restClient = RestClient.create()

    @Volatile
    var currentWeather: WeatherData? = null
        internal set

    @Scheduled(fixedRate = 900_000, initialDelay = 5_000) // every 15 minutes
    fun fetchWeather() {
        if (!weatherProperties.enabled || weatherProperties.apiKey.isBlank()) {
            return
        }

        try {
            val url = "https://api.openweathermap.org/data/2.5/weather" +
                "?zip=${weatherProperties.zipCode},us" +
                "&appid=${weatherProperties.apiKey}" +
                "&units=${weatherProperties.units}"

            val response = restClient.get()
                .uri(url)
                .retrieve()
                .body(String::class.java)

            if (response != null) {
                val weather = parseOpenWeatherResponse(response)
                currentWeather = weather
                eventPublisher.publishEvent(WeatherChangeEvent(weather))
                log.debug("Weather updated: {}°F in {}", weather.currentTemperature, weather.city)
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch weather: {}", e.message)
        }
    }

    private fun parseOpenWeatherResponse(json: String): WeatherData {
        val root = objectMapper.readTree(json)
        val main = root.get("main")
        val wind = root.get("wind")
        val weather = root.get("weather")?.get(0)
        val sys = root.get("sys")

        return WeatherData(
            city = root.get("name")?.asText() ?: "",
            state = sys?.get("country")?.asText() ?: "",
            currentTemperature = main?.get("temp")?.asText() ?: "",
            humidity = main?.get("humidity")?.asText() ?: "",
            windSpeed = wind?.get("speed")?.asText() ?: "",
            text = weather?.get("description")?.asText() ?: "",
            currentImage = weather?.let {
                "https://openweathermap.org/img/wn/${it.get("icon")?.asText()}@2x.png"
            } ?: "",
            code = weather?.get("id")?.asText() ?: "",
            forecastHigh = main?.get("temp_max")?.asText() ?: "",
            forecastLow = main?.get("temp_min")?.asText() ?: "",
            forecastCondition = weather?.get("main")?.asText() ?: ""
        )
    }
}
