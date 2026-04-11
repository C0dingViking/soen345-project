package com.spinachtesters.spinachbooking.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.User
import com.spinachtesters.spinachbooking.domain.security.PasswordEncoder
import com.spinachtesters.spinachbooking.ui.screens.LoginScreen
import com.spinachtesters.spinachbooking.ui.screens.HomeScreen
import com.spinachtesters.spinachbooking.ui.screens.ManageEventsScreen
import com.spinachtesters.spinachbooking.ui.screens.SignUpScreen
import com.spinachtesters.spinachbooking.ui.viewmodels.LoginViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        SessionManager.clearSession()
    }

    @Test
    fun clickingLoginTextOnSignUp_navigatesToLogin() {
        renderNavGraph(startDestination = Screen.SignUp.route)

        composeRule.onNodeWithText("Have an account? Login").performClick()

        composeRule.runOnIdle {
            assertEquals(
                Screen.Login.route,
                navController.currentBackStackEntry?.destination?.route
            )
        }
    }

    @Test
    fun loginScreen_showsCoreComponents() {
        renderLoginScreen()

        assertTrue(composeRule.onAllNodesWithText("Login").fetchSemanticsNodes().isNotEmpty())
        composeRule.onNodeWithText("Username, Email, or Phone").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()
        composeRule.onNodeWithText("No Account? Register").assertIsDisplayed()
    }

    @Test
    fun login_withBlankFields_showsCredentialsError() {
        renderLoginScreen()

        clickLoginButton()

        composeRule.onNodeWithText("Please provide your username/email/phone and password.")
            .assertIsDisplayed()
    }

    @Test
    fun passwordVisibilityToggle_changesContentDescription() {
        renderLoginScreen()

        // false branches 🥲
        composeRule.onNodeWithContentDescription("Show password").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Show password").performClick()
        composeRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Hide password").performClick()
        composeRule.onNodeWithContentDescription("Show password").assertIsDisplayed()
    }

    @Test
    fun clickingRegister_navigatesToSignup() {
        renderLoginScreen()

        composeRule.onNodeWithText("No Account? Register").performClick()

        composeRule.runOnIdle {
            assertEquals(
                Screen.SignUp.route,
                navController.currentBackStackEntry?.destination?.route
            )
        }
    }

    @Test
    fun login_withValidCredentials_navigatesToHome() {
        val userRepository: UserRepository = mockk()
        val passwordEncoder: PasswordEncoder = mockk()
        val viewModel = LoginViewModel(userRepository, passwordEncoder, SessionManager)
        val user = User(
            id = "u1",
            fullName = "Jane Doe",
            username = "jane",
            passwordHash = "hash",
            passwordSalt = "salt",
            passwordIterations = 210000,
            email = "jane@example.com",
            phoneNb = "5145551234",
            organizer = false
        )

        coEvery { userRepository.findByLoginIdentifier("jane") } returns user
        every {
            passwordEncoder.verify(
                plainTextPassword = "StrongPass123!",
                hash = "hash",
                salt = "salt",
                iterations = 210000
            )
        } returns true

        renderLoginScreen(viewModel)
        composeRule.onNodeWithTag("login_identifier_input").performTextInput("jane")
        composeRule.onNodeWithTag("login_password_input").performTextInput("StrongPass123!")
        clickLoginButton()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            navController.currentBackStackEntry?.destination?.route == Screen.Home.route
        }

        composeRule.runOnIdle {
            assertEquals(Screen.Home.route, navController.currentBackStackEntry?.destination?.route)
            assertEquals("u1", SessionManager.currentUserId)
        }
    }

    @Test
    fun login_withOrganizerCredentials_navigatesToManageEvents() {
        val userRepository: UserRepository = mockk()
        val passwordEncoder: PasswordEncoder = mockk()
        val viewModel = LoginViewModel(userRepository, passwordEncoder, SessionManager)
        val user = User(
            id = "u2",
            fullName = "Org User",
            username = "org",
            passwordHash = "hash",
            passwordSalt = "salt",
            passwordIterations = 210000,
            email = "org@example.com",
            phoneNb = "5145554321",
            organizer = true
        )

        coEvery { userRepository.findByLoginIdentifier("org") } returns user
        every {
            passwordEncoder.verify(
                plainTextPassword = "StrongPass123!",
                hash = "hash",
                salt = "salt",
                iterations = 210000
            )
        } returns true

        renderLoginScreen(viewModel)
        composeRule.onNodeWithTag("login_identifier_input").performTextInput("org")
        composeRule.onNodeWithTag("login_password_input").performTextInput("StrongPass123!")
        clickLoginButton()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            navController.currentBackStackEntry?.destination?.route == Screen.ManageEvents.route
        }

        composeRule.runOnIdle {
            assertEquals(
                Screen.ManageEvents.route,
                navController.currentBackStackEntry?.destination?.route
            )
            assertEquals("u2", SessionManager.currentUserId)
        }
    }

    private fun clickLoginButton() {
        composeRule.onNodeWithTag("login_submit_button").performClick()
    }

    private fun renderLoginScreen(viewModel: LoginViewModel = LoginViewModel()) {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController = navController, startDestination = Screen.Login.route) {
                composable(Screen.Login.route) {
                    LoginScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.SignUp.route) { SignUpScreen(navController) }
                composable(Screen.Home.route) {
                    HomeScreen(
                        loadOnStart = false,
                        navController = navController
                    )
                }
                composable(Screen.ManageEvents.route) { ManageEventsScreen(navController) }
            }
        }
    }

    private fun renderNavGraph(startDestination: String = Screen.Login.route) {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController = navController, startDestination = startDestination) {
                composable(Screen.Login.route) { LoginScreen(navController = navController) }
                composable(Screen.SignUp.route) { SignUpScreen(navController) }
                composable(Screen.Home.route) {
                    HomeScreen(
                        loadOnStart = false,
                        navController = navController
                    )
                }
                composable(Screen.ManageEvents.route) { ManageEventsScreen(navController) }
            }
        }
    }
}
