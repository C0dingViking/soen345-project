package com.spinachtesters.spinachbooking.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.ui.screens.AddEventScreen
import com.spinachtesters.spinachbooking.ui.screens.EventDetailScreen
import com.spinachtesters.spinachbooking.ui.screens.HomeScreen
import com.spinachtesters.spinachbooking.ui.screens.LoginScreen
import com.spinachtesters.spinachbooking.ui.screens.ManageEventsScreen
import com.spinachtesters.spinachbooking.ui.screens.SignUpScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object ManageEvents : Screen("manage_events")
    object AddEvent : Screen("add_event")

    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.SignUp.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.ManageEvents.route) {
            ManageEventsScreen(navController)
        }
        composable(Screen.AddEvent.route) {
            AddEventScreen(navController)
        }
        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EventDetailScreen(eventId, navController)
        }
    }
}
