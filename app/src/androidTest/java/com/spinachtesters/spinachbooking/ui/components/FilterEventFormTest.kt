package com.spinachtesters.spinachbooking.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.ui.viewmodels.FilterEventViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterEventFormTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selectingTheaterShowsTheaterFields() {
        val viewModel = renderForm()

        openEventTypeMenu()
        composeRule.onNodeWithTag("theater_eventType_input").performClick()

        composeRule.onNodeWithText("Play Writer").assertIsDisplayed()
        composeRule.onNodeWithText("Play Genre").assertIsDisplayed()
        composeRule.onNodeWithText("Play Duration (minutes)").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals("Theater", viewModel.uiState.value.eventType)
        }
    }

    @Test
    fun selectingSportsShowsSportsFields() {
        val viewModel = renderForm()

        openEventTypeMenu()
        composeRule.onNodeWithTag("sports_eventType_input").performClick()

        composeRule.onNodeWithText("Sport Type").assertIsDisplayed()
        composeRule.onNodeWithText("Home Team").assertIsDisplayed()
        composeRule.onNodeWithText("Visiting Team").assertIsDisplayed()
        composeRule.onNodeWithText("Sport League").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals("Sports", viewModel.uiState.value.eventType)
        }
    }

    @Test
    fun selectingFilmShowsFilmFields() {
        val viewModel = renderForm()

        openEventTypeMenu()
        composeRule.onNodeWithTag("film_eventType_input").performClick()

        composeRule.onNodeWithText("Film Director").assertIsDisplayed()
        composeRule.onNodeWithText("Film Runtime (minutes)").assertIsDisplayed()
        composeRule.onNodeWithText("Film Rating (?/5)").assertIsDisplayed()
        composeRule.onNodeWithText("Film Genre").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals("Film", viewModel.uiState.value.eventType)
        }
    }

    @Test
    fun selectingConcertShowsConcertFields() {
        val viewModel = renderForm()

        openEventTypeMenu()
        composeRule.onNodeWithTag("concert_eventType_input").performClick()

        composeRule.onNodeWithText("Concert Artist").assertIsDisplayed()
        composeRule.onNodeWithText("Concert Genre").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals("Concert", viewModel.uiState.value.eventType)
        }
    }

    @Test
    fun numericTextFields_ignoreInvalidInput_andAcceptValidInput() {
        val viewModel = renderForm()

        composeRule.onNodeWithTag("create_minPrice_input").performTextInput("abc")
        composeRule.runOnIdle {
            assertEquals("", viewModel.uiState.value.minPrice)
        }

        composeRule.onNodeWithTag("create_minPrice_input").performTextInput("12.5")
        composeRule.runOnIdle {
            assertEquals("12.5", viewModel.uiState.value.minPrice)
        }
    }

    @Test
    fun checkbox_togglesOpenOnlyState() {
        val viewModel = renderForm()

        composeRule.onNodeWithTag("open_only_checkbox").performClick()

        composeRule.runOnIdle {
            assertFalse(viewModel.uiState.value.isOpenOnly)
        }

        composeRule.onNodeWithTag("open_only_checkbox").performClick()

        composeRule.runOnIdle {
            assertTrue(viewModel.uiState.value.isOpenOnly)
        }
    }

    private fun renderForm(): FilterEventViewModel {
        val viewModel = FilterEventViewModel()
        composeRule.setContent {
            FilterEventForm(viewModel = viewModel)
        }
        return viewModel
    }

    private fun openEventTypeMenu() {
        composeRule.onNodeWithTag("create_eventType_input").performClick()
    }
}
