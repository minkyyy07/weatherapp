package com.example.weatherapp
import com.example.weatherapp.HourlyModel

data class HourlyModel(
    val hour: String,
    val temp: Int,
    val picPath: String
)