package com.spinachtesters.spinachbooking.ui.viewmodels

import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.domain.security.PasswordEncoder
import com.spinachtesters.spinachbooking.domain.security.PasswordHashResult
import com.spinachtesters.spinachbooking.testutils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val passwordEncoder: PasswordEncoder = mockk()

    @Test
    fun signUp_withWeakPassword_setsValidationError() = runTest {
        val viewModel = SignUpViewModel(userRepository, passwordEncoder)

        viewModel.onFullNameChanged("Jane Doe")
        viewModel.onUsernameChanged("janedoe")
        viewModel.onEmailChanged("jane@example.com")
        viewModel.onPasswordChanged("weak")
        viewModel.onConfirmPasswordChanged("weak")

        viewModel.signUp(useEmail = true)

        assertEquals("Password must be at least 12 characters long.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { userRepository.create(any()) }
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

        viewModel.onFullNameChanged("Jane Doe")
        viewModel.onUsernameChanged("janedoe")
        viewModel.onEmailChanged("jane@example.com")
        viewModel.onPasswordChanged("StrongPass123!")
        viewModel.onConfirmPasswordChanged("StrongPass123!")

        viewModel.signUp(useEmail = true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.errorMessage)
        coVerify(exactly = 1) { userRepository.create(any()) }
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
}
