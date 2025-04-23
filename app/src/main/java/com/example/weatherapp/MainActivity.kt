package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ContentView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

        }
    }
}

@Preview
@Composable
fun WeatherScreen() {
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
            ))
            {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally) {

                        item{
                            Text(text = "Mostly Cloudy",
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 48.dp),
                                textAlign = TextAlign.Center
                           )

                            Image(painter = painterResource(id = R.drawable.cloudy_sunny)
                            ,contentDescription = null,
                                modifier = Modifier
                                    .size(150.dp)
                                    .padding(top = 8.dp)
                            )

                            // Dispay date and time
                            Text(text = "Today, 12.00 PM",
                                fontSize = 19.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                textAlign = TextAlign.Center
                           )

                            // Display temperature
                            Text(text = "25c"
                                , fontSize = 63.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(top = 8.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(text = "H:27 L:18",
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                textAlign = TextAlign.Center
                           )

                            //Weather Box

                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .background(
                                    color = colorResource(id = R.color.purple),
                                    shape = RoundedCornerShape(25.dp)
                                )
                            ){
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(horizontal = 8.dp),
                                    verticalAlignment =  Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    WeatherDetailItem(
                                        icon = R.drawable.rain,
                                        value = "22%",
                                        label = "Rain"
                                    )
                                    WeatherDetailItem(
                                        icon = R.drawable.wind,
                                        value = "26%",
                                        label = "Wind Speed"
                                    )
                                    WeatherDetailItem(
                                        icon = R.drawable.humidity,
                                        value = "18%",
                                        label = "Humidity"
                                    )
                                }
                            }

                            // Display weather details

                        }
                    }
       }
    }
}

@Composable
fun WeatherDetailItem(icon:Int, value:String, label:String) {
    Column(modifier = Modifier.padding(16.dp)
        ,horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painter = painterResource(id = icon)
            , contentDescription = null,
            modifier = Modifier.size(34.dp)
        )
        Text(text = value,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Center
       )
        Text(text = label,
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Center
        )
    }
}