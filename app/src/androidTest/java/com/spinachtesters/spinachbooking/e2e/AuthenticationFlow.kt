package com.spinachtesters.spinachbooking.e2e

import com.spinachtesters.spinachbooking.ui.navigation.NavGraph

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.sign

@RunWith(AndroidJUnit4::class)
class AuthenticationFlow {

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
    fun appStart_signup_toLogin_invalidLogin_showsError() {
        val signUpNodes = composeRule.onAllNodesWithText("Sign up")
        signUpNodes.assertCountEquals(2)
        var nodes = signUpNodes.fetchSemanticsNodes() // needed to iterate in the loop
        for (i in nodes.indices) {
            signUpNodes[i].assertIsDisplayed()
        }

        composeRule.onNodeWithTag("signup_submit_button").assertIsDisplayed() //should be covered by the previous assertion but the more, the merrier

        composeRule.onNodeWithTag("login_toggle").performClick()
        composeRule.waitForIdle()

        val loginNodes = composeRule.onAllNodesWithText("Login")
        loginNodes.assertCountEquals(2)
        nodes = loginNodes.fetchSemanticsNodes()
        for (i in nodes.indices) {
            loginNodes[i].assertIsDisplayed()
        }

        composeRule.onNodeWithTag("login_submit_button").assertIsDisplayed() //should be covered by the previous assertion but the more, the merrier

        composeRule.onNodeWithTag("login_identifier_input")
            .performTextInput("janedoe")

        composeRule.onNodeWithTag("login_submit_button").performClick()

        composeRule.onNodeWithText(
            "Please provide your username/email/phone and password."
        ).assertIsDisplayed()
    }
}