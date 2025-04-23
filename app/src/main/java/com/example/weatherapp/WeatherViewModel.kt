package com.example.weatherapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherViewModel : ViewModel() {
    var weatherState by mutableStateOf<WeatherUiState>(WeatherUiState.Loading)
        private set

    var forecastState by mutableStateOf<List<HourlyModel>>(emptyList())
        private set

    fun getWeatherData(city: String = "London") {
        viewModelScope.launch {
            try {
                weatherState = WeatherUiState.Loading

                // Simulate API delay
                kotlinx.coroutines.delay(1000)

                // Create mock weather data based on city
                val mockWeatherData = createMockWeatherData(city)

                // Update state with mock data
                weatherState = WeatherUiState.Success(mockWeatherData)

                // Create mock forecast data
                forecastState = createMockForecastData(city)
            } catch (e: Exception) {
                weatherState = WeatherUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun createMockWeatherData(city: String): WeatherData {
        // Different weather for different cities
        return when (city.lowercase()) {
            "london" -> WeatherData(
                temperature = 15,
                description = "Cloudy",
                minTemp = 12,
                maxTemp = 18,
                humidity = 78,
                windSpeed = 4.5f,
                cityName = "London",
                weatherType = "cloudy"
            )
            "new york" -> WeatherData(
                temperature = 22,
                description = "Sunny",
                minTemp = 18,
                maxTemp = 25,
                humidity = 65,
                windSpeed = 3.2f,
                cityName = "New York",
                weatherType = "sunny"
            )
            "tokyo" -> WeatherData(
                temperature = 28,
                description = "Rainy",
                minTemp = 24,
                maxTemp = 30,
                humidity = 85,
                windSpeed = 5.0f,
                cityName = "Tokyo",
                weatherType = "rainy"
            )
            else -> WeatherData(
                temperature = 20,
                description = "Clear",
                minTemp = 16,
                maxTemp = 23,
                humidity = 70,
                windSpeed = 3.8f,
                cityName = city.capitalize(),
                weatherType = "sunny"
            )
        }
    }

    private fun createMockForecastData(city: String): List<HourlyModel> {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return List(6) { index ->
            val hour = (currentHour + index + 1) % 24
            val format = SimpleDateFormat("h a", Locale.getDefault())
            val date = Date().apply {
                hours = hour
            }

            HourlyModel(
                hour = format.format(date),
                temp = when (index) {
                    0 -> 21
                    1 -> 20
                    2 -> 19
                    3 -> 18
                    4 -> 17
                    else -> 16
                },
                picPath = when (index % 3) {
                    0 -> "sunny"
                    1 -> "cloudy"
                    else -> "rainy"
                }
            )
        }
    }

    private fun getWeatherType(weatherMain: String): String {
        return when (weatherMain.lowercase()) {
            "clouds", "mist", "fog", "haze" -> "cloudy"
            "rain", "drizzle", "thunderstorm" -> "rainy"
            "clear" -> "sunny"
            "snow" -> "cloudy_sunny"
            else -> "sunny"
        }
    }

    // Extension function to capitalize first letter
    private fun String.capitalize(): String {
        return this.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }
    }

    sealed class WeatherUiState {
        object Loading : WeatherUiState()
        data class Success(val data: WeatherData) : WeatherUiState()
        data class Error(val message: String) : WeatherUiState()
    }
}

data class WeatherData(
    val temperature: Int,
    val description: String,
    val minTemp: Int,
    val maxTemp: Int,
    val humidity: Int,
    val windSpeed: Float,
    val cityName: String,
    val weatherType: String
)