package com.spinachtesters.spinachbooking.e2e

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

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

    @Before
    fun setup() {
        val sharedEventRepository = EventRepository()
        val signUpViewModel = SignUpViewModel()
        val loginViewModel = LoginViewModel()
        val manageEventsViewModel = ManageEventsViewModel(sharedEventRepository)
        addEventViewModel = AddEventViewModel(sharedEventRepository)

        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            NavHost(
                navController = navController,
                startDestination = Screen.SignUp.route
            ) {
                composable(Screen.SignUp.route) { SignUpScreen(navController = navController, viewModel = signUpViewModel) }
                composable(Screen.Login.route) { LoginScreen(navController = navController, viewModel = loginViewModel) }
                composable(Screen.ManageEvents.route) { ManageEventsScreen(navController = navController, viewModel = manageEventsViewModel) }
                composable(Screen.AddEvent.route) { AddEventScreen(navController = navController, viewModel = addEventViewModel) }
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

    private fun signUpAndLoginOrganizer() {
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

    private fun openAddEvent() {
        composeRule.onNodeWithContentDescription("Add Event").performClick()
        waitForRoute(Screen.AddEvent.route)
    }

    private fun addSportsEvent(title: String) {
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

    private fun addTheaterEvent(title: String) {
        openAddEvent()
        fillCommonEventFields(title, "Theater")
        scrollToTag("create_writer_input")
        setText("create_writer_input", "Miller")
        scrollToTag("create_genre_input")
        setText("create_genre_input", "Drama")
        submitAndWaitForManage(title)
    }

    private fun addConcertEvent(title: String) {
        openAddEvent()
        fillCommonEventFields(title, "Concert")
        scrollToTag("create_artist_input")
        setText("create_artist_input", "Coldplay")
        scrollToTag("create_genre_input")
        setText("create_genre_input", "Rock")
        submitAndWaitForManage(title)
    }

    private fun addFilmEvent(title: String) {
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

    private fun fillCommonEventFields(title: String, eventType: String) {
        setText("create_name_input", title)
        setText("create_price_input", "45")
        composeRule.onNodeWithTag("create_eventType_input").performClick()
        composeRule.onNodeWithText(eventType).performClick()

        composeRule.runOnUiThread {
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
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
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
}
