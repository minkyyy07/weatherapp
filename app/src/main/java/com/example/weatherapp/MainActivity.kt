package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val cities = listOf("London", "New York", "Tokyo", "Paris", "Moscow", "Berlin", "Sydney", "Nüremberg")
    var selectedCity by remember { mutableStateOf("London") }
    var expanded by remember { mutableStateOf(false) }

    // Add debounce mechanism to prevent too many rapid API calls
    var lastApiCallTime by remember { mutableStateOf(0L) }
    val debounceTime = 2000L // 2 seconds between API calls

    LaunchedEffect(key1 = selectedCity) {
        val currentTime = System.currentTimeMillis()

        // Check if enough time has passed since the last API call
        if (currentTime - lastApiCallTime >= debounceTime) {
            // Add a small delay to prevent immediate API calls
            delay(300)
            viewModel.getWeatherData(selectedCity)
            lastApiCallTime = System.currentTimeMillis()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(android.graphics.Color.parseColor("#59469d")),
                        Color(android.graphics.Color.parseColor("#643d67")),
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (val state = viewModel.weatherState) {
            is WeatherViewModel.WeatherUiState.Loading -> {
                CircularProgressIndicator(color = Color.White)
            }
            is WeatherViewModel.WeatherUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.getWeatherData(selectedCity) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.purple)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
            is WeatherViewModel.WeatherUiState.Success -> {
                val data = state.data
                // If the city is Nuremberg but we're displaying Nüremberg, fix the display name
                val displayData = if (selectedCity == "Nüremberg" && data.cityName.equals("Nuremberg", ignoreCase = true)) {
                    data.copy(cityName = "Nüremberg")
                } else data

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            // City selector dropdown
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = colorResource(id = R.color.purple),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .clickable { expanded = true }
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Location",
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = selectedCity,
                                                color = Color.White,
                                                fontSize = 18.sp
                                            )
                                        }
                                        Icon(
                                            imageVector = if (expanded)
                                                Icons.Default.KeyboardArrowUp
                                            else
                                                Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle Dropdown",
                                            tint = Color.White
                                        )
                                    }

                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(
                                            colorResource(id = R.color.purple)
                                        )
                                    ) {
                                        cities.forEach { city ->
                                            DropdownMenuItem(
                                                text = { Text(text = city, color = Color.White) },
                                                onClick = {
                                                    selectedCity = city
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = displayData.description,
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                textAlign = TextAlign.Center
                            )

                            val weatherIconId = when (displayData.weatherType) {
                                "cloudy" -> R.drawable.cloudy
                                "sunny" -> R.drawable.sunny
                                "rainy" -> R.drawable.rain
                                else -> R.drawable.cloudy_sunny
                            }

                            Image(
                                painter = painterResource(id = weatherIconId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(150.dp)
                                    .padding(top = 8.dp)
                            )

                            Text(
                                text = "Today, ${getCurrentTime()}",
                                fontSize = 19.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "${displayData.temperature}°C",
                                fontSize = 63.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "H:${displayData.maxTemp}° L:${displayData.minTemp}°",
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                                    .background(
                                        color = colorResource(id = R.color.purple),
                                        shape = RoundedCornerShape(25.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    WeatherDetailItem(
                                        icon = R.drawable.rain,
                                        value = "30%",
                                        label = "Rain"
                                    )
                                    WeatherDetailItem(
                                        icon = R.drawable.wind,
                                        value = "${displayData.windSpeed} m/s",
                                        label = "Wind Speed"
                                    )
                                    WeatherDetailItem(
                                        icon = R.drawable.humidity,
                                        value = "${displayData.humidity}%",
                                        label = "Humidity"
                                    )
                                }
                            }

                            Text(
                                text = "Today's Weather",
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(viewModel.forecastState) { item ->
                                    FutureModelViewHolder(model = item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun FutureModelViewHolder(model: HourlyModel) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .wrapContentHeight()
            .padding(4.dp)
            .background(
                color = colorResource(id = R.color.purple),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = model.hour,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        Image(
            painter = painterResource(
                id = when (model.picPath) {
                    "cloudy" -> R.drawable.cloudy
                    "sunny" -> R.drawable.sunny
                    "wind" -> R.drawable.wind
                    "rainy" -> R.drawable.rain
                    "cloudy_sunny" -> R.drawable.cloudy
                    else -> R.drawable.sunny
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .padding(bottom = 8.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "${model.temp}°",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeatherDetailItem(icon: Int, value: String, label: String) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(34.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Center
        )
    }
}