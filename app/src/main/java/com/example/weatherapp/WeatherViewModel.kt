package com.example.weatherapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherViewModel : ViewModel() {
    // Замените на ваш ключ API от WeatherAPI.com
    private val apiKey = "053fafa63bb949d3ab4210023252404"

    var weatherState by mutableStateOf<WeatherUiState>(WeatherUiState.Loading)
        private set

    var forecastState by mutableStateOf<List<HourlyModel>>(emptyList())
        private set

    init {
        getWeatherData("London")
    }

    // Обновленный RetrofitClient для WeatherAPI.com
    object RetrofitClient {
        private const val BASE_URL = "https://api.weatherapi.com/v1/"

        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val weatherService: WeatherApiService by lazy {
            retrofit.create(WeatherApiService::class.java)
        }
    }

    // Новый интерфейс для WeatherAPI
    interface WeatherApiService {
        @GET("forecast.json")
        suspend fun getForecastData(
            @Query("key") apiKey: String,
            @Query("q") location: String,
            @Query("days") days: Int = 1,
            @Query("aqi") aqi: String = "no",
            @Query("alerts") alerts: String = "no"
        ): WeatherApiResponse
    }

    fun getWeatherData(city: String = "London") {
        viewModelScope.launch {
            weatherState = WeatherUiState.Loading

            try {
                // Map any special characters for API compatibility
                val apiCity = when (city.lowercase()) {
                    "nüremberg" -> "nuremberg"
                    else -> city
                }

                try {
                    val weatherResponse = RetrofitClient.weatherService.getForecastData(
                        apiKey = apiKey,
                        location = apiCity
                    )

                    val current = weatherResponse.current
                    val location = weatherResponse.location

                    val weatherData = WeatherData(
                        temperature = current.temp_c.toInt(),
                        description = current.condition.text.capitalize(),
                        minTemp = weatherResponse.forecast.forecastday.firstOrNull()?.day?.mintemp_c?.toInt() ?: 0,
                        maxTemp = weatherResponse.forecast.forecastday.firstOrNull()?.day?.maxtemp_c?.toInt() ?: 0,
                        humidity = current.humidity,
                        windSpeed = current.wind_kph / 3.6f, // конвертируем км/ч в м/с
                        cityName = if (city.equals("Nüremberg", ignoreCase = true)) "Nüremberg" else location.name,
                        weatherType = getWeatherType(current.condition.code)
                    )

                    weatherState = WeatherUiState.Success(weatherData)

                    // Извлекаем прогноз
                    createForecastFromApiResponse(weatherResponse)

                } catch (e: HttpException) {
                    weatherState = WeatherUiState.Error("API limit reached (${e.code()}). Try again later.")
                } catch (e: Exception) {
                    weatherState = WeatherUiState.Error("Network error: ${e.message}")
                }
            } catch (e: Exception) {
                weatherState = WeatherUiState.Error("Error: ${e.message}")
            }
        }
    }

    private fun createForecastFromApiResponse(response: WeatherApiResponse) {
        val hourlyData = response.forecast.forecastday.firstOrNull()?.hour ?: emptyList()

        // Получаем текущий час для фильтрации будущих часов
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Фильтруем только будущие часы текущего дня
        val futureHours = hourlyData.filter {
            val hourTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .parse(it.time)?.let { date ->
                    val cal = Calendar.getInstance()
                    cal.time = date
                    cal.get(Calendar.HOUR_OF_DAY)
                } ?: 0

            hourTime > currentHour
        }

        // Берем до 6 будущих часов
        forecastState = futureHours.take(6).map { hour ->
            val hourFormat = SimpleDateFormat("h a", Locale.getDefault())
            val hourDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(hour.time)

            HourlyModel(
                hour = hourFormat.format(hourDate ?: Date()),
                temp = hour.temp_c.toInt(),
                picPath = getWeatherType(hour.condition.code)
            )
        }

        // Если не хватает данных для 6 часов, добавляем генерированные
        if (forecastState.size < 6) {
            val currentWeather = (weatherState as? WeatherUiState.Success)?.data
            if (currentWeather != null) {
                val generatedHours = createGeneratedForecast(currentWeather, 6 - forecastState.size)
                forecastState = forecastState + generatedHours
            }
        }
    }

    private fun createGeneratedForecast(weatherData: WeatherData, count: Int): List<HourlyModel> {
        val lastHour = if (forecastState.isNotEmpty()) {
            val lastTimeStr = forecastState.last().hour
            val format = SimpleDateFormat("h a", Locale.getDefault())
            val date = format.parse(lastTimeStr) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.get(Calendar.HOUR_OF_DAY)
        } else {
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        }

        return List(count) { index ->
            val hour = (lastHour + index + 1) % 24
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
            }
            val format = SimpleDateFormat("h a", Locale.getDefault())

            HourlyModel(
                hour = format.format(calendar.time),
                temp = weatherData.temperature + (-2..2).random(),
                picPath = weatherData.weatherType
            )
        }
    }

    private fun createForecastFromWeather(weatherData: WeatherData) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        forecastState = List(6) { index ->
            val hour = (currentHour + index + 1) % 24
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
            }
            val format = SimpleDateFormat("h a", Locale.getDefault())

            HourlyModel(
                hour = format.format(calendar.time),
                temp = weatherData.temperature + (-2..2).random(),
                picPath = weatherData.weatherType
            )
        }
    }

    // Преобразование кодов состояния погоды от WeatherAPI в типы погоды для приложения
    private fun getWeatherType(conditionCode: Int): String {
        return when (conditionCode) {
            1000 -> "sunny" // Ясно
            in 1003..1009 -> "cloudy" // Облачно
            in 1030..1035 -> "cloudy" // Туман, дымка
            in 1063..1201 -> "rainy" // Дождь
            in 1204..1237 -> "cloudy_sunny" // Снег или смешанно
            in 1240..1282 -> "rainy" // Дождь, гроза
            else -> "cloudy_sunny"
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

// Классы для парсинга ответа от WeatherAPI.com
data class WeatherApiResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,
    val localtime_epoch: Long,
    val localtime: String
)

data class Current(
    val last_updated_epoch: Long,
    val last_updated: String,
    val temp_c: Float,
    val temp_f: Float,
    val is_day: Int,
    val condition: Condition,
    val wind_mph: Float,
    val wind_kph: Float,
    val wind_degree: Int,
    val wind_dir: String,
    val pressure_mb: Float,
    val pressure_in: Float,
    val precip_mm: Float,
    val precip_in: Float,
    val humidity: Int,
    val cloud: Int,
    val feelslike_c: Float,
    val feelslike_f: Float,
    val vis_km: Float,
    val vis_miles: Float,
    val uv: Float,
    val gust_mph: Float,
    val gust_kph: Float
)

data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val date_epoch: Long,
    val day: Day,
    val astro: Astro,
    val hour: List<Hour>
)

data class Day(
    val maxtemp_c: Float,
    val maxtemp_f: Float,
    val mintemp_c: Float,
    val mintemp_f: Float,
    val avgtemp_c: Float,
    val avgtemp_f: Float,
    val maxwind_mph: Float,
    val maxwind_kph: Float,
    val totalprecip_mm: Float,
    val totalprecip_in: Float,
    val totalsnow_cm: Float,
    val avgvis_km: Float,
    val avgvis_miles: Float,
    val avghumidity: Int,
    val daily_will_it_rain: Int,
    val daily_chance_of_rain: Int,
    val daily_will_it_snow: Int,
    val daily_chance_of_snow: Int,
    val condition: Condition,
    val uv: Float
)

data class Astro(
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val moon_phase: String,
    val moon_illumination: Int,
    val is_moon_up: Int,
    val is_sun_up: Int
)

data class Hour(
    val time_epoch: Long,
    val time: String,
    val temp_c: Float,
    val temp_f: Float,
    val is_day: Int,
    val condition: Condition,
    val wind_mph: Float,
    val wind_kph: Float,
    val wind_degree: Int,
    val wind_dir: String,
    val pressure_mb: Float,
    val pressure_in: Float,
    val precip_mm: Float,
    val precip_in: Float,
    val humidity: Int,
    val cloud: Int,
    val feelslike_c: Float,
    val feelslike_f: Float,
    val windchill_c: Float,
    val windchill_f: Float,
    val heatindex_c: Float,
    val heatindex_f: Float,
    val dewpoint_c: Float,
    val dewpoint_f: Float,
    val will_it_rain: Int,
    val chance_of_rain: Int,
    val will_it_snow: Int,
    val chance_of_snow: Int,
    val vis_km: Float,
    val vis_miles: Float,
    val gust_mph: Float,
    val gust_kph: Float,
    val uv: Float
)
