package com.spinachtesters.spinachbooking.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class AuthNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavGraph(navController = navController)
        }
    }

    @Test
    fun clickingLoginTextOnSignUp_navigatesToLogin() {
        composeRule.onNodeWithText("Have an account? Login").performClick()

        composeRule.runOnIdle {
            assertEquals(Screen.Login.route, navController.currentBackStackEntry?.destination?.route)
        }
    }

    @Test
    fun clickingRegisterTextOnLogin_navigatesToSignUp() {
        composeRule.onNodeWithText("Have an account? Login").performClick()
        composeRule.onNodeWithText("No Account? Register").performClick()

        composeRule.runOnIdle {
            assertEquals(Screen.SignUp.route, navController.currentBackStackEntry?.destination?.route)
        }
    }
}

