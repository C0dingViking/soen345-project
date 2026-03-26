package com.spinachtesters.spinachbooking.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.material.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.ui.screens.AddEventScreen
import com.spinachtesters.spinachbooking.ui.viewmodels.AddEventViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddEventScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: AddEventViewModel
    private lateinit var eventRepository: EventRepository

    @SuppressLint("ViewModelConstructorInComposable")
    @Before
    fun setup() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            eventRepository = mockk(relaxed = true)
            viewModel = AddEventViewModel(eventRepository)

            NavHost(
                navController = navController,
                startDestination = "add_event"
            ) {
                composable("add_event") { AddEventScreen(navController, viewModel) }
                composable("manage_events") { Text("Manage Events") }
            }
        }
    }

    @Test
    fun showsErrorWhenSubmittingEmptyForm() {
        composeRule.onNodeWithText("Add Event")
            .performClick()

        composeRule.onNodeWithText("Event Name is required.")
            .assertExists()
    }

    @Test
    fun fillingFieldsClearsError() {
        composeRule.onNodeWithText("Add Event")
            .performClick()

        composeRule.onNodeWithText("Event Name is required.")
            .assertExists()

        composeRule.onNodeWithTag("create_name_input")
            .performTextInput("My Event")

        composeRule.onNodeWithText("Event Name is required.")
            .assertDoesNotExist()
    }

    @Test
    fun selectingFilmShowsFilmFields() {
        composeRule.onNodeWithTag("create_eventType_input")
            .performClick()

        composeRule.onNodeWithText("Film")
            .performClick()

        composeRule.onNodeWithText("Film Director")
            .assertExists()

        composeRule.onNodeWithTag("form")
            .performScrollToNode(hasText("Film Rating (?/5)"))

        composeRule.onNodeWithText("Film Rating (?/5)")
            .assertExists()

        composeRule.onNodeWithText("Film Rating (?/5)")
            .assertExists()

        composeRule.onNodeWithTag("form")
            .performScrollToNode(hasText("Film Genre"))

        composeRule.onNodeWithText("Film Genre")
            .assertExists()
    }

    private fun scrollTo(tag: String) {
        composeRule.onNode(hasScrollAction())   // finds the LazyColumn
            .performScrollToNode(hasTestTag(tag))
    }

    private fun setValidEntries() {
        scrollTo("create_name_input")
        composeRule.onNodeWithTag("create_name_input")
            .performTextInput("Concert Night")

        scrollTo("create_price_input")
        composeRule.onNodeWithTag("create_price_input")
            .performTextInput("20")

        scrollTo("create_eventType_input")
        composeRule.onNodeWithTag("create_eventType_input")
            .performClick()
        composeRule.onNodeWithText("Concert")
            .performClick()

        scrollTo("create_location_input")
        composeRule.onNodeWithTag("create_location_input")
            .performTextInput("Montreal")

        composeRule.runOnUiThread {
            viewModel.onEventDateChanged("2026-05-10")
            viewModel.onTimeStartChanged("18:00")
            viewModel.onTimeEndChanged("20:00")
        }

        scrollTo("create_artist_input")
        composeRule.onNodeWithTag("create_artist_input")
            .performTextInput("Artist")

        scrollTo("create_genre_input")
        composeRule.onNodeWithTag("create_genre_input")
            .performTextInput("Rock")
    }

    @Test
    fun submittingValidFormCallsRepository() = runTest {
        coEvery { eventRepository.create(any()) } answers { firstArg<Event>() }

        setValidEntries()

        composeRule.onNodeWithText("Add Event")
            .performClick()

        coVerify { eventRepository.create(any()) }
    }


    @Test
    fun successNavigatesBack() = runTest {
        coEvery { eventRepository.create(any()) } answers { firstArg<Event>() }

        setValidEntries()

        composeRule.onNodeWithText("Add Event")
            .performClick()

        composeRule.waitForIdle()

        assertEquals("manage_events", navController.currentDestination?.route)
    }
}
