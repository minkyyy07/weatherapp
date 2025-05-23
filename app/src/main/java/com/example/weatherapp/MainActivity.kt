package com.example.weatherapp

// Add these imports at the top of your file
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.PaddingValues
import kotlin.math.absoluteValue
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.div
import kotlin.text.get
import kotlin.text.toFloat
import kotlin.times

// Add this class definition
data class ThemeOption(
    val name: String,
    val backgroundBrush: Brush,
    val primaryColor: Color
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Check if it's the first launch
            val context = LocalContext.current
            val preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isFirstLaunch = preferences.getBoolean("is_first_launch", true)
            
            var showWelcomeScreen by remember { mutableStateOf(isFirstLaunch) }
            
            if (showWelcomeScreen) {
                WelcomeScreen(
                    onGetStarted = {
                        showWelcomeScreen = false
                        // Save that app has been launched
                        preferences.edit().putBoolean("is_first_launch", false).apply()
                    }
                )
            } else {
                WeatherScreen()
            }
        }
    }
}

// Helper function to get current theme
@Composable
fun getCurrentTheme(): ThemeOption {
    val context = LocalContext.current
    val preferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    val currentThemeIndex = preferences.getInt("current_theme", 0)

    val themeOptions = listOf(
        ThemeOption(
            "Purple",
            Brush.horizontalGradient(
                colors = listOf(
                    Color(android.graphics.Color.parseColor("#59469d")),
                    Color(android.graphics.Color.parseColor("#643d67")),
                )
            ),
            primaryColor = Color(android.graphics.Color.parseColor("#8a72d6"))
        ),
        ThemeOption(
            "Ocean",
            Brush.horizontalGradient(
                colors = listOf(
                    Color(android.graphics.Color.parseColor("#1a2980")),
                    Color(android.graphics.Color.parseColor("#26d0ce"))
                )
            ),
            primaryColor = Color(android.graphics.Color.parseColor("#3498db"))
        ),
        ThemeOption(
            "Sunset",
            Brush.horizontalGradient(
                colors = listOf(
                    Color(android.graphics.Color.parseColor("#FF7E5F")),
                    Color(android.graphics.Color.parseColor("#FF6F20"))
                )
            ),
            primaryColor = Color(android.graphics.Color.parseColor("#e74c3c"))
        )
    )

    return themeOptions[currentThemeIndex]
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    // Force the entire composable to rebuild when themes change
    val context = LocalContext.current
    val preferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    // Read from shared preferences every time this composable is called
    val currentThemeIndex = preferences.getInt("current_theme", 0)
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeOptions = listOf(
        ThemeOption(
            "Purple",
            Brush.horizontalGradient(
                colors = listOf(
                    Color(android.graphics.Color.parseColor("#59469d")),
                    Color(android.graphics.Color.parseColor("#643d67")),
                )
            ),
            primaryColor = Color(android.graphics.Color.parseColor("#8a72d6"))
        ),
        ThemeOption(
            "Ocean",
            Brush.horizontalGradient(
                colors = listOf(
                    Color(android.graphics.Color.parseColor("#1a2980")),
                    Color(android.graphics.Color.parseColor("#26d0ce"))
                )
            ),
            primaryColor = Color(android.graphics.Color.parseColor("#3498db"))
        ),
        ThemeOption(
            "Sunset",
            Brush.horizontalGradient(
                colors = listOf(
                    Color(android.graphics.Color.parseColor("#FF7E5F")),
                    Color(android.graphics.Color.parseColor("#FF6F20"))
                )
            ),
            primaryColor = Color(android.graphics.Color.parseColor("#e74c3c"))
        )
    )

    val currentTheme = themeOptions[currentThemeIndex]

    // Country and city selection
    val countries = listOf("United Kingdom", "USA", "Japan", "France", "Russia", "Germany", "Australia", "Ukraine")

    // Read saved country and city from SharedPreferences
    val savedCountry = preferences.getString("selected_country", "United Kingdom") ?: "United Kingdom"
    val savedCity = preferences.getString("selected_city", "London") ?: "London"

    var selectedCountry by remember { mutableStateOf(savedCountry) }
    var selectedCity by remember { mutableStateOf(savedCity) }
    var expandedCountry by remember { mutableStateOf(false) }
    var expandedCity by remember { mutableStateOf(false) }

    // City mapping
    val citiesByCountry = mapOf(
        "United Kingdom" to listOf("London", "Manchester", "Liverpool", "Birmingham", "Edinburgh", "Glasgow"),
        "USA" to listOf("New York", "Los Angeles", "Chicago", "Miami", "San Francisco", "Boston", "Seattle", "Denver"),
        "Japan" to listOf("Tokyo", "Osaka", "Kyoto", "Sapporo", "Yokohama", "Nagoya"),
        "France" to listOf("Paris", "Marseille", "Lyon", "Nice", "Bordeaux", "Toulouse", "Strasbourg"),
        "Russia" to listOf("Moscow", "Saint Petersburg", "Novosibirsk", "Kazan", "Sochi", "Vladivostok"),
        "Germany" to listOf("Berlin", "Munich", "Hamburg", "Cologne", "Nüremberg", "Frankfurt", "Dresden", "Stuttgart"),
        "Australia" to listOf("Sydney", "Melbourne", "Perth", "Brisbane", "Adelaide", "Canberra", "Gold Coast"),
        "Ukraine" to listOf("Kyiv", "Lviv", "Odesa", "Irpin", "Kharkiv", "Dnipro", "Mariupol", "Zaporizhzhia")
    )

    // Tab selection
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Today", "Weekly", "Charts")
    val pagerState = rememberPagerState()

    // Effect to update the city when country changes
    LaunchedEffect(selectedCountry) {
        val cities = citiesByCountry[selectedCountry] ?: emptyList()
        if (cities.isNotEmpty() && !cities.contains(selectedCity)) {
            selectedCity = cities.first()

            // Save the selected country and city to SharedPreferences
            preferences.edit()
                .putString("selected_country", selectedCountry)
                .putString("selected_city", selectedCity)
                .apply()

            viewModel.getWeatherData(selectedCity)
        }
    }

    // Effect to sync tab selection with pager state
    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    // Effect to load weather data when the city changes
    LaunchedEffect(selectedCity) {
        // Save selected city to SharedPreferences
        preferences.edit()
            .putString("selected_country", selectedCountry)
            .putString("selected_city", selectedCity)
            .apply()

        viewModel.getWeatherData(selectedCity)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.backgroundBrush),
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Failed to load weather data: ${state.message}",
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
                val displayData = if (selectedCity == "Nüremberg" && data.cityName.equals("Nuremberg", ignoreCase = true)) {
                    data.copy(cityName = "Nüremberg")
                } else data

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Theme switcher and country selector row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Theme button
                        IconButton(
                            onClick = { showThemeDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    currentTheme.primaryColor.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.cloudy),
                                contentDescription = "Theme",
                                tint = Color.White
                            )
                        }

                        // Country dropdown
                        Box {
                            Button(
                                onClick = { expandedCountry = !expandedCountry },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = currentTheme.primaryColor.copy(alpha = 0.2f)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text(selectedCountry, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (expandedCountry)
                                        Icons.Default.KeyboardArrowUp
                                    else
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = expandedCountry,
                                onDismissRequest = { expandedCountry = false },
                                containerColor = currentTheme.primaryColor.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                countries.forEachIndexed { index, country ->
                                    AnimatedDropdownMenuItem(
                                        text = { Text(country, color = Color.White) },
                                        onClick = {
                                            selectedCountry = country
                                            expandedCountry = false

                                            // Save selection immediately
                                            preferences.edit()
                                                .putString("selected_country", selectedCountry)
                                                .apply()
                                        },
                                        index = index,
                                        isSelected = country == selectedCountry,
                                        currentTheme = currentTheme
                                    )
                                }
                            }
                        }
                    }

                    // City selector
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Button(
                            onClick = { expandedCity = !expandedCity },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTheme.primaryColor.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Location",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(selectedCity, color = Color.White)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (expandedCity)
                                    Icons.Default.KeyboardArrowUp
                                else
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = expandedCity,
                            onDismissRequest = { expandedCity = false },
                            containerColor = currentTheme.primaryColor.copy(alpha = 0.9f),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            val cities = citiesByCountry[selectedCountry] ?: emptyList()
                            cities.forEachIndexed { index, city ->
                                AnimatedDropdownMenuItem(
                                    text = { Text(city, color = Color.White) },
                                    onClick = {
                                        selectedCity = city
                                        expandedCity = false

                                        // Save selection immediately
                                        preferences.edit()
                                            .putString("selected_city", selectedCity)
                                            .apply()
                                    },
                                    index = index,
                                    isSelected = city == selectedCity,
                                    currentTheme = currentTheme
                                )
                            }
                        }
                    }

                    // Location and date info
                    Text(
                        text = "${displayData.cityName}, ${selectedCountry}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        indicator = @Composable { tabPositions ->
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                    .height(4.dp)
                                    .padding(horizontal = 32.dp)
                                    .background(
                                        color = currentTheme.primaryColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        },
                        divider = { Spacer(modifier = Modifier.height(4.dp)) }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        color = Color.White,
                                        fontWeight = if (selectedTabIndex == index)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    // Tab content with pager
                    HorizontalPager(
                        count = tabs.size,
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        AnimatedTabContent(selectedTab = page) {
                            when (page) {
                                0 -> TodayWeatherContent(data = displayData, viewModel = viewModel)
                                1 -> WeeklyForecastContent(weeklyForecast = viewModel.weeklyForecastState)
                                2 -> WeatherChartsContent(data = displayData, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }

        // Theme selection dialog
        if (showThemeDialog) {
            ThemeSelectionDialog(
                themes = themeOptions,
                currentThemeIndex = currentThemeIndex,
                onThemeSelected = { index ->
                    // Save the theme preference in SharedPreferences
                    preferences.edit()
                        .putInt("current_theme", index)
                        .apply()

                    // Force activity recreation to apply theme instantly
                    (context as? ComponentActivity)?.recreate()
                },
                onDismiss = { showThemeDialog = false }
            )
        }
    }
}

@Composable
fun TodayWeatherContent(data: WeatherData, viewModel: WeatherViewModel) {
    val currentDate = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date()) }
    val currentTime = getCurrentTime()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current location and date
        Text(
            text = data.cityName,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        // Current weather icon and temperature
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Weather icon
                Image(
                    painter = painterResource(
                        id = when (data.weatherType) {
                            "sunny" -> R.drawable.sunny
                            "cloudy" -> R.drawable.cloudy
                            "rainy" -> R.drawable.rain
                            "cloudy_sunny" -> R.drawable.cloudy_sunny
                            else -> R.drawable.cloudy_sunny
                        }
                    ),
                    contentDescription = "Weather",
                    modifier = Modifier.size(120.dp)
                )
                
                // Temperature
                Text(
                    text = "${data.temperature}°",
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Weather description
                Text(
                    text = "Precipitations",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp
                )
                
                // Min/Max temperature
                Text(
                    text = "Max: ${data.maxTemp}°   Min:${data.minTemp}°",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // House image
        Image(
            painter = painterResource(id = R.drawable.cloudy_sunny),
            contentDescription = "House",
            modifier = Modifier
                .size(120.dp)
                .padding(vertical = 16.dp)
        )
        
        // Today's date
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Today",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date()),
                color = Color.White,
                fontSize = 18.sp
            )
        }
        
        // Hourly forecast
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val hourlyForecasts = viewModel.forecastState
            
            itemsIndexed(hourlyForecasts) { index, hourlyModel ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(
                            Color(0xFF8B5CF6).copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${hourlyModel.temp}°C",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Image(
                        painter = painterResource(
                            id = when (hourlyModel.picPath) {
                                "sunny" -> R.drawable.sunny
                                "cloudy" -> R.drawable.cloudy
                                "rainy" -> R.drawable.rain
                                "cloudy_sunny" -> R.drawable.cloudy_sunny
                                else -> R.drawable.cloudy_sunny
                            }
                        ),
                        contentDescription = "Weather",
                        modifier = Modifier.size(40.dp)
                    )
                    
                    Text(
                        text = hourlyModel.hour,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyForecastContent(weeklyForecast: List<DailyForecast>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Region name and temperature range
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "North America",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Max: 24°   Min:18°",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        
        // 7-Days Forecasts title
        Text(
            text = "7-Days Forecasts",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // Weekly forecast cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(weeklyForecast) { index, forecast ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(100.dp)
                        .background(
                            Color(0xFF8B5CF6).copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = "${forecast.maxTemp}°C",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Image(
                        painter = painterResource(id = forecast.icon),
                        contentDescription = forecast.condition,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(vertical = 8.dp)
                    )
                    
                    Text(
                        text = forecast.date.split(",").firstOrNull() ?: "",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // Air quality section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Air Quality",
                        tint = Color.White
                    )
                    
                    Text(
                        text = "AIR QUALITY",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Text(
                    text = "3-Low Health Risk",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "See more",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "See more",
                        tint = Color.White
                    )
                }
            }
        }
        
        // Sun info row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sunrise card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SUNRISE",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = "5:28 AM",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Sunset: 7:25PM",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
            
            // UV Index card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "UV INDEX",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = "4",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Moderate",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    themes: List<ThemeOption>,
    currentThemeIndex: Int,
    onThemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        containerColor = Color.DarkGray,
        titleContentColor = Color.White,
        text = {
            LazyColumn {
                itemsIndexed(themes) { index, theme ->
                    ThemeOptionItem(
                        theme = theme,
                        isSelected = index == currentThemeIndex,
                        onClick = {
                            // Save theme index
                            context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .putInt("current_theme", index)
                                .apply()

                            // Notify the caller with the new index
                            onThemeSelected(index)

                            // Add small delay before dismissing dialog
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(200)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun ThemeOptionItem(
    theme: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Theme preview
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(theme.backgroundBrush, RoundedCornerShape(8.dp))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = theme.name,
            color = Color.White,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Selected",
                tint = Color.Green
            )
        }
    }
}

// Linear interpolation function to smoothly transition values
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Composable
fun AnimatedDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    index: Int,
    isSelected: Boolean = false,
    currentTheme: ThemeOption = getCurrentTheme()
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = if (isSelected)
                        currentTheme.primaryColor.copy(alpha = 0.4f)
                    else
                        currentTheme.primaryColor.copy(alpha = 0.15f)
                )
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    text()
                }

                if (isSelected) {
                    Icon(
                        painter = painterResource(id = R.drawable.sunny),
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun FutureModelViewHolder(model: HourlyModel, index: Int) {
    val currentTheme = getCurrentTheme()
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
                    color = currentTheme.primaryColor.copy(alpha = 0.2f),
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
fun DailyForecastItem(forecast: DailyForecast) {
    val currentTheme = getCurrentTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = currentTheme.primaryColor.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Use the correct property names from your DailyForecast class
            Text(
                text = forecast.date, // Использование нового имени свойства
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.width(100.dp)
            )

            // Weather icon
            Image(
                painter = painterResource(id = forecast.icon), // Использование нового имени свойства
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )

            // Use the correct temperature property names
            Text(
                text = "${forecast.minTemp}° / ${forecast.maxTemp}°",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun WeatherChartsContent(data: WeatherData, viewModel: WeatherViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Weather Charts",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Hourly temperature chart
        item {
            AnimatedListItem(
                content = {
                    WeatherChartCard(title = "Today's Hourly Temperature") {
                        HourlyTemperatureChart(viewModel.forecastState)
                    }
                },
                index = 0
            )
        }

        // Weekly temperature chart
        item {
            AnimatedListItem(
                content = {
                    WeatherChartCard(title = "Weekly Temperature") {
                        WeeklyTemperatureChart(viewModel.weeklyForecastState)
                    }
                },
                index = 1
            )
        }

        // Humidity and wind chart
        item {
            AnimatedListItem(
                content = {
                    WeatherChartCard(title = "Humidity and Wind") {
                        // Simple display of humidity and wind data
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Humidity", color = Color.White)
                                Text("${data.humidity}%", color = Color.White, fontSize = 20.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Wind Speed", color = Color.White)
                                Text("${data.windSpeed} m/s", color = Color.White, fontSize = 20.sp)
                            }
                        }
                    }
                },
                index = 2
            )
        }
    }
}

@Composable
fun AnimatedListItem(content: @Composable () -> Unit, index: Int) {
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
        content()
    }
}

@Composable
fun WeatherChartCard(title: String, content: @Composable () -> Unit) {
    val currentTheme = getCurrentTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = currentTheme.primaryColor.copy(alpha = 0.2f)
        )
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
fun HourlyTemperatureChart(hourlyForecast: List<HourlyModel>) {
    if (hourlyForecast.isEmpty()) {
        Text("No hourly data available", color = Color.White)
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        val maxTemp = hourlyForecast.maxOfOrNull { it.temp }?.toInt() ?: 0

        hourlyForecast.forEachIndexed { index, item ->
            AnimatedTemperatureBar(
                temp = item.temp.toInt(),
                max = maxTemp,
                label = item.hour,
                index = index
            )
        }
    }
}

@Composable
fun WeeklyTemperatureChart(weeklyForecast: List<DailyForecast>) {
    if (weeklyForecast.isEmpty()) {
        Text("No weekly data available", color = Color.White)
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        weeklyForecast.forEachIndexed { index, forecast ->
            val dayName = forecast.date.split(",").firstOrNull() ?: "" // Использование нового имени свойства 'date' // <-- Здесь ошибка

            AnimatedTemperatureBarForWeekly(
                maxTemp = forecast.maxTemp,
                minTemp = forecast.minTemp,
                dayName = dayName,
                index = index
            )
        }
    }
}

@Composable
fun AnimatedTemperatureBar(
    temp: Int,
    max: Int,
    label: String,
    index: Int
) {
    val currentTheme = getCurrentTheme()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(30L * index) // Faster staggering
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Temperature value
            Text(
                text = "$temp°",
                fontSize = 12.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Temperature bar with relative height
            val height = if (max > 0) (temp.toFloat() / max * 100).coerceIn(10f, 100f) else 10f
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(height.dp)
                    .background(currentTheme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Label
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White
            )
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
    val currentTheme = getCurrentTheme()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Text(
                text = dayName,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .height(100.dp)
                    .width(20.dp)
            ) {
                // Min temperature bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height((minTemp * 2).dp)
                        .background(
                            currentTheme.primaryColor.copy(alpha = 0.4f),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )

                // Max temperature bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height((maxTemp * 2).dp)
                        .background(
                            currentTheme.primaryColor,
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${maxTemp}°",
                color = Color.White,
                fontSize = 14.sp
            )

            Text(
                text = "${minTemp}°",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedTabContent(selectedTab: Int, content: @Composable () -> Unit) {
    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "tabAnimation"
    ) {
        content()
    }
}

@Composable
fun PulsatingLoadingAnimation() {
    val currentTheme = getCurrentTheme()

    val infiniteTransition = rememberInfiniteTransition(label = "loadingPulsate")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
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
                .background(currentTheme.primaryColor.copy(alpha = 0.3f), CircleShape)
        )
        CircularProgressIndicator(
            color = currentTheme.primaryColor,
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
fun PulsatingIcon(iconId: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconPulsate")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
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
            .size(150.dp)
            .scale(scale),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun AnimatedButton(text: String, onClick: () -> Unit) {
    val currentTheme = getCurrentTheme()

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
        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor)
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
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val purpleGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF8B5CF6), // Light purple
            Color(0xFF4C1D95)  // Dark purple
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(purpleGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Weather icon
            Image(
                painter = painterResource(id = R.drawable.cloudy_sunny),
                contentDescription = "Weather Icon",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp)
            )
            
            // Title with two colors
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Weather",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "ForeCasts",
                    color = Color(0xFFFCD34D), // Yellow color
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Get Started button
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFCD34D) // Yellow color
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Get Start",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C1D95) // Dark purple
                )
            }
        }
    }
}