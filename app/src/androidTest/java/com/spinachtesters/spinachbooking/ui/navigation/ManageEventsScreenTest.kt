package com.spinachtesters.spinachbooking.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.ui.screens.ManageEventsScreen
import com.spinachtesters.spinachbooking.ui.viewmodels.ManageEventsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ManageEventsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: ManageEventsViewModel
    private lateinit var eventRepository: EventRepository

    @Before
    fun setup() {
        eventRepository = mockk(relaxed = true)

        coEvery { eventRepository.getAll() } returns listOf(
            Event(
                id = "1",
                title = "Test Event",
                location = "Montreal",
                date = LocalDate.now(),
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1),
                ticketPrice = 10.0,
                status = "Open",
                details = ConcertDetails("Artist", "Rock")
            )
        )

        viewModel = ManageEventsViewModel(eventRepository)

        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            NavHost(
                navController = navController,
                startDestination = "manage_events"
            ) {
                composable("manage_events") {
                    ManageEventsScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
                composable("modify_event/{eventId}") { backStackEntry ->
                    Text("Modify ${backStackEntry.arguments?.getString("eventId")}")
                }
            }
        }
    }

    @Test
    fun showsEmptyMessageWhenNoEvents() {
        coEvery { eventRepository.getAll() } returns emptyList()
        viewModel.loadEvents()
        composeRule.onNodeWithText("No events have been created yet... make one below!")
            .assertExists()
    }

    @Test
    fun showsEventCardWhenEventsExist() {
        composeRule.onNodeWithText("Test Event")
            .assertExists()
    }

    @Test
    fun clickingDeleteOpensConfirmationDialog() {
        composeRule.onNodeWithContentDescription("Delete")
            .performClick()

        composeRule.onNodeWithText("Delete Event")
            .assertExists()

        composeRule.onNodeWithText("Are you sure you want to delete this event?")
            .assertExists()
    }

    @Test
    fun confirmingDeleteCallsRepositoryDelete() {
        composeRule.onNodeWithContentDescription("Delete")
            .performClick()

        composeRule.onNodeWithText("Delete")
            .performClick()

        coVerify { eventRepository.deleteById("1") }
    }

    @Test
    fun clickingEventCardNavigatesToModifyEvent() {

        composeRule.onNodeWithText("Test Event")
            .performClick()

        composeRule.onNodeWithText("Modify 1")
            .assertExists()
    }

}
