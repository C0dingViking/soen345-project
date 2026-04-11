package com.spinachtesters.spinachbooking.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.Booking
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.ui.screens.HomeScreen
import com.spinachtesters.spinachbooking.ui.viewmodels.EventFilter
import com.spinachtesters.spinachbooking.ui.viewmodels.FilterEventViewModel
import com.spinachtesters.spinachbooking.ui.viewmodels.HomeViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        SessionManager.clearSession()
    }

    @Test
    fun homeScreen_showsCoreComponents() {
        renderHome()
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

    @Test
    fun clickingAvailableEventCard_navigatesToEventDetail() {
        renderHome()
        composeRule.onAllNodesWithTag("available_event_card")[0].performClick()

        composeRule.runOnIdle {
            assertEquals(
                Screen.EventDetail.route,
                navController.currentBackStackEntry?.destination?.route
            )
            assertEquals("1", navController.currentBackStackEntry?.arguments?.getString("eventId"))
        }
    }

    @Test
    fun clickingBlankBookedEventCard_doesNotNavigate() {
        val blankBookedEvent = Event(
            id = "",
            title = "Blank Booked Event",
            date = LocalDate.of(2026, 12, 14),
            startTime = LocalDateTime.of(2026, 12, 14, 20, 0),
            endTime = LocalDateTime.of(2026, 12, 14, 23, 0),
            ticketPrice = 200.0,
            location = "Laval, QC",
            status = "BOOKED",
            details = ConcertDetails()
        )
        val homeViewModel = HomeViewModel(
            eventRepository = mockk<EventRepository>().also {
                coEvery { it.getAll() } returns listOf(blankBookedEvent)
            },
            bookingRepository = mockk<BookingRepository>().also {
                coEvery { it.getAll() } returns listOf(
                    Booking(
                        bookedBy = "u1",
                        bookedFor = "",
                        dateOfBooking = LocalDate.of(2026, 1, 1),
                        status = "ACTIVE"
                    )
                )
            },
            sessionManager = SessionManager
        )
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        renderHome(homeViewModel = homeViewModel, loadOnStart = true)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("booked_event_card").performClick()

        composeRule.runOnIdle {
            assertEquals("home", navController.currentBackStackEntry?.destination?.route)
        }
    }

    @Test
    fun searchDialog_validSearch_resetsAndDismisses() {
        val filterViewModel = FilterEventViewModel().apply {
            onTitleChanged("Drake")
        }
        renderHome(filterViewModel = filterViewModel)

        openSearchDialog()
        composeRule.onNodeWithText("Search").performClick()

        composeRule.runOnIdle {
            assertEquals("", filterViewModel.uiState.value.title)
            assertFalse(filterViewModel.uiState.value.isError)
        }
        composeRule.onNodeWithText("Search Events").assertDoesNotExist()
    }

    @Test
    fun searchDialog_invalidSearch_keepsDialogOpenAndShowsError() {
        val filterViewModel = FilterEventViewModel().apply {
            onTitleChanged("Jazz")
            onMinPriceChanged("20")
            onMaxPriceChanged("10")
        }
        renderHome(filterViewModel = filterViewModel)

        openSearchDialog()
        composeRule.onNodeWithText("Search").performClick()

        composeRule.onNodeWithText("Search Events").assertExists()
        composeRule.onNodeWithText("Max Price must be greater than min price").assertExists()
        composeRule.runOnIdle {
            assertEquals("Jazz", filterViewModel.uiState.value.title)
            assertEquals(true, filterViewModel.uiState.value.isError)
        }
    }

    @Test
    fun searchDialog_cancel_resetsAndDismisses() {
        val filterViewModel = FilterEventViewModel().apply {
            onTitleChanged("Drake")
        }
        renderHome(filterViewModel = filterViewModel)

        openSearchDialog()
        composeRule.onNodeWithText("Cancel").performClick()

        composeRule.runOnIdle {
            assertEquals("", filterViewModel.uiState.value.title)
        }
        composeRule.onNodeWithText("Search Events").assertDoesNotExist()
    }

    @Test
    fun clearFilteredEvents_buttonClearsFilterState() {
        val homeViewModel = HomeViewModel(
            eventRepository = mockk(relaxed = true),
            bookingRepository = mockk(relaxed = true),
            sessionManager = SessionManager
        )
        homeViewModel.filterEvents(EventFilter())
        renderHome(homeViewModel = homeViewModel)

        composeRule.onNodeWithTag("clear_button").performClick()

        composeRule.runOnIdle {
            assertFalse(homeViewModel.uiState.value.isFilterActive)
            assertFalse(homeViewModel.uiState.value.filteredEvents.isNotEmpty())
        }
        composeRule.onNodeWithTag("clear_button").assertDoesNotExist()
    }

    private fun renderHome(
        homeViewModel: HomeViewModel = HomeViewModel(
            eventRepository = mockk(relaxed = true),
            bookingRepository = mockk(relaxed = true),
            sessionManager = SessionManager
        ),
        filterViewModel: FilterEventViewModel = FilterEventViewModel(),
        loadOnStart: Boolean = false
    ) {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        viewModel = homeViewModel,
                        filterEventViewModel = filterViewModel,
                        loadOnStart = loadOnStart,
                        navController = navController
                    )
                }
                composable(
                    route = Screen.EventDetail.route,
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) {
                    Text("Event Detail")
                }
            }
        }
    }

    private fun openSearchDialog() {
        composeRule.onNodeWithText("Search for an event...").performClick()
        composeRule.onNodeWithText("Search Events").assertExists()
    }
}
