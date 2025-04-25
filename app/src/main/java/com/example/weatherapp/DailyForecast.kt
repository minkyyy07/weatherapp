package com.example.weatherapp

data class DailyForecast(
    val date: String,
    val maxTemp: Int,
    val minTemp: Int,
    val weatherType: String,
    val condition: String
)