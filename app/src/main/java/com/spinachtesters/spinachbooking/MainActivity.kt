package com.spinachtesters.spinachbooking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.spinachtesters.spinachbooking.ui.navigation.NavGraph
import com.spinachtesters.spinachbooking.ui.theme.SpinachBookingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpinachBookingTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}