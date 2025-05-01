package com.example.weatherapp
import com.example.weatherapp.HourlyModel

data class HourlyModel(
    val hour: String,
    val temp: Int,
    val picPath: String,
    val humidity: Int = 0,           // Added with default
    val windSpeed: Float = 0f,       // Added with default
    val precipitationValue: Float = 0f,  // Added with default
    val icon: String = "sunny"       // Added with default
)