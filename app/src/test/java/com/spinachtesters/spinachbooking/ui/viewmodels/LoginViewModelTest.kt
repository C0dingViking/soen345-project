package com.spinachtesters.spinachbooking.ui.viewmodels

import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.User
import com.spinachtesters.spinachbooking.domain.security.PasswordEncoder
import com.spinachtesters.spinachbooking.testutils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()

    @BeforeEach
    fun resetSession() {
        SessionManager.clearSession()
    }

    @Test
    fun login_withBlankFields_setsError() = runTest {
        val viewModel = LoginViewModel(userRepository, passwordEncoder)

        viewModel.login()

        assertEquals(
            "Please provide your username/email/phone and password.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun login_withUnknownUser_setsInvalidCredentials() = runTest {
        val viewModel = LoginViewModel(userRepository, passwordEncoder, SessionManager)
        coEvery { userRepository.findByLoginIdentifier("unknown") } returns null

        viewModel.onIdentifierChanged("unknown")
        viewModel.onPasswordChanged("StrongPass123!")

        viewModel.login()
        advanceUntilIdle()

        assertEquals("Invalid credentials.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals("", SessionManager.currentUserId)
    }

    @Test
    fun login_withValidCredentials_authenticates() = runTest {
        val viewModel = LoginViewModel(userRepository, passwordEncoder, SessionManager)
        val user = User(
            id = "u1",
            username = "jane",
            passwordHash = "hash",
            passwordSalt = "salt",
            passwordIterations = 210000,
            email = "jane@example.com"
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

        viewModel.onIdentifierChanged("jane")
        viewModel.onPasswordChanged("StrongPass123!")

        viewModel.login()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals(null, viewModel.uiState.value.errorMessage)
        assertEquals("", viewModel.uiState.value.password)
        assertEquals("u1", SessionManager.currentUserId)
    }
}

