package com.example.weatherapp

data class DailyModel(
    val day: String,
    val picPath: String, // Assuming iconRes is intended to be a path or resource identifier
    val status: String,
    val highTemp: Int,
    val lowTemp: Int,
    val hours: List<HourlyModel> // Include the list of hourly forecasts
)