package com.spinachtesters.spinachbooking.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.ui.screens.HomeScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            HomeScreen(
                loadOnStart = false,
                navController = navController
            )
        }
    }

    @Test
    fun homeScreen_showsCoreComponents() {
        composeRule.onNodeWithText("Search and filter events by date, location, and category:")
            .assertExists()
        composeRule.onNodeWithText("Search for an event...")
            .assertExists()
        composeRule.onNodeWithText("Available events:")
            .assertExists()
        composeRule.onNodeWithText("Booked events:")
            .assertExists()
        composeRule.onNodeWithText("Drake Concert")
            .assertExists()
    }
}


