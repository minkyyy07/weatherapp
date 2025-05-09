package com.example.weatherapp

import androidx.annotation.DrawableRes

data class DailyForecast(
    val date: String, // Изменено с 'day' на 'date'
    val maxTemp: Int,
    val minTemp: Int,
    val weatherType: String, // Добавлен параметр weatherType
    val condition: String, // Добавлен параметр condition
    @DrawableRes val icon: Int // Изменено с 'iconRes' на 'icon' и добавлен @DrawableRes
)