package com.spinachtesters.spinachbooking.ui.viewmodels

import android.util.Log
import com.google.firebase.database.DatabaseException
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.domain.security.PasswordEncoder
import com.spinachtesters.spinachbooking.domain.security.PasswordHashResult
import com.spinachtesters.spinachbooking.testutils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val passwordEncoder: PasswordEncoder = mockk()

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @Test
    fun signUp_withWeakPassword_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        fillValidEmailMode(viewModel)
        viewModel.onPasswordChanged("weak")
        viewModel.onConfirmPasswordChanged("weak")

        viewModel.signUp(useEmail = true)

        assertEquals(
            "Password must be at least 12 characters long.",
            viewModel.uiState.value.errorMessage
        )
        coVerify(exactly = 0) { userRepository.create(any()) }
    }

    @Test
    fun signUp_withInvalidEmail_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        fillValidEmailMode(viewModel)
        viewModel.onEmailChanged("invalid-email")

        viewModel.signUp(useEmail = true)

        assertEquals("Please enter a valid email address.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun signUp_withInvalidPhone_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        fillValidPhoneMode(viewModel)
        viewModel.onPhoneNumberChanged("abc")

        viewModel.signUp(useEmail = false)

        assertEquals("Please enter a valid phone number.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun signUp_withUsernameTooShort_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        fillValidEmailMode(viewModel)
        viewModel.onUsernameChanged("abc")

        viewModel.signUp(useEmail = true)

        assertEquals(
            "Username must be at least 4 characters long.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun signUp_withPasswordMismatch_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        fillValidEmailMode(viewModel)
        viewModel.onConfirmPasswordChanged("DifferentPass123!")

        viewModel.signUp(useEmail = true)

        assertEquals("Passwords do not match.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun signUp_withMissingUppercase_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        fillValidEmailMode(viewModel)
        viewModel.onPasswordChanged("lowercase123!")
        viewModel.onConfirmPasswordChanged("lowercase123!")

        viewModel.signUp(useEmail = true)

        assertEquals(
            "Password must include an uppercase letter.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun signUp_withMissingLowercase_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        fillValidEmailMode(viewModel)
        viewModel.onPasswordChanged("UPPERCASE123!")
        viewModel.onConfirmPasswordChanged("UPPERCASE123!")

        viewModel.signUp(useEmail = true)

        assertEquals(
            "Password must include a lowercase letter.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun signUp_withMissingDigit_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        fillValidEmailMode(viewModel)
        viewModel.onPasswordChanged("NoDigitsHere!!")
        viewModel.onConfirmPasswordChanged("NoDigitsHere!!")

        viewModel.signUp(useEmail = true)

        assertEquals("Password must include a number.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun signUp_withMissingSpecialCharacter_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        fillValidEmailMode(viewModel)
        viewModel.onPasswordChanged("NoSpecial1234")
        viewModel.onConfirmPasswordChanged("NoSpecial1234")

        viewModel.signUp(useEmail = true)

        assertEquals(
            "Password must include a special character.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun signUp_withUsernameAlreadyTaken_setsErrorAndStops() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)
        fillValidEmailMode(viewModel)

        coEvery { userRepository.usernameExists("janedoe") } returns true

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertEquals("Username is already taken.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { userRepository.create(any()) }
        coVerify(exactly = 0) { userRepository.emailExists(any()) }
    }

    @Test
    fun signUp_withEmailAlreadyRegistered_setsErrorAndStops() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)
        fillValidEmailMode(viewModel)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.emailExists("jane@example.com") } returns true

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertEquals("Email is already registered.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { userRepository.create(any()) }
    }

    @Test
    fun signUp_withPhoneAlreadyRegistered_setsErrorAndStops() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)
        fillValidPhoneMode(viewModel)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.phoneExists("5145551234") } returns true

        viewModel.signUp(useEmail = false)
        advanceUntilIdle()

        assertEquals("Phone number is already registered.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { userRepository.create(any()) }
        coVerify(exactly = 0) { userRepository.emailExists(any()) }
    }

    @Test
    fun signUp_withValidInput_createsUserAndMarksSuccess() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.emailExists("jane@example.com") } returns false
        coEvery { userRepository.create(any()) } answers { firstArg() }
        every { passwordEncoder.hash("StrongPass123!") } returns PasswordHashResult(
            hash = "hash",
            salt = "salt",
            iterations = 210000
        )

        fillValidEmailMode(viewModel)

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("", viewModel.uiState.value.password)
        assertEquals("", viewModel.uiState.value.confirmPassword)
        coVerify(exactly = 1) { userRepository.create(any()) }
    }

    @Test
    fun signUp_withValidPhoneMode_persistsPhoneAndClearsEmail() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.phoneExists("5145551234") } returns false
        coEvery { userRepository.create(any()) } answers { firstArg() }
        every { passwordEncoder.hash("StrongPass123!") } returns PasswordHashResult(
            hash = "hash",
            salt = "salt",
            iterations = 210000
        )

        fillValidPhoneMode(viewModel)

        viewModel.signUp(useEmail = false)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            userRepository.create(
                match {
                    it.phoneNb == "5145551234" &&
                            it.email == ""
                }
            )
        }
    }

    @Test
    fun signUp_withOrganizerSelected_persistsOrganizerFlag() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        coEvery { userRepository.usernameExists("organizer1") } returns false
        coEvery { userRepository.emailExists("org@example.com") } returns false
        coEvery { userRepository.create(any()) } answers { firstArg() }
        every { passwordEncoder.hash("StrongPass123!") } returns PasswordHashResult(
            hash = "hash",
            salt = "salt",
            iterations = 210000
        )

        viewModel.onFullNameChanged("Org User")
        viewModel.onUsernameChanged("organizer1")
        viewModel.onEmailChanged("org@example.com")
        viewModel.onPasswordChanged("StrongPass123!")
        viewModel.onConfirmPasswordChanged("StrongPass123!")
        viewModel.onIsOrganizerChanged(true)

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        coVerify(exactly = 1) { userRepository.create(match { it.organizer }) }
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun signUp_whenUnknownHost_setsFriendlyErrorMessage() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)
        fillValidEmailMode(viewModel)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.emailExists("jane@example.com") } returns false
        every { passwordEncoder.hash("StrongPass123!") } returns PasswordHashResult("h", "s", 1)
        coEvery { userRepository.create(any()) } throws UnknownHostException("offline")

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertEquals(
            "No internet connection. Please connect and try again.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun signUp_whenUnknownHostIsCause_setsFriendlyErrorMessage() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)
        fillValidEmailMode(viewModel)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.emailExists("jane@example.com") } returns false
        every { passwordEncoder.hash("StrongPass123!") } returns PasswordHashResult("h", "s", 1)
        coEvery { userRepository.create(any()) } throws RuntimeException(
            "wrapped",
            UnknownHostException("offline")
        )

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertEquals(
            "No internet connection. Please connect and try again.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun signUp_whenDatabaseException_setsFriendlyErrorMessage() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)
        fillValidEmailMode(viewModel)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.emailExists("jane@example.com") } returns false
        every { passwordEncoder.hash("StrongPass123!") } returns PasswordHashResult("h", "s", 1)
        coEvery { userRepository.create(any()) } throws DatabaseException("denied")

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertEquals(
            "Database write failed. Please check Firebase rules and try again.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun signUp_whenPbkdf2HashingFails_setsFriendlyErrorMessage() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)
        fillValidEmailMode(viewModel)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.emailExists("jane@example.com") } returns false
        every { passwordEncoder.hash("StrongPass123!") } throws RuntimeException("PBKDF2 unavailable")

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertEquals(
            "Device security provider is not supported for password hashing.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun signUp_whenUnexpectedException_setsGenericError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)
        fillValidEmailMode(viewModel)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.emailExists("jane@example.com") } returns false
        every { passwordEncoder.hash("StrongPass123!") } throws RuntimeException("something else")

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertEquals("Could not complete sign up.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun consumeSuccess_resetsSuccessFlag() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        coEvery { userRepository.usernameExists("janedoe") } returns false
        coEvery { userRepository.emailExists("jane@example.com") } returns false
        coEvery { userRepository.create(any()) } answers { firstArg() }
        every { passwordEncoder.hash("StrongPass123!") } returns PasswordHashResult("h", "s", 1)

        fillValidEmailMode(viewModel)
        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        viewModel.consumeSuccess()
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    private fun fillValidEmailMode(viewModel: SignUpViewModel) {
        viewModel.onFullNameChanged("Jane Doe")
        viewModel.onUsernameChanged("janedoe")
        viewModel.onEmailChanged("jane@example.com")
        viewModel.onPasswordChanged("StrongPass123!")
        viewModel.onConfirmPasswordChanged("StrongPass123!")
    }

    private fun fillValidPhoneMode(viewModel: SignUpViewModel) {
        viewModel.onFullNameChanged("Jane Doe")
        viewModel.onUsernameChanged("janedoe")
        viewModel.onPhoneNumberChanged("5145551234")
        viewModel.onPasswordChanged("StrongPass123!")
        viewModel.onConfirmPasswordChanged("StrongPass123!")
    }
}
