package com.spinachtesters.spinachbooking.e2e

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.ui.navigation.NavGraph
import com.spinachtesters.spinachbooking.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationFlow {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController
    private val userRepository = UserRepository()
    private val createdUsernames = mutableSetOf<String>()

    @Before
    fun setup() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavGraph(navController = navController)
        }
    }

    @After
    fun cleanupCreatedUsers() = runBlocking {
        createdUsernames.forEach { username ->
            cleanupByUsername(username)
        }
        createdUsernames.clear()
    }

    @Test
    fun appStart_signup_toLogin_invalidLogin_showsError() {
        goToLogin()

        val missingUser = "non-existent-e2e-user-${System.currentTimeMillis()}"
        composeRule.onNodeWithTag("login_identifier_input").performTextInput(missingUser)
        composeRule.onNodeWithTag("login_password_input").performTextInput("WrongPassword123!")
        composeRule.onNodeWithTag("login_submit_button").performClick()

        waitForText("Invalid credentials.")
        composeRule.onNodeWithText("Invalid credentials.").assertIsDisplayed()
    }

    @Test
    fun signupAndLogin_regularUser_navigatesToHome() {
        val account = newAccount("user")
        signUpAccount(account, isOrganizer = false)
        waitForRoute(Screen.Login.route)

        login(account.username, account.password)
        waitForRoute(Screen.Home.route)
    }

    @Test
    fun signupAndLogin_organizer_navigatesToManageEvents() {
        val account = newAccount("organizer")
        signUpAccount(account, isOrganizer = true)
        waitForRoute(Screen.Login.route)

        login(account.username, account.password)
        waitForRoute(Screen.ManageEvents.route)
    }

    @Test
    fun signup_withDuplicateUsername_showsError() {
        val first = newAccount("duplicate")
        signUpAccount(first, isOrganizer = false)
        waitForRoute(Screen.Login.route)

        composeRule.onNodeWithText("No Account? Register").performClick()
        waitForRoute(Screen.SignUp.route)

        val second = first.copy(email = "second_${System.currentTimeMillis()}@mail.com")
        fillSignUpForm(second)
        composeRule.onNodeWithTag("signup_submit_button").performClick()

        composeRule.onNodeWithText("Username is already taken.").assertIsDisplayed()
    }

    private fun goToLogin() {
        composeRule.onNodeWithTag("login_toggle").performClick()
        waitForRoute(Screen.Login.route)
    }

    private fun signUpAccount(account: TestAccount, isOrganizer: Boolean) {
        createdUsernames += account.username
        fillSignUpForm(account)
        if (isOrganizer) {
            composeRule.onNodeWithText("Organizer").performClick()
        } else {
            composeRule.onNodeWithText("User").performClick()
        }
        composeRule.onNodeWithTag("signup_submit_button").performClick()
    }

    private fun fillSignUpForm(account: TestAccount) {
        composeRule.onNodeWithTag("signup_contact_input").performTextInput(account.email)
        composeRule.onNodeWithTag("signup_fullname_input").performTextInput(account.fullName)
        composeRule.onNodeWithTag("signup_username_input").performTextInput(account.username)
        composeRule.onNodeWithTag("signup_password_input").performTextInput(account.password)
        composeRule.onNodeWithTag("signup_confirm_password_input").performTextInput(account.password)
    }

    private fun login(identifier: String, password: String) {
        composeRule.onNodeWithTag("login_identifier_input").performTextInput(identifier)
        composeRule.onNodeWithTag("login_password_input").performTextInput(password)
        composeRule.onNodeWithTag("login_submit_button").performClick()
    }

    private fun waitForRoute(route: String) {
        composeRule.waitUntil(15_000) {
            navController.currentBackStackEntry?.destination?.route == route
        }
        composeRule.runOnIdle {
            assertEquals(route, navController.currentBackStackEntry?.destination?.route)
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(15_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private suspend fun cleanupByUsername(username: String) {
        repeat(4) {
            val user = userRepository.findByUsername(username)
            if (user != null) {
                userRepository.deleteById(user.id)
                return
            }
            delay(500)
        }
    }

    private fun newAccount(label: String): TestAccount {
        val stamp = System.currentTimeMillis()
        val username = "e2e_auth_${label}_$stamp"
        return TestAccount(
            username = username,
            email = "$username@mail.com",
            fullName = "E2E $label Tester",
            password = "StrongPass123!"
        )
    }

    private data class TestAccount(
        val username: String,
        val email: String,
        val fullName: String,
        val password: String
    )
}