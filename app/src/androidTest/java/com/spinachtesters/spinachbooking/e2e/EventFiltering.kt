package com.spinachtesters.spinachbooking.e2e

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.spinachtesters.spinachbooking.ui.navigation.NavGraph

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventFiltering {

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
    fun invalidFilter_showsError() {
        composeRule.onNodeWithTag("login_toggle")
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("login_identifier_input")
            .performTextInput("willUsr")

        composeRule.onNodeWithTag("login_password_input")
            .performTextInput("Password123!")

        composeRule.onNodeWithTag("login_submit_button")
            .performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil {
            composeRule
                .onAllNodesWithTag("home_screen_scaffold")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("home_screen_scaffold")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("search_bar")
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("search_form_lazy_column")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("create_minPrice_input")
            .performTextInput("100")

        composeRule.onNodeWithTag("create_maxPrice_input")
            .performTextInput("10")


        composeRule.onNodeWithTag("search_button")
            .performClick()

        composeRule.onNodeWithTag("filter_error_message")
            .assertIsDisplayed()
    }

    @Test
    fun enterConcordia_asLocation_thenApplySearch_showsAppropriateEventCard() {
        composeRule.onNodeWithTag("login_toggle")
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("login_identifier_input")
            .performTextInput("willUsr")

        composeRule.onNodeWithTag("login_password_input")
            .performTextInput("Password123!")

        composeRule.onNodeWithTag("login_submit_button")
            .performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil {
            composeRule
                .onAllNodesWithTag("home_screen_scaffold")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("home_screen_scaffold")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("search_bar")
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("search_form_lazy_column")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("create_location_input")
            .performTextInput("concordia")

        composeRule.onNodeWithTag("search_button")
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Concordia")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Gnomeo and Juliet")
            .assertIsDisplayed()
    }
}