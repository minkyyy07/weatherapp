// WeatherModels.kt
package com.example.weatherapp

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val name: String,
    val dt: Long
)

data class Main(
    val temp: Float,
    val feelsLike: Float,
    val tempMin: Float,
    val tempMax: Float,
    val humidity: Int,
    val pressure: Int = 0
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Float,
    val deg: Int = 0
)

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City = City(0, "")
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>
)

data class City(
    val id: Long,
    val name: String
)