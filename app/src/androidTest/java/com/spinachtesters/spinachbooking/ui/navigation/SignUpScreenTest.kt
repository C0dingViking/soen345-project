package com.spinachtesters.spinachbooking.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavGraph(navController = navController)
        }
    }

    @Test
    fun appLaunchesOnSignUpScreen() {
        composeRule.runOnIdle {
            assertEquals(Screen.SignUp.route, navController.currentBackStackEntry?.destination?.route)
        }

        composeRule.onNodeWithText("Have an account? Login").assertIsDisplayed()
    }

    @Test
    fun clickingRegisterTextOnLogin_navigatesToSignUp() {
        composeRule.onNodeWithText("Have an account? Login").performClick()
        composeRule.onNodeWithText("No Account? Register").performClick()

        composeRule.runOnIdle {
            assertEquals(Screen.SignUp.route, navController.currentBackStackEntry?.destination?.route)
        }
    }

    @Test
    fun signUp_withBlankFields_showsRequiredNameError() {
        clickSignUpButton()

        composeRule.onNodeWithText("Full name is required.").assertIsDisplayed()
    }

    @Test
    fun signUp_withShortUsername_showsUsernameLengthError() {
        composeRule.onNodeWithTag("signup_contact_input").performTextInput("jane@example.com")
        composeRule.onNodeWithTag("signup_fullname_input").performTextInput("Jane Doe")
        composeRule.onNodeWithTag("signup_username_input").performTextInput("jan")
        composeRule.onNodeWithTag("signup_password_input").performTextInput("StrongPass123!")
        composeRule.onNodeWithTag("signup_confirm_password_input").performTextInput("StrongPass123!")

        clickSignUpButton()

        composeRule.onNodeWithText("Username must be at least 4 characters long.").assertIsDisplayed()
    }

    @Test
    fun signUp_withInvalidEmail_showsEmailError() {
        composeRule.onNodeWithTag("signup_contact_input").performTextInput("not-an-email")
        composeRule.onNodeWithTag("signup_fullname_input").performTextInput("Jane Doe")
        composeRule.onNodeWithTag("signup_username_input").performTextInput("janedoe")
        composeRule.onNodeWithTag("signup_password_input").performTextInput("StrongPass123!")
        composeRule.onNodeWithTag("signup_confirm_password_input").performTextInput("StrongPass123!")

        clickSignUpButton()

        composeRule.onNodeWithText("Please enter a valid email address.").assertIsDisplayed()
    }

    @Test
    fun signUp_withPhoneModeAndInvalidPhone_showsPhoneError() {
        composeRule.onNodeWithText("phone").performClick()
        composeRule.onNodeWithTag("signup_contact_input").performTextInput("123")
        composeRule.onNodeWithTag("signup_fullname_input").performTextInput("Jane Doe")
        composeRule.onNodeWithTag("signup_username_input").performTextInput("janedoe")
        composeRule.onNodeWithTag("signup_password_input").performTextInput("StrongPass123!")
        composeRule.onNodeWithTag("signup_confirm_password_input").performTextInput("StrongPass123!")

        clickSignUpButton()

        composeRule.onNodeWithText("Please enter a valid phone number.").assertIsDisplayed()
    }

    @Test
    fun signUp_withPasswordMismatch_showsMismatchError() {
        composeRule.onNodeWithTag("signup_contact_input").performTextInput("jane@example.com")
        composeRule.onNodeWithTag("signup_fullname_input").performTextInput("Jane Doe")
        composeRule.onNodeWithTag("signup_username_input").performTextInput("janedoe")
        composeRule.onNodeWithTag("signup_password_input").performTextInput("StrongPass123!")
        composeRule.onNodeWithTag("signup_confirm_password_input").performTextInput("DifferentPass123!")

        clickSignUpButton()

        composeRule.onNodeWithText("Passwords do not match.").assertIsDisplayed()
    }

    @Test
    fun signUp_withWeakPassword_showsPasswordStrengthError() {
        composeRule.onNodeWithTag("signup_contact_input").performTextInput("jane@example.com")
        composeRule.onNodeWithTag("signup_fullname_input").performTextInput("Jane Doe")
        composeRule.onNodeWithTag("signup_username_input").performTextInput("janedoe")
        composeRule.onNodeWithTag("signup_password_input").performTextInput("weak")
        composeRule.onNodeWithTag("signup_confirm_password_input").performTextInput("weak")

        clickSignUpButton()

        composeRule.onNodeWithText("Password must be at least 12 characters long.").assertIsDisplayed()
    }

    private fun clickSignUpButton() {
        composeRule.onNodeWithTag("signup_submit_button").performClick()
    }
}
