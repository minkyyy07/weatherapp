package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.legend.legendItem
import com.patrykandpatrick.vico.compose.legend.verticalLegend
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.marker.Marker
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
    val countries = listOf("United Kingdom", "USA", "Japan", "France", "Russia", "Germany", "Australia", "Ukraine")
    var selectedCountry by remember { mutableStateOf("United Kingdom") }
    var selectedCity by remember { mutableStateOf("London") }
    var expandedCountry by remember { mutableStateOf(false) }
    var expandedCity by remember { mutableStateOf(false) }
    val favoritesManager = remember { FavoritesManager() }

    val settingsExpanded by remember { mutableStateOf(false) }

    @Composable
    fun AnimatedList(items: List<String>) {
        LazyColumn {
            items(items) { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Text(item, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }

    @Composable
    fun AnimatedExample() {
        var isVisible by remember { mutableStateOf(true) }

        Column {
            Button(onClick = { isVisible = !isVisible }) {
                Text("Toggle Visibility")
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text("Hello, Animation!", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    Button(
        onClick = { favoritesManager.addCity(selectedCity) },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Text("Add to Favorites", color = Color.Black)
    }

    // Comment out language selection functionality
    /*
    // Language selection
    val languages = listOf("English", "Русский", "Українська")
    var selectedLanguage by remember { mutableStateOf("English") }
    var expandedLanguage by remember { mutableStateOf(false) }

    // Context for changing locale
    val context = LocalContext.current

    // Update locale when language changes
    LaunchedEffect(selectedLanguage) {
        val locale = when(selectedLanguage) {
            "English" -> Locale("en")
            "Русский" -> Locale("ru")
            "Українська" -> Locale("uk")
            else -> Locale.getDefault()
        }
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
    }
    */

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
                        text = "Error: " + state.message,
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Comment out language selector
                    /*
                    // Language selector
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedLanguage,
                            onExpandedChange = { expandedLanguage = !expandedLanguage }
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
                                        imageVector = Icons.Default.Settings, // Instead of Language
                                        contentDescription = "Language",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedLanguage,
                                        color = Color.White
                                    )
                                }
                                Icon(
                                    imageVector = if (expandedLanguage)
                                        Icons.Default.KeyboardArrowUp
                                    else
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Arrow",
                                    tint = Color.White
                                )
                            }

                            ExposedDropdownMenu(
                                expanded = expandedLanguage,
                                onDismissRequest = { expandedLanguage = false },
                                modifier = Modifier.background(colorResource(id = R.color.purple))
                            ) {
                                languages.forEach { language ->
                                    DropdownMenuItem(
                                        text = { Text(language, color = Color.White) },
                                        onClick = {
                                            selectedLanguage = language
                                            expandedLanguage = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    */

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
                                        imageVector = Icons.Default.Info, // Instead of Public
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
                                countries.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text(country, color = Color.White) },
                                        onClick = {
                                            selectedCountry = country
                                            selectedCity = citiesByCountry[country]?.first() ?: ""
                                            expandedCountry = false
                                        }
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
                                        imageVector = Icons.Default.Place, // Instead of LocationCity
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
                                citiesByCountry[selectedCountry]?.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city, color = Color.White) },
                                        onClick = {
                                            selectedCity = city
                                            expandedCity = false
                                        }
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

                    // Display content based on selected tab
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
                shape = RoundedCornerShape(16.dp) // Changed from 8.dp
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

            Image(
                painter = painterResource(id = weatherIconId),
                contentDescription = null,
                modifier = Modifier
                    .size(130.dp)
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
                    WeatherDetailItem(
                        icon = R.drawable.humidity,
                        value = "${data.humidity}%",
                        label = "Humidity"
                    )
                    WeatherDetailItem(
                        icon = R.drawable.wind,
                        value = "${data.windSpeed} m/s",
                        label = "Wind"
                    )
                    WeatherDetailItem(
                        icon = R.drawable.rain,
                        value = "${String.format("%.1f", data.precipitation)} mm",
                        label = "Rain"
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
                items(viewModel.forecastState) { item ->
                    FutureModelViewHolder(model = item)
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

        items(weeklyForecast) { forecast ->
            DailyForecastItem(forecast = forecast)
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
            WeatherChartCard(title = "Today's Temperature") {
                HourlyTemperatureChart(hourlyData)
            }
        }

        item {
            WeatherChartCard(title = "Weekly Temperature") {
                WeeklyTemperatureChart(weeklyData)
            }
        }

        item {
            WeatherChartCard(title = "Humidity and Wind") {
                MetricsChart(hourlyData)
            }
        }

        item {
            WeatherChartCard(title = "Precipitation") {
                PrecipitationChart(hourlyData)
            }
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
            hourlyData.take(8).forEach { hourData ->
                TemperatureBar(
                    temp = hourData.temp,
                    max = hourlyData.maxOf { it.temp } + 5,
                    label = hourData.hour
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
            .height(200.dp)
            .padding(4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFF06292)))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Max", color = Color.White, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF64B5F6)))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Min", color = Color.White, fontSize = 10.sp)
            }
        }

        // Find min/max values
        val maxTemp = weeklyData.maxOf { it.maxTemp }
        val minTemp = weeklyData.minOf { it.minTemp }

        // Use a minimum range of 10°C to prevent excessive scaling
        val minRange = 10
        val actualRange = (maxTemp - minTemp).coerceAtLeast(1)
        val range = maxOf(actualRange, minRange)

        // Create visual midpoint for better appearance
        val visualMinTemp = minTemp - ((range - actualRange) / 2)

        // Available height for bars
        val maxBarHeight = 100.dp

        // Chart container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.take(7).forEach { forecast ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "${forecast.maxTemp}°",
                            color = Color.White,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )

                        // Max temp bar with less sensitive scaling
                        val maxBarHeightPercentage = ((forecast.maxTemp - visualMinTemp).toFloat() / range)
                            .coerceIn(0.05f, 1f) // Minimum size to ensure visibility

                        Box(
                            modifier = Modifier
                                .width(9.dp)
                                .height(maxBarHeight * maxBarHeightPercentage)
                                .background(
                                    Color(0xFFF06292),
                                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Min temp bar with less sensitive scaling
                        val minBarHeightPercentage = ((forecast.minTemp - visualMinTemp).toFloat() / range)
                            .coerceIn(0.05f, 1f) // Minimum size to ensure visibility

                        Box(
                            modifier = Modifier
                                .width(9.dp)
                                .height(maxBarHeight * minBarHeightPercentage)
                                .background(
                                    Color(0xFF64B5F6),
                                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                )
                        )

                        Text(
                            text = "${forecast.minTemp}°",
                            color = Color.White,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )

                        Text(
                            text = forecast.date.take(3),
                            color = Color.White,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsChart(hourlyData: List<HourlyModel>) {
    if (hourlyData.isEmpty()) {
        EmptyChartMessage()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Wind & Humidity Chart",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            "Still in development | discord - .paveldurov.",
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PrecipitationChart(hourlyData: List<HourlyModel>) {
    if (hourlyData.isEmpty()) {
        EmptyChartMessage()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Precipitation Chart",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            "Still in development | discord - .paveldurov.",
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyChartMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No data to display",
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun TemperatureBar(temp: Int, max: Int, label: String) {
    val heightPercent = if (max > 0) (temp.toFloat() / max) else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = "$temp°",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .width(24.dp)
                .height(120.dp * heightPercent)
                .background(
                    Color(0xFF64B5F6),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )

        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun TemperatureRange(min: Int, max: Int, label: String) {
    val maxTemp = 40 // Assuming max possible temperature
    val minTemp = -10 // Assuming min possible temperature
    val range = maxTemp - minTemp

    val maxHeightPercent = (max - minTemp).toFloat() / range
    val minHeightPercent = (min - minTemp).toFloat() / range

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = "$max°",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .width(24.dp)
                .height(120.dp * (maxHeightPercent - minHeightPercent))
                .background(
                    Color(0xFFF06292),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )

        Text(
            text = "$min°",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}