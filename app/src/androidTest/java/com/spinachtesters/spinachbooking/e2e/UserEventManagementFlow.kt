package com.spinachtesters.spinachbooking.e2e

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.ui.navigation.Screen
import com.spinachtesters.spinachbooking.ui.screens.EventDetailScreen
import com.spinachtesters.spinachbooking.ui.screens.HomeScreen
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

class UserE2eActions(
    private val composeRule: ComposeTestRule,
    private val navControllerProvider: () -> TestNavHostController,
    private val createdUsernames: MutableSet<String>,
    private val eventRepository: EventRepository
) {
    fun goToLoginFromSignUp() {
        composeRule.onNodeWithTag("login_toggle").performClick()
        waitForRoute(Screen.Login.route)
    }

    fun signUpUserAccount(
        username: String,
        email: String,
        fullName: String,
        password: String = "StrongPass123!"
    ) {
        createdUsernames += username
        composeRule.onNodeWithTag("signup_contact_input").performTextClearance()
        composeRule.onNodeWithTag("signup_contact_input").performTextInput(email)
        composeRule.onNodeWithTag("signup_fullname_input").performTextClearance()
        composeRule.onNodeWithTag("signup_fullname_input").performTextInput(fullName)
        composeRule.onNodeWithTag("signup_username_input").performTextClearance()
        composeRule.onNodeWithTag("signup_username_input").performTextInput(username)
        composeRule.onNodeWithTag("signup_password_input").performTextClearance()
        composeRule.onNodeWithTag("signup_password_input").performTextInput(password)
        composeRule.onNodeWithTag("signup_confirm_password_input").performTextClearance()
        composeRule.onNodeWithTag("signup_confirm_password_input").performTextInput(password)
        composeRule.onNodeWithText("User").performClick()
        composeRule.onNodeWithTag("signup_submit_button").performClick()
        waitForRoute(Screen.Login.route)
    }

    fun loginUserAccount(identifier: String, password: String = "StrongPass123!") {
        composeRule.onNodeWithTag("login_identifier_input").performTextClearance()
        composeRule.onNodeWithTag("login_identifier_input").performTextInput(identifier)
        composeRule.onNodeWithTag("login_password_input").performTextClearance()
        composeRule.onNodeWithTag("login_password_input").performTextInput(password)
        composeRule.onNodeWithTag("login_submit_button").performClick()
        waitForRoute(Screen.Home.route)
    }

    fun registerForEvent(
        eventTitle: String,
        expectNotificationError: Boolean = false
    ) {
        openEventDetailByTitle(eventTitle)
        waitForRoute(Screen.EventDetail.route)

        waitForActionButton()

        if (hasTextNode("Participate")) {
            composeRule.onNodeWithText("Participate").performClick()
            composeRule.onNodeWithText("Yes").performClick()
            handlePostBookingFlow(
                expectNotificationError = expectNotificationError,
                errorPrefix = "Booking completed, but notification failed:"
            )
        } else {
            composeRule.runOnIdle { navControllerProvider().popBackStack() }
            waitForRoute(Screen.Home.route)
        }
        refreshHome()
    }

    fun deregisterFromEvent(
        eventTitle: String,
        expectNotificationError: Boolean = false
    ) {
        openEventDetailByTitle(eventTitle)
        waitForRoute(Screen.EventDetail.route)

        waitForActionButton()

        if (hasTextNode("Cancel")) {
            composeRule.onNodeWithText("Cancel").performClick()
            composeRule.onNodeWithText("Yes").performClick()
            handlePostBookingFlow(
                expectNotificationError = expectNotificationError,
                errorPrefix = "Cancellation completed, but notification failed:"
            )
        } else {
            composeRule.runOnIdle { navControllerProvider().popBackStack() }
            waitForRoute(Screen.Home.route)
        }
        refreshHome()
    }

    private fun handlePostBookingFlow(expectNotificationError: Boolean, errorPrefix: String) {
        if (expectNotificationError) {
            // In e2e, notification can fail (dialog) or still succeed (direct Home).
            composeRule.waitUntil(20_000) {
                navControllerProvider().currentBackStackEntry?.destination?.route == Screen.Home.route ||
                    composeRule.onAllNodesWithText(errorPrefix, substring = true)
                        .fetchSemanticsNodes().isNotEmpty()
            }

            val isErrorVisible = composeRule
                .onAllNodesWithText(errorPrefix, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()

            if (isErrorVisible) {
                composeRule.onNodeWithText(errorPrefix, substring = true).assertIsDisplayed()
                composeRule.onNodeWithText("OK").performClick()
            }

            if (navControllerProvider().currentBackStackEntry?.destination?.route != Screen.Home.route) {
                composeRule.runOnIdle { navControllerProvider().popBackStack() }
            }
            waitForRoute(Screen.Home.route)
        } else {
            waitForRoute(Screen.Home.route)
        }
    }

    fun assertBookingExists(eventTitle: String) {
        val bookedDisplayTitle = toBookedCardTitle(eventTitle)
        composeRule.waitUntil(40_000) {
            composeRule.onAllNodesWithText(bookedDisplayTitle).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(bookedDisplayTitle).assertIsDisplayed()
    }

    fun assertBookingMissing(eventTitle: String) {
        val bookedDisplayTitle = toBookedCardTitle(eventTitle)
        composeRule.waitUntil(20_000) {
            composeRule.onAllNodesWithText(bookedDisplayTitle).fetchSemanticsNodes().isEmpty()
        }
    }

    fun assertEventIsNotBooked(eventTitle: String) {
        openEventDetailByTitle(eventTitle)
        waitForRoute(Screen.EventDetail.route)
        waitForActionButton()
        composeRule.onNodeWithText("Participate").assertIsDisplayed()
        composeRule.runOnIdle { navControllerProvider().popBackStack() }
        waitForRoute(Screen.Home.route)
    }

    fun assertConflictRejected(
        eventTitle: String,
        errorText: String = "You have a conflicting booking during this time."
    ) {
        openEventDetailByTitle(eventTitle)
        waitForRoute(Screen.EventDetail.route)
        waitForActionButton()
        composeRule.onNodeWithText("Participate").performClick()
        composeRule.waitUntil(20_000) {
            composeRule.onAllNodesWithText(errorText).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(errorText).assertIsDisplayed()
        composeRule.onNodeWithText("OK").performClick()
        composeRule.runOnIdle { navControllerProvider().popBackStack() }
        waitForRoute(Screen.Home.route)
    }

    private fun openEventDetailByTitle(eventTitle: String) {
        val eventId = runBlocking {
            eventRepository.getAll().firstOrNull { it.title == eventTitle }?.id
        }
        require(!eventId.isNullOrBlank()) { "Event not found for title: $eventTitle" }

        composeRule.runOnIdle {
            navControllerProvider().navigate(Screen.EventDetail.createRoute(eventId))
        }
    }

    private fun waitForRoute(route: String) {
        composeRule.waitUntil(20_000) {
            navControllerProvider().currentBackStackEntry?.destination?.route == route
        }
        composeRule.runOnIdle {
            assertEquals(route, navControllerProvider().currentBackStackEntry?.destination?.route)
        }
    }

    private fun waitForActionButton() {
        composeRule.waitUntil(20_000) {
            hasTextNode("Participate") || hasTextNode("Cancel")
        }
    }

    private fun hasTextNode(text: String): Boolean {
        return composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    private fun refreshHome() {
        composeRule.runOnIdle {
            navControllerProvider().navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
        waitForRoute(Screen.Home.route)
    }

    private fun toBookedCardTitle(title: String): String {
        return if (title.length > 15) "${title.take(15)}..." else title
    }
}

@RunWith(AndroidJUnit4::class)
class UserEventManagementFlow {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController
    private lateinit var organizerActions: OrganizerE2eActions
    private lateinit var userActions: UserE2eActions
    private lateinit var addEventViewModel: AddEventViewModel
    private lateinit var loginViewModel: LoginViewModel

    private val userRepository = UserRepository()
    private val eventRepository = EventRepository()
    private val bookingRepository = BookingRepository()

    private val createdUsernames = mutableSetOf<String>()
    private val createdEventTitles = mutableSetOf<String>()

    @Before
    fun setup() {
        SessionManager.clearSession()
        initializeTestNavHost()

        organizerActions = OrganizerE2eActions(
            composeRule = composeRule,
            navControllerProvider = { navController },
            addEventViewModel = addEventViewModel,
            createdUsernames = createdUsernames,
            createdEventTitles = createdEventTitles
        )
        userActions = UserE2eActions(
            composeRule = composeRule,
            navControllerProvider = { navController },
            createdUsernames = createdUsernames,
            eventRepository = eventRepository
        )
    }

    private fun initializeTestNavHost() {
        val sharedEventRepository = EventRepository()
        val signUpViewModel = SignUpViewModel()
        loginViewModel = LoginViewModel()
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
                composable(Screen.SignUp.route) {
                    SignUpScreen(navController = navController, viewModel = signUpViewModel)
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
                    AddEventScreen(navController = navController, viewModel = addEventViewModel)
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
                composable(Screen.Home.route) {
                    HomeScreen(navController = navController)
                }
                composable(
                    route = Screen.EventDetail.route,
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    EventDetailScreen(
                        eventId = backStackEntry.arguments?.getString("eventId"),
                        navController = navController
                    )
                }
            }
        }
    }

    @After
    fun cleanup() = runBlocking {
        val createdUsers = createdUsernames.mapNotNull { username ->
            userRepository.findByUsername(username)
        }
        val createdEvents = eventRepository.getAll().filter { event ->
            createdEventTitles.contains(event.title)
        }

        createdUsers.forEach { user ->
            createdEvents.forEach { event ->
                bookingRepository.deleteById("${user.id}-${event.id}")
            }
        }

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
    fun user_registers_for_event_booking_appears_on_home() {
        val stamp = System.currentTimeMillis().toString().takeLast(6)
        val eventTitle = "UE_REGISTER_EVENT_$stamp"

        organizerActions.signUpAndLoginOrganizer()
        organizerActions.addConcertEvent(eventTitle)

        resetToSignUp()

        val username = "ue_user_$stamp"
        userActions.signUpUserAccount(
            username = username,
            email = "$username@mail.com",
            fullName = "User $stamp"
        )
        userActions.loginUserAccount(username)

        userActions.registerForEvent(eventTitle, expectNotificationError = true)
        userActions.assertBookingExists(eventTitle)
    }

    @Test
    fun user_deregisters_event_booking_removed_from_home() {
        val stamp = System.currentTimeMillis().toString().takeLast(6)
        val eventTitle = "UE_DEREGISTER_EVENT_$stamp"

        organizerActions.signUpAndLoginOrganizer()
        organizerActions.addConcertEvent(eventTitle)

        resetToSignUp()

        val username = "ue_user2_$stamp"
        userActions.signUpUserAccount(
            username = username,
            email = "$username@mail.com",
            fullName = "User Two $stamp"
        )
        userActions.loginUserAccount(username)

        userActions.registerForEvent(eventTitle, expectNotificationError = true)
        userActions.assertBookingExists(eventTitle)

        userActions.deregisterFromEvent(eventTitle, expectNotificationError = true)
        assertBookingRemoved(eventTitle)
    }

    @Test
    fun user_conflicting_booking_is_rejected() {
        val stamp = System.currentTimeMillis().toString().takeLast(6)
        val firstEvent = "UE_CONFLICT_A_$stamp"
        val secondEvent = "UE_CONFLICT_B_$stamp"

        organizerActions.signUpAndLoginOrganizer()
        organizerActions.addConcertEvent(firstEvent)
        organizerActions.addFilmEvent(secondEvent)

        resetToSignUp()

        val username = "ue_conflict_$stamp"
        userActions.signUpUserAccount(
            username = username,
            email = "$username@mail.com",
            fullName = "Conflict User $stamp"
        )
        userActions.loginUserAccount(username)

        userActions.registerForEvent(firstEvent, expectNotificationError = true)
        userActions.assertBookingExists(firstEvent)

        userActions.assertConflictRejected(secondEvent)
        userActions.assertBookingMissing(secondEvent)
    }

    @Test
    fun user_cancel_when_not_booked_is_noop() {
        val stamp = System.currentTimeMillis().toString().takeLast(6)
        val eventTitle = "UE_NOOP_CANCEL_$stamp"

        organizerActions.signUpAndLoginOrganizer()
        organizerActions.addConcertEvent(eventTitle)

        resetToSignUp()

        val username = "ue_noop_$stamp"
        userActions.signUpUserAccount(
            username = username,
            email = "$username@mail.com",
            fullName = "Noop User $stamp"
        )
        userActions.loginUserAccount(username)

        userActions.assertEventIsNotBooked(eventTitle)
        userActions.deregisterFromEvent(eventTitle)
        userActions.assertBookingMissing(eventTitle)
    }

    @Test
    fun user_booking_persists_after_session_reopen() {
        val stamp = System.currentTimeMillis().toString().takeLast(6)
        val eventTitle = "UE_PERSIST_BOOKING_$stamp"

        organizerActions.signUpAndLoginOrganizer()
        organizerActions.addConcertEvent(eventTitle)

        resetToSignUp()

        val username = "ue_persist_$stamp"
        userActions.signUpUserAccount(
            username = username,
            email = "$username@mail.com",
            fullName = "Persist User $stamp"
        )
        userActions.loginUserAccount(username)
        userActions.registerForEvent(eventTitle, expectNotificationError = true)
        userActions.assertBookingExists(eventTitle)

        resetToSignUp()
        userActions.goToLoginFromSignUp()
        userActions.loginUserAccount(username)
        userActions.assertBookingExists(eventTitle)
    }

    @Test
    fun event_detail_invalid_id_shows_not_found() {
        val invalidEventId = "invalid-event-${System.currentTimeMillis()}"

        composeRule.runOnIdle {
            navController.navigate(Screen.EventDetail.createRoute(invalidEventId))
        }
        waitForRoute(Screen.EventDetail.route)

        composeRule.waitUntil(20_000) {
            composeRule.onAllNodesWithTag("event_detail_error").fetchSemanticsNodes().isNotEmpty()
        }

        val showsNotFound = composeRule
            .onAllNodesWithText("Event not found.")
            .fetchSemanticsNodes()
            .isNotEmpty()
        val showsLoadFailure = composeRule
            .onAllNodesWithText("Could not load event.")
            .fetchSemanticsNodes()
            .isNotEmpty()

        composeRule.onNodeWithTag("event_detail_error").assertIsDisplayed()
        check(showsNotFound || showsLoadFailure) {
            "Expected EventDetail error text to be 'Event not found.' or 'Could not load event.'"
        }
    }

    private fun resetToSignUp() {
        SessionManager.clearSession()
        loginViewModel.consumeAuthenticationSuccess()
        composeRule.runOnIdle {
            navController.navigate(Screen.SignUp.route) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
        waitForRoute(Screen.SignUp.route)
    }

    private fun assertBookingRemoved(eventTitle: String) {
        val bookedDisplayTitle =
            if (eventTitle.length > 15) "${eventTitle.take(15)}..." else eventTitle
        composeRule.waitUntil(20_000) {
            composeRule.onAllNodesWithText(bookedDisplayTitle).fetchSemanticsNodes().isEmpty()
        }
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