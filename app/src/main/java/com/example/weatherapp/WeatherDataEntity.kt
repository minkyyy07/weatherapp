package com.example.weatherapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_data")
data class WeatherDataEntity(
    @PrimaryKey
    @ColumnInfo(name = "city")
    val city: String,

    @ColumnInfo(name = "temperature")
    val temperature: Double,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "weatherData")
    val weatherData: String // JSON string of complete weather data
)