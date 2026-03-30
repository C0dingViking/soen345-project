package com.spinachtesters.spinachbooking.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.ui.screens.EventDetailScreen
import com.spinachtesters.spinachbooking.ui.viewmodels.EventDetailViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class EventDetailScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: EventDetailViewModel

    @Before
    fun setup() {
        val eventRepository = mockk<EventRepository>(relaxed = true)
        val bookingRepository = mockk<BookingRepository>(relaxed = true)

        coEvery { eventRepository.getById("event-1") } returns Event(
            id = "event-1",
            title = "Test Event Detail",
            location = "Montreal",
            date = LocalDate.of(2026, 5, 10),
            startTime = LocalDateTime.of(2026, 5, 10, 18, 0),
            endTime = LocalDateTime.of(2026, 5, 10, 20, 0),
            ticketPrice = 20.0,
            status = "AVAILABLE",
            details = SportDetails(homeTeam = "Canadiens", visitingTeam = "Rangers")
        )
        coEvery { bookingRepository.getAll() } returns emptyList()

        viewModel = EventDetailViewModel(eventRepository, bookingRepository)

        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            EventDetailScreen(
                eventId = "event-1",
                navController = navController,
                viewModel = viewModel
            )
        }
    }

    @Test
    fun eventDetailScreen_showsCoreComponents() {
        composeRule.onNodeWithText("Test Event Detail")
            .assertExists()
        composeRule.onNodeWithText("Location")
            .assertExists()
        composeRule.onNodeWithText("Price")
            .assertExists()
        composeRule.onNodeWithText("Date")
            .assertExists()
        composeRule.onNodeWithText("Time")
            .assertExists()
        composeRule.onNodeWithText("Description")
            .assertExists()
        composeRule.onNodeWithText("Participate")
            .assertExists()
    }
}

