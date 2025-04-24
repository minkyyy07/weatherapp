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
    private val apiKey = "bd5e378503939ddaee76f12ad7a97608"

    var weatherState by mutableStateOf<WeatherUiState>(WeatherUiState.Loading)
        private set

    var forecastState by mutableStateOf<List<HourlyModel>>(emptyList())
        private set

    fun getWeatherData(city: String = "London") {
        viewModelScope.launch {
            try {
                weatherState = WeatherUiState.Loading

                if (apiKey != "YOUR_API_KEY") {
                    val weatherResponse = RetrofitClient.weatherService.getCurrentWeather(
                        location = city,
                        apiKey = apiKey
                    )

                    val weatherData = WeatherData(
                        temperature = weatherResponse.main.temp.toInt(),
                        description = weatherResponse.weather.firstOrNull()?.description ?: "Unknown",
                        minTemp = weatherResponse.main.tempMin.toInt(),
                        maxTemp = weatherResponse.main.tempMax.toInt(),
                        humidity = weatherResponse.main.humidity,
                        windSpeed = weatherResponse.wind.speed,
                        cityName = weatherResponse.name,
                        weatherType = getWeatherType(weatherResponse.weather.firstOrNull()?.main ?: "")
                    )

                    weatherState = WeatherUiState.Success(weatherData)

                    val forecastResponse = RetrofitClient.weatherService.getForecast(
                        location = city,
                        apiKey = apiKey
                    )

                    val hourlyForecast = forecastResponse.list.take(6).map { item ->
                        val dateTime = Date(item.dt * 1000L)
                        val format = SimpleDateFormat("h a", Locale.getDefault())

                        HourlyModel(
                            hour = format.format(dateTime),
                            temp = item.main.temp.toInt(),
                            picPath = getWeatherType(item.weather.firstOrNull()?.main ?: "")
                        )
                    }

                    forecastState = hourlyForecast
                } else {
                    kotlinx.coroutines.delay(1000)

                    val mockWeatherData = createMockWeatherData(city)
                    weatherState = WeatherUiState.Success(mockWeatherData)
                    forecastState = createMockForecastData(city)
                }
            } catch (e: Exception) {
                weatherState = WeatherUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun createMockWeatherData(city: String): WeatherData {
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
            "nüremberg" -> WeatherData(
                temperature = 14,
                description = "Light Rain",
                minTemp = 10,
                maxTemp = 16,
                humidity = 75,
                windSpeed = 3.9f,
                cityName = "Nüremberg",
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