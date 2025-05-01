package com.example.weatherapp

data class DailyForecast(
    val date: String,
    val condition: String,
    val maxTemp: Int,
    val minTemp: Int,
    val weatherType: String,
    val icon: Int // Add this property
)