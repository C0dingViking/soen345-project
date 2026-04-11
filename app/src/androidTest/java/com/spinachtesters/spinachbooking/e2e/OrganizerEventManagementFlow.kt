package com.spinachtesters.spinachbooking.e2e

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.ui.navigation.Screen
import com.spinachtesters.spinachbooking.ui.screens.AddEventScreen
import com.spinachtesters.spinachbooking.ui.screens.LoginScreen
import com.spinachtesters.spinachbooking.ui.screens.ManageEventsScreen
import com.spinachtesters.spinachbooking.ui.screens.SignUpScreen
import com.spinachtesters.spinachbooking.ui.viewmodels.AddEventViewModel
import com.spinachtesters.spinachbooking.ui.viewmodels.LoginViewModel
import com.spinachtesters.spinachbooking.ui.viewmodels.ManageEventsViewModel
import com.spinachtesters.spinachbooking.ui.viewmodels.SignUpViewModel
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

class OrganizerE2eActions(
    private val composeRule: ComposeTestRule,
    private val navControllerProvider: () -> TestNavHostController,
    private val addEventViewModel: AddEventViewModel,
    private val createdUsernames: MutableSet<String>,
    private val createdEventTitles: MutableSet<String>
) {
    fun signUpAndLoginOrganizer() {
        val stamp = System.currentTimeMillis().toString().takeLast(8)
        val username = "org$stamp"
        val email = "$username@mail.com"
        val password = "StrongPass123!"

        createdUsernames += username

        composeRule.onNodeWithTag("signup_contact_input").performTextInput(email)
        composeRule.onNodeWithTag("signup_fullname_input").performTextInput("Organizer $stamp")
        composeRule.onNodeWithTag("signup_username_input").performTextInput(username)
        composeRule.onNodeWithTag("signup_password_input").performTextInput(password)
        composeRule.onNodeWithTag("signup_confirm_password_input").performTextInput(password)
        composeRule.onNodeWithText("Organizer").performClick()
        composeRule.onNodeWithTag("signup_submit_button").performClick()

        waitForRoute(Screen.Login.route)

        composeRule.onNodeWithTag("login_identifier_input").performTextInput(username)
        composeRule.onNodeWithTag("login_password_input").performTextInput(password)
        composeRule.onNodeWithTag("login_submit_button").performClick()

        waitForRoute(Screen.ManageEvents.route)
    }

    fun addSportsEvent(title: String) {
        openAddEvent()
        fillCommonEventFields(title, "Sports")
        scrollToTag("create_sport_input")
        setText("create_sport_input", "Hockey")
        scrollToTag("create_home team_input")
        setText("create_home team_input", "Habs")
        scrollToTag("create_visiting team_input")
        setText("create_visiting team_input", "Rangers")
        scrollToTag("create_league_input")
        setText("create_league_input", "NHL")
        submitAndWaitForManage(title)
    }

    fun addTheaterEvent(title: String) {
        openAddEvent()
        fillCommonEventFields(title, "Theater")
        scrollToTag("create_writer_input")
        setText("create_writer_input", "Miller")
        scrollToTag("create_genre_input")
        setText("create_genre_input", "Drama")
        submitAndWaitForManage(title)
    }

    fun addConcertEvent(title: String) {
        openAddEvent()
        fillCommonEventFields(title, "Concert")
        scrollToTag("create_artist_input")
        setText("create_artist_input", "Coldplay")
        scrollToTag("create_genre_input")
        setText("create_genre_input", "Rock")
        submitAndWaitForManage(title)
    }

    fun addFilmEvent(title: String) {
        openAddEvent()
        fillCommonEventFields(title, "Film")
        scrollToTag("create_director_input")
        setText("create_director_input", "Nolan")
        scrollToTag("create_rating_input")
        setText("create_rating_input", "5")
        scrollToTag("create_genre_input")
        setText("create_genre_input", "SciFi")
        submitAndWaitForManage(title)
    }

    private fun openAddEvent() {
        composeRule.onNodeWithContentDescription("Add Event").performClick()
        waitForRoute(Screen.AddEvent.route)
    }

    private fun fillCommonEventFields(title: String, eventType: String) {
        setText("create_name_input", title)
        setText("create_price_input", "45")
        composeRule.onNodeWithTag("create_eventType_input").performClick()
        composeRule.onNodeWithText(eventType).performClick()

        composeRule.runOnIdle {
            val date = LocalDate.now().plusDays(1)
            addEventViewModel.onEventDateChanged("${date.year}-${date.monthValue}-${date.dayOfMonth}")
            addEventViewModel.onTimeStartChanged("18:00")
            addEventViewModel.onTimeEndChanged("20:00")
        }

        setText("create_location_input", "Montreal")
    }

    private fun setText(tag: String, value: String) {
        composeRule.onNodeWithTag(tag).performTextClearance()
        composeRule.onNodeWithTag(tag).performTextInput(value)
    }

    private fun submitAndWaitForManage(title: String) {
        createdEventTitles += title
        composeRule.onNodeWithText("Add Event").performClick()
        waitForRoute(Screen.ManageEvents.route)
    }

    private fun scrollToTag(tag: String) {
        composeRule.onNodeWithTag("form").performScrollToNode(hasTestTag(tag))
    }

    private fun waitForRoute(route: String) {
        composeRule.waitUntil(20_000) {
            navControllerProvider().currentBackStackEntry?.destination?.route == route
        }
        composeRule.runOnIdle {
            assertEquals(route, navControllerProvider().currentBackStackEntry?.destination?.route)
        }
    }
}

@RunWith(AndroidJUnit4::class)
class OrganizerEventManagementFlow {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController
    private lateinit var addEventViewModel: AddEventViewModel

    private val userRepository = UserRepository()
    private val eventRepository = EventRepository()

    private val createdUsernames = mutableSetOf<String>()
    private val createdEventTitles = mutableSetOf<String>()

    private lateinit var organizerActions: OrganizerE2eActions

    @Before
    fun setup() {
        val sharedEventRepository = EventRepository()
        val signUpViewModel = SignUpViewModel()
        val loginViewModel = LoginViewModel()
        val manageEventsViewModel = ManageEventsViewModel(sharedEventRepository)
        addEventViewModel = AddEventViewModel(sharedEventRepository)
        organizerActions = OrganizerE2eActions(
            composeRule = composeRule,
            navControllerProvider = { navController },
            addEventViewModel = addEventViewModel,
            createdUsernames = createdUsernames,
            createdEventTitles = createdEventTitles
        )

        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            NavHost(
                navController = navController,
                startDestination = Screen.SignUp.route
            ) {
                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        navController = navController,
                        viewModel = signUpViewModel
                    )
                }
                composable(Screen.Login.route) {
                    LoginScreen(
                        navController = navController,
                        viewModel = loginViewModel
                    )
                }
                composable(Screen.ManageEvents.route) {
                    ManageEventsScreen(
                        navController = navController,
                        viewModel = manageEventsViewModel
                    )
                }
                composable(Screen.AddEvent.route) {
                    AddEventScreen(
                        navController = navController,
                        viewModel = addEventViewModel
                    )
                }
                composable(
                    route = Screen.ModifyEvent.route,
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    AddEventScreen(
                        navController = navController,
                        viewModel = addEventViewModel,
                        eventId = backStackEntry.arguments?.getString("eventId"),
                        isModifyMode = true
                    )
                }
            }
        }
    }

    @After
    fun cleanup() = runBlocking {
        createdEventTitles.forEach { title ->
            eventRepository.getAll()
                .filter { it.title == title }
                .forEach { eventRepository.deleteById(it.id) }
        }
        createdEventTitles.clear()

        createdUsernames.forEach { username ->
            userRepository.findByUsername(username)?.let { userRepository.deleteById(it.id) }
        }
        createdUsernames.clear()
    }

    @Test
    fun organizer_addsFourEventTypes_eachAppears_thenDeletesAll() {
        signUpAndLoginOrganizer()

        val prefix = System.currentTimeMillis().toString().takeLast(6)
        val sportsTitle = "SP$prefix"
        val theaterTitle = "TH$prefix"
        val concertTitle = "CO$prefix"
        val filmTitle = "FI$prefix"

        addSportsEvent(sportsTitle)
        assertEventVisible(sportsTitle)

        addTheaterEvent(theaterTitle)
        assertEventVisible(theaterTitle)

        addConcertEvent(concertTitle)
        assertEventVisible(concertTitle)

        addFilmEvent(filmTitle)
        assertEventVisible(filmTitle)

        deleteEventAndConfirm(sportsTitle)
        deleteEventAndConfirm(theaterTitle)
        deleteEventAndConfirm(concertTitle)
        deleteEventAndConfirm(filmTitle)

        assertEventNotVisible(sportsTitle)
        assertEventNotVisible(theaterTitle)
        assertEventNotVisible(concertTitle)
        assertEventNotVisible(filmTitle)
    }

    @Test
    fun organizer_cancelDelete_keepsEventInList() {
        signUpAndLoginOrganizer()

        val title = "CX${System.currentTimeMillis().toString().takeLast(6)}"
        addConcertEvent(title)
        assertEventVisible(title)

        composeRule.onNodeWithTag("manage_events_list")
            .performScrollToNode(hasTestTag("manage_delete_$title"))
        composeRule.onNodeWithTag("manage_delete_$title", useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithText("Cancel").performClick()

        assertEventVisible(title)
    }

    @Test
    fun organizer_addEvent_withEmptyForm_showsRequiredError() {
        signUpAndLoginOrganizer()
        openAddEvent()

        composeRule.onNodeWithText("Add Event").performClick()
        composeRule.onNodeWithText("Event Name is required.").assertIsDisplayed()
    }

    @Test
    fun organizer_addEvent_withoutType_showsTypeRequiredError() {
        signUpAndLoginOrganizer()
        openAddEvent()

        composeRule.onNodeWithTag("create_name_input").performTextInput("NO_TYPE")
        composeRule.onNodeWithTag("create_price_input").performTextInput("25")

        composeRule.runOnUiThread {
            val date = LocalDate.now().plusDays(1)
            addEventViewModel.onEventDateChanged("${date.year}-${date.monthValue}-${date.dayOfMonth}")
            addEventViewModel.onTimeStartChanged("18:00")
            addEventViewModel.onTimeEndChanged("20:00")
        }

        composeRule.onNodeWithTag("create_location_input").performTextInput("Montreal")
        composeRule.onNodeWithText("Add Event").performClick()

        composeRule.onNodeWithText("Event Type is required.").assertIsDisplayed()
    }

    @Test
    fun organizer_modifyEvent_basicModification_persistsUpdatedValues() {
        signUpAndLoginOrganizer()

        val stamp = System.currentTimeMillis().toString().takeLast(6)
        val originalTitle = "MB$stamp"
        val updatedTitle = "MU$stamp"

        addConcertEvent(originalTitle)
        assertEventVisible(originalTitle)

        openModifyEventByTitle(originalTitle)
        setText("create_name_input", updatedTitle)
        setText("create_price_input", "60")
        setText("create_location_input", "Quebec")
        composeRule.onNodeWithText("Update Event").performClick()
        waitForRoute(Screen.ManageEvents.route)

        createdEventTitles += updatedTitle

        assertEventNotVisible(originalTitle)
        assertEventVisible(updatedTitle)

        val updatedEvent = waitForEventByTitle(updatedTitle)
        assertEquals("Quebec", updatedEvent.location)
        assertEquals(60.0, updatedEvent.ticketPrice, 0.0)
        assertEquals("concert", updatedEvent.details.detailType)
    }

    @Test
    fun organizer_modifyEvent_changeType_concertToFilm_persistsFilmDetails() {
        signUpAndLoginOrganizer()

        val stamp = System.currentTimeMillis().toString().takeLast(6)
        val originalTitle = "TC$stamp"
        val updatedTitle = "TF$stamp"

        addConcertEvent(originalTitle)
        assertEventVisible(originalTitle)

        openModifyEventByTitle(originalTitle)
        setText("create_name_input", updatedTitle)
        composeRule.onNodeWithTag("create_eventType_input").performClick()
        composeRule.onNodeWithText("Film").performClick()

        scrollToTag("create_director_input")
        setText("create_director_input", "Villeneuve")
        scrollToTag("create_rating_input")
        setText("create_rating_input", "4")
        scrollToTag("create_genre_input")
        setText("create_genre_input", "Drama")

        composeRule.onNodeWithText("Update Event").performClick()
        waitForRoute(Screen.ManageEvents.route)

        createdEventTitles += updatedTitle

        val updatedEvent = waitForEventByTitle(updatedTitle)
        assertEquals("film", updatedEvent.details.detailType)
        assertTrue(updatedEvent.details is FilmDetails)

        val filmDetails = updatedEvent.details as FilmDetails
        assertEquals("Villeneuve", filmDetails.director)
        assertEquals(4, filmDetails.rating)
        assertEquals("Drama", filmDetails.genre)
    }

    @Test
    fun organizer_modifyEvent_noModification_arrowBackKeepsEventUnchanged() {
        signUpAndLoginOrganizer()

        val title = "NB${System.currentTimeMillis().toString().takeLast(6)}"
        addConcertEvent(title)
        assertEventVisible(title)

        val before = waitForEventByTitle(title)

        openModifyEventByTitle(title)
        composeRule.onNodeWithTag("topbar_back_button").performClick()
        waitForRoute(Screen.ManageEvents.route)

        assertEventVisible(title)

        val after = waitForEventByTitle(title)
        assertEquals(before.id, after.id)
        assertEquals(before.title, after.title)
        assertEquals(before.location, after.location)
        assertEquals(before.ticketPrice, after.ticketPrice, 0.0)
        assertEquals(before.details.detailType, after.details.detailType)
    }

    fun signUpAndLoginOrganizer() = organizerActions.signUpAndLoginOrganizer()

    fun addSportsEvent(title: String) = organizerActions.addSportsEvent(title)

    fun addTheaterEvent(title: String) = organizerActions.addTheaterEvent(title)

    fun addConcertEvent(title: String) = organizerActions.addConcertEvent(title)

    fun addFilmEvent(title: String) = organizerActions.addFilmEvent(title)

    private fun openAddEvent() {
        composeRule.onNodeWithContentDescription("Add Event").performClick()
        waitForRoute(Screen.AddEvent.route)
    }

    private fun setText(tag: String, value: String) {
        composeRule.onNodeWithTag(tag).performTextClearance()
        composeRule.onNodeWithTag(tag).performTextInput(value)
    }

    private fun submitAndWaitForManage(title: String) {
        createdEventTitles += title
        composeRule.onNodeWithText("Add Event").performClick()
        waitForRoute(Screen.ManageEvents.route)
    }

    private fun deleteEventAndConfirm(title: String) {
        composeRule.onNodeWithTag("manage_events_list")
            .performScrollToNode(hasTestTag("manage_delete_$title"))
        composeRule.onNodeWithTag("manage_delete_$title", useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitForIdle()
    }

    private fun assertEventVisible(title: String) {
        composeRule.waitUntil(20_000) {
            val scrolledToItem = runCatching {
                composeRule.onNodeWithTag("manage_events_list")
                    .performScrollToNode(hasText(title))
                true
            }.getOrElse { false }

            scrolledToItem && composeRule.onAllNodesWithText(title).fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun assertEventNotVisible(title: String) {
        composeRule.waitUntil(20_000) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun scrollToTag(tag: String) {
        composeRule.onNodeWithTag("form").performScrollToNode(hasTestTag(tag))
    }

    private fun waitForRoute(route: String) {
        composeRule.waitUntil(20_000) {
            navController.currentBackStackEntry?.destination?.route == route
        }
        composeRule.runOnIdle {
            assertEquals(route, navController.currentBackStackEntry?.destination?.route)
        }
    }

    private fun openModifyEventByTitle(title: String) {
        composeRule.onNodeWithTag("manage_events_list")
            .performScrollToNode(hasText(title))
        composeRule.onNodeWithText(title)
            .performClick()
        waitForModifyRoute()
    }

    private fun waitForEventByTitle(title: String): Event {
        var found: Event? = null
        composeRule.waitUntil(20_000) {
            found = runBlocking {
                eventRepository.getAll().firstOrNull { it.title == title }
            }
            found != null
        }
        return found!!
    }

    private fun waitForModifyRoute() {
        composeRule.waitUntil(20_000) {
            navController.currentBackStackEntry?.destination?.route == Screen.ModifyEvent.route
        }
        composeRule.runOnIdle {
            assertEquals(
                Screen.ModifyEvent.route,
                navController.currentBackStackEntry?.destination?.route
            )
        }
    }
}
