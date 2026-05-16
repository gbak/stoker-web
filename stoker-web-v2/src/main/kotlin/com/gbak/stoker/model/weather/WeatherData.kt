package com.gbak.stoker.model.weather

data class WeatherData(
    val city: String = "",
    val state: String = "",
    val currentTemperature: String = "",
    val humidity: String = "",
    val windSpeed: String = "",
    val text: String = "",
    val currentImage: String = "",
    val code: String = "",
    val forecastHigh: String = "",
    val forecastLow: String = "",
    val forecastCondition: String = ""
)
