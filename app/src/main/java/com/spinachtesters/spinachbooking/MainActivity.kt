package com.spinachtesters.spinachbooking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.spinachtesters.spinachbooking.ui.components.TitleTopBar
import com.spinachtesters.spinachbooking.ui.screens.HomeScreen
import com.spinachtesters.spinachbooking.ui.screens.LoginScreen
import com.spinachtesters.spinachbooking.ui.screens.SignUpScreen
import com.spinachtesters.spinachbooking.ui.theme.SpinachBookingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpinachBookingTheme {
                //LoginScreen()
                //SignUpScreen() // don't have time to figure out routing right now
                HomeScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SpinachBookingTheme {
        Greeting("Android")
    }
}