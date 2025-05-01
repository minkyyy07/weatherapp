package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Brush
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.text.toFloat
import kotlin.times

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val countries = listOf("United Kingdom", "USA", "Japan", "France", "Russia", "Germany", "Australia", "Ukraine")
    var selectedCountry by remember { mutableStateOf("United Kingdom") }
    var selectedCity by remember { mutableStateOf("London") }
    var expandedCountry by remember { mutableStateOf(false) }
    var expandedCity by remember { mutableStateOf(false) }
    val favoritesManager = remember { FavoritesManager() }

    // Map of countries to their cities
    val citiesByCountry = remember {
        mapOf(
            "United Kingdom" to listOf("London", "Manchester", "Liverpool"),
            "USA" to listOf("New York", "Los Angeles", "Chicago"),
            "Japan" to listOf("Tokyo", "Osaka", "Kyoto"),
            "France" to listOf("Paris", "Lyon", "Marseille"),
            "Russia" to listOf("Moscow", "Saint Petersburg", "Kazan"),
            "Germany" to listOf("Berlin", "Munich", "Nüremberg"),
            "Australia" to listOf("Sydney", "Melbourne", "Brisbane"),
            "Ukraine" to listOf("Kyiv", "Irpin", "Odesa")
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Today", "Weekly", "Charts")

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
                PulsatingLoadingAnimation()
            }
            is WeatherViewModel.WeatherUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: " + state.message,
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedButton(text = "Retry") {
                        viewModel.getWeatherData(selectedCity)
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Country selector
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedCountry,
                            onExpandedChange = { expandedCountry = !expandedCountry }
                        ) {
                            Row(
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .background(
                                        color = colorResource(id = R.color.purple),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Country",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedCountry,
                                        color = Color.White
                                    )
                                }
                                Icon(
                                    imageVector = if (expandedCountry)
                                        Icons.Default.KeyboardArrowUp
                                    else
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Arrow",
                                    tint = Color.White
                                )
                            }

                            ExposedDropdownMenu(
                                expanded = expandedCountry,
                                onDismissRequest = { expandedCountry = false },
                                modifier = Modifier.background(colorResource(id = R.color.purple))
                            ) {
                                countries.forEachIndexed { index, country ->
                                    AnimatedDropdownMenuItem(
                                        text = { Text(country, color = Color.White) },
                                        onClick = {
                                            selectedCountry = country
                                            selectedCity = citiesByCountry[country]?.first() ?: ""
                                            expandedCountry = false
                                        },
                                        index = index
                                    )
                                }
                            }
                        }
                    }

                    // City selector
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedCity,
                            onExpandedChange = { expandedCity = !expandedCity }
                        ) {
                            Row(
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .background(
                                        color = colorResource(id = R.color.purple),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "City",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedCity,
                                        color = Color.White
                                    )
                                }
                                Icon(
                                    imageVector = if (expandedCity)
                                        Icons.Default.KeyboardArrowUp
                                    else
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Arrow",
                                    tint = Color.White
                                )
                            }

                            ExposedDropdownMenu(
                                expanded = expandedCity,
                                onDismissRequest = { expandedCity = false },
                                modifier = Modifier.background(colorResource(id = R.color.purple))
                            ) {
                                citiesByCountry[selectedCountry]?.forEachIndexed { index, city ->
                                    AnimatedDropdownMenuItem(
                                        text = { Text(city, color = Color.White) },
                                        onClick = {
                                            selectedCity = city
                                            expandedCity = false
                                        },
                                        index = index
                                    )
                                }
                            }
                        }
                    }

                    // Tab Row for switching between views
                    Surface(
                        color = colorResource(id = R.color.purple),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color.White
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title) }
                                )
                            }
                        }
                    }

                    // Display content based on selected tab with animation
                    AnimatedTabContent(selectedTab = selectedTab) {
                        when (selectedTab) {
                            0 -> TodayWeatherContent(displayData, viewModel)
                            1 -> WeeklyForecastContent(viewModel.weeklyForecastState)
                            2 -> WeatherChartsContent(displayData, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedDropdownMenuItem(text: @Composable () -> Unit, onClick: () -> Unit, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically()
    ) {
        DropdownMenuItem(
            text = text,
            onClick = onClick,
            modifier = Modifier.animateEnterExit()
        )
    }
}

@Composable
fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun FutureModelViewHolder(model: HourlyModel, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically()
    ) {
        Column(
            modifier = Modifier
                .width(90.dp)
                .wrapContentHeight()
                .padding(4.dp)
                .background(
                    color = colorResource(id = R.color.purple),
                    shape = RoundedCornerShape(16.dp)
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
}

@Composable
fun AnimatedWeatherDetailItem(icon: Int, value: String, label: String, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        WeatherDetailItem(icon = icon, value = value, label = label)
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

@Composable
fun TodayWeatherContent(data: WeatherData, viewModel: WeatherViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Text(
                text = data.description,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center
            )

            val weatherIconId = when (data.weatherType) {
                "cloudy" -> R.drawable.cloudy
                "sunny" -> R.drawable.sunny
                "rainy" -> R.drawable.rain
                else -> R.drawable.cloudy_sunny
            }

            // Add pulsating animation to weather icon
            PulsatingIcon(iconId = weatherIconId)

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
                text = "${data.temperature}°C",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "H:${data.maxTemp}° L:${data.minTemp}°",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            // Additional details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .background(
                        color = colorResource(id = R.color.purple),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AnimatedWeatherDetailItem(
                        icon = R.drawable.humidity,
                        value = "${data.humidity}%",
                        label = "Humidity",
                        index = 0
                    )
                    AnimatedWeatherDetailItem(
                        icon = R.drawable.wind,
                        value = "${data.windSpeed} m/s",
                        label = "Wind",
                        index = 1
                    )
                    AnimatedWeatherDetailItem(
                        icon = R.drawable.rain,
                        value = "${String.format("%.1f", data.precipitation)} mm",
                        label = "Rain",
                        index = 2
                    )
                }
            }

            // Today's forecast by hours
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
                itemsIndexed(viewModel.forecastState) { index, item ->
                    FutureModelViewHolder(model = item, index = index)
                }
            }
        }
    }
}

@Composable
fun WeeklyForecastContent(weeklyForecast: List<DailyForecast>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "7-Day Forecast",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        itemsIndexed(weeklyForecast) { index, forecast ->
            AnimatedListItem(
                content = { DailyForecastItem(forecast = forecast) },
                index = index
            )
        }
    }
}

@Composable
fun DailyForecastItem(forecast: DailyForecast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.purple)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Day
            Text(
                text = forecast.date,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.width(100.dp)
            )

            // Weather icon and description
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(
                        id = when (forecast.weatherType) {
                            "cloudy" -> R.drawable.cloudy
                            "sunny" -> R.drawable.sunny
                            "wind" -> R.drawable.wind
                            "rainy" -> R.drawable.rain
                            "cloudy_sunny" -> R.drawable.cloudy
                            else -> R.drawable.sunny
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = forecast.condition,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // Temperatures
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = "${forecast.maxTemp}°",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${forecast.minTemp}°",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherChartsContent(data: WeatherData, viewModel: WeatherViewModel) {
    val hourlyData = viewModel.forecastState
    val weeklyData = viewModel.weeklyForecastState

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Weather Charts",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        item {
            AnimatedListItem(
                content = {
                    WeatherChartCard(title = "Today's Temperature") {
                        HourlyTemperatureChart(hourlyData)
                    }
                },
                index = 0
            )
        }

        item {
            AnimatedListItem(
                content = {
                    WeatherChartCard(title = "Weekly Temperature") {
                        WeeklyTemperatureChart(weeklyData)
                    }
                },
                index = 1
            )
        }

        item {
            AnimatedListItem(
                content = {
                    WeatherChartCard(title = "Humidity and Wind") {
                        MetricsChart(hourlyData)
                    }
                },
                index = 2
            )
        }

        item {
            AnimatedListItem(
                content = {
                    WeatherChartCard(title = "Precipitation") {
                        PrecipitationChart(hourlyData)
                    }
                },
                index = 3
            )
        }
    }
}

@Composable
fun WeatherChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.purple))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun HourlyTemperatureChart(hourlyData: List<HourlyModel>) {
    if (hourlyData.isEmpty()) {
        EmptyChartMessage()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            hourlyData.take(8).forEachIndexed { index, hourData ->
                AnimatedTemperatureBar(
                    temp = hourData.temp,
                    max = hourlyData.maxOf { it.temp } + 5,
                    label = hourData.hour,
                    index = index
                )
            }
        }
    }
}

@Composable
fun WeeklyTemperatureChart(weeklyData: List<DailyForecast>) {
    if (weeklyData.isEmpty()) {
        EmptyChartMessage()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Legend - with white text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Min", color = Color(0xFF8FB3FF), fontSize = 14.sp)
            Text("Max", color = Color(0xFFFF9E80), fontSize = 14.sp)
            Text("Temperature (°C)", color = Color.White, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create horizontal temperature chart like hourly chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyData.forEachIndexed { index, forecast ->
                val dayName = forecast.date.split(",").firstOrNull() ?: ""

                AnimatedTemperatureBarForWeekly(
                    maxTemp = forecast.maxTemp,
                    minTemp = forecast.minTemp,
                    dayName = dayName,
                    index = index
                )
            }
        }
    }
}

@Composable
fun AnimatedTemperatureBarForWeekly(
    maxTemp: Int,
    minTemp: Int,
    dayName: String,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it * 2 }),
        exit = fadeOut() + slideOutVertically()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Max temperature with value
            Text(
                text = "$maxTemp°",
                color = Color(0xFFFF9E80),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            // Temperature bar
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(Color(0x33FFFFFF))
                )

                // Gradient temperature bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(8.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF9E80), // Max temp color (warm)
                                    Color(0xFF8FB3FF)  // Min temp color (cool)
                                )
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }

            // Min temperature with value
            Text(
                text = "$minTemp°",
                color = Color(0xFF8FB3FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Day name
            Text(
                text = dayName,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MetricsChart(hourlyData: List<HourlyModel>) {
    if (hourlyData.isEmpty()) {
        EmptyChartMessage()
        return
    }

    Column(modifier = Modifier.padding(8.dp)) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF2196F3), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Humidity (%)", color = Color.White, fontSize = 12.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFFFF9800), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Wind (m/s x10)", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simplified chart
        AnimatedChart {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                hourlyData.take(8).forEachIndexed { index, data ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Humidity bar
                        val humidity = data.humidity ?: 0
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height((humidity.toFloat()).coerceAtLeast(0f).toInt().dp)
                                .background(Color(0xFF2196F3), RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        )

                        // Wind speed bar
                        val windSpeed = data.windSpeed ?: 0.0
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height((windSpeed.toFloat() * 10).coerceAtLeast(0f).toInt().dp)
                                .background(Color(0xFFFF9800), RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = data.hour,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrecipitationChart(hourlyData: List<HourlyModel>) {
    if (hourlyData.isEmpty()) {
        EmptyChartMessage()
        return
    }

    Column(modifier = Modifier.padding(8.dp)) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF4CAF50), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Precipitation (mm x10)", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simplified chart
        AnimatedChart {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                hourlyData.take(8).forEachIndexed { index, data ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Precipitation bar - safe access with default
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height((data.precipitationValue * 10).dp)
                                .background(
                                    Color(0xFF4CAF50),
                                    RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = data.hour,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedChart(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        content()
    }
}

@Composable
fun AnimatedTemperatureBar(
    temp: Int,
    max: Int,
    label: String,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = "$temp°",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Temperature bar
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height((temp.toFloat() / max.toFloat() * 100).dp)
                    .background(
                        Color(0xFF5E9EFF),
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun AnimatedListItem(
    content: @Composable () -> Unit,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        content()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedTabContent(selectedTab: Int, content: @Composable () -> Unit) {
    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
            fadeIn() with fadeOut()
        },
        content = { content() }
    )
}

@Composable
fun PulsatingIcon(iconId: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconPulsate")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Image(
        painter = painterResource(id = iconId),
        contentDescription = null,
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .padding(16.dp)
    )
}

@Composable
fun PulsatingLoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingPulsate")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadingScale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .background(Color(0x226200EE), CircleShape)
        )
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
fun AnimatedButton(text: String, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "buttonScale"
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier.scale(scale),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.purple))
    ) {
        Text(text, color = Color.White)
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun EmptyChartMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No data available",
            color = Color.White,
            fontSize = 16.sp
        )
    }
}