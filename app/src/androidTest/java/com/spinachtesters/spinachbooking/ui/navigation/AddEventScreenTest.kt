package com.spinachtesters.spinachbooking.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.ui.screens.AddEventScreen
import com.spinachtesters.spinachbooking.ui.viewmodels.AddEventViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

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
        eventRepository = mockk(relaxed = true)
        viewModel = AddEventViewModel(eventRepository)

        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            NavHost(
                navController = navController,
                startDestination = "add_event"
            ) {
                composable("add_event") { AddEventScreen(navController, viewModel) }
                composable("modify_event/{eventId}") { backStackEntry ->
                    AddEventScreen(
                        navController = navController,
                        viewModel = viewModel,
                        eventId = backStackEntry.arguments?.getString("eventId"),
                        isModifyMode = true
                    )
                }
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

    private fun openModifyEventScreen(existingEvent: Event) {
        coEvery { eventRepository.getById(existingEvent.id) } returns existingEvent
        coEvery { eventRepository.save(any(), any()) } just Runs

        composeRule.runOnUiThread {
            navController.navigate("modify_event/${existingEvent.id}")
        }
        composeRule.waitForIdle()
    }

    @Test
    fun modifyModePrepopulatesFields() {
        val existingEvent = Event(
            id = "existing-1",
            title = "Edited Concert",
            date = LocalDate.of(2026, 6, 20),
            startTime = LocalDateTime.of(2026, 6, 20, 18, 0),
            endTime = LocalDateTime.of(2026, 6, 20, 21, 0),
            ticketPrice = 80.0,
            location = "Montreal",
            status = "Open",
            details = ConcertDetails(id = "detail-1", mainArtist = "Coldplay", genre = "Rock")
        )

        openModifyEventScreen(existingEvent)

        composeRule.onNodeWithText("Modify Event").assertExists()
        composeRule.onNodeWithText("Update Event").assertExists()
        composeRule.onNodeWithTag("create_name_input").assertTextContains("Edited Concert")
        composeRule.onNodeWithTag("create_location_input").assertTextContains("Montreal")
        composeRule.onNodeWithTag("create_eventType_input").assertTextContains("Concert")
    }

    @Test
    fun updatingExistingEventCallsSaveAndNavigatesBack() = runTest {
        val existingEvent = Event(
            id = "existing-2",
            title = "Old Concert",
            date = LocalDate.of(2026, 6, 21),
            startTime = LocalDateTime.of(2026, 6, 21, 18, 0),
            endTime = LocalDateTime.of(2026, 6, 21, 21, 0),
            ticketPrice = 50.0,
            location = "Old Venue",
            status = "Open",
            details = ConcertDetails(id = "detail-2", mainArtist = "Artist", genre = "Rock")
        )

        openModifyEventScreen(existingEvent)

        composeRule.onNodeWithTag("create_name_input").performTextClearance()
        composeRule.onNodeWithTag("create_name_input").performTextInput("New Concert")
        composeRule.onNodeWithTag("create_location_input").performTextClearance()
        composeRule.onNodeWithTag("create_location_input").performTextInput("Bell Centre")

        composeRule.onNodeWithText("Update Event").performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) {
            eventRepository.save(
                "existing-2",
                match { it.title == "New Concert" && it.location == "Bell Centre" }
            )
        }
        assertEquals("manage_events", navController.currentDestination?.route)
    }
}
