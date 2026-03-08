package com.spinachtesters.spinachbooking.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

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
    fun login_withBlankFields_showsCredentialsError() {
        composeRule.onNodeWithText("Have an account? Login").performClick()

        clickLoginButton()

        composeRule.onNodeWithText("Please provide your username/email/phone and password.").assertIsDisplayed()
    }

    @Test
    fun login_withOnlyIdentifier_showsCredentialsError() {
        composeRule.onNodeWithText("Have an account? Login").performClick()
        composeRule.onNodeWithTag("login_identifier_input").performTextInput("janedoe")

        clickLoginButton()

        composeRule.onNodeWithText("Please provide your username/email/phone and password.").assertIsDisplayed()
    }

    @Test
    fun login_withOnlyPassword_showsCredentialsError() {
        composeRule.onNodeWithText("Have an account? Login").performClick()
        composeRule.onNodeWithTag("login_password_input").performTextInput("StrongPass123!")

        clickLoginButton()

        composeRule.onNodeWithText("Please provide your username/email/phone and password.").assertIsDisplayed()
    }

    private fun clickLoginButton() {
        composeRule.onNodeWithTag("login_submit_button").performClick()
    }
}
