package com.spinachtesters.spinachbooking.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.security.Pbkdf2PasswordEncoder
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.ui.viewmodels.LoginViewModel
import com.spinachtesters.spinachbooking.ui.viewmodels.SignUpViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpLoginIntegrationTest {

    private val userRepository = UserRepository()
    private val passwordEncoder = Pbkdf2PasswordEncoder()
    private val createdUserIds = mutableListOf<String>()
    private val stamp = System.currentTimeMillis().toString().takeLast(7)

    @Before
    fun setUp() {
        SessionManager.clearSession()
    }

    @After
    fun tearDown() = runBlocking {
        for (id in createdUserIds) {
            try { userRepository.deleteById(id) } catch (_: Exception) {}
        }
        SessionManager.clearSession()
    }

    @Test
    fun signUp_withEmail_createsUserInFirebase() = runBlocking {
        val username = "it_user_$stamp"
        val email = "it_$stamp@test.com"

        val signUpVm = SignUpViewModel(userRepository, passwordEncoder)
        signUpVm.onFullNameChanged("Integration Test User")
        signUpVm.onUsernameChanged(username)
        signUpVm.onEmailChanged(email)
        signUpVm.onPasswordChanged("StrongPass123!")
        signUpVm.onConfirmPasswordChanged("StrongPass123!")
        signUpVm.onIsOrganizerChanged(false)

        signUpVm.signUp(useEmail = true)
        waitUntil("sign up completes") {
            signUpVm.uiState.value.isSuccess || signUpVm.uiState.value.errorMessage != null
        }

        assertNull(signUpVm.uiState.value.errorMessage)
        assertTrue(signUpVm.uiState.value.isSuccess)

        val savedUser = userRepository.findByUsername(username)
        assertNotNull(savedUser)
        createdUserIds.add(savedUser!!.id)

        assertEquals("Integration Test User", savedUser.fullName)
        assertEquals(username, savedUser.username)
        assertEquals(email, savedUser.email)
        assertFalse(savedUser.organizer)
        assertTrue(savedUser.passwordHash.isNotBlank())
        assertTrue(savedUser.passwordSalt.isNotBlank())
    }

    @Test
    fun signUp_withPhone_createsOrganizerInFirebase() = runBlocking {
        val username = "it_org_$stamp"
        val phone = "+1234567${stamp}"

        val signUpVm = SignUpViewModel(userRepository, passwordEncoder)
        signUpVm.onFullNameChanged("Organizer User")
        signUpVm.onUsernameChanged(username)
        signUpVm.onPhoneNumberChanged(phone)
        signUpVm.onPasswordChanged("StrongPass123!")
        signUpVm.onConfirmPasswordChanged("StrongPass123!")
        signUpVm.onIsOrganizerChanged(true)

        signUpVm.signUp(useEmail = false)
        waitUntil("sign up completes") {
            signUpVm.uiState.value.isSuccess || signUpVm.uiState.value.errorMessage != null
        }

        assertNull(signUpVm.uiState.value.errorMessage)

        val savedUser = userRepository.findByUsername(username)
        assertNotNull(savedUser)
        createdUserIds.add(savedUser!!.id)

        assertEquals(phone, savedUser.phoneNb)
        assertTrue(savedUser.organizer)
    }

    @Test
    fun signUp_thenLogin_withUsername_authenticatesAndStartsSession() = runBlocking {
        val username = "it_login_$stamp"
        val password = "StrongPass123!"

        val signUpVm = SignUpViewModel(userRepository, passwordEncoder)
        signUpVm.onFullNameChanged("Login Test User")
        signUpVm.onUsernameChanged(username)
        signUpVm.onEmailChanged("it_login_$stamp@test.com")
        signUpVm.onPasswordChanged(password)
        signUpVm.onConfirmPasswordChanged(password)
        signUpVm.signUp(useEmail = true)
        waitUntil("sign up completes") {
            signUpVm.uiState.value.isSuccess || signUpVm.uiState.value.errorMessage != null
        }
        assertNull(signUpVm.uiState.value.errorMessage)

        val savedUser = userRepository.findByUsername(username)
        assertNotNull(savedUser)
        createdUserIds.add(savedUser!!.id)

        val loginVm = LoginViewModel(userRepository, passwordEncoder, SessionManager)
        loginVm.onIdentifierChanged(username)
        loginVm.onPasswordChanged(password)
        loginVm.login()
        waitUntil("login completes") {
            loginVm.uiState.value.isAuthenticated || loginVm.uiState.value.errorMessage != null
        }

        assertNull(loginVm.uiState.value.errorMessage)
        assertTrue(loginVm.uiState.value.isAuthenticated)
        assertEquals(savedUser.id, SessionManager.currentUserId)
    }

    @Test
    fun signUp_thenLogin_withEmail_authenticates() = runBlocking {
        val username = "it_email_login_$stamp"
        val email = "it_email_login_$stamp@test.com"
        val password = "StrongPass123!"

        val signUpVm = SignUpViewModel(userRepository, passwordEncoder)
        signUpVm.onFullNameChanged("Email Login User")
        signUpVm.onUsernameChanged(username)
        signUpVm.onEmailChanged(email)
        signUpVm.onPasswordChanged(password)
        signUpVm.onConfirmPasswordChanged(password)
        signUpVm.signUp(useEmail = true)
        waitUntil("sign up completes") {
            signUpVm.uiState.value.isSuccess || signUpVm.uiState.value.errorMessage != null
        }
        assertNull(signUpVm.uiState.value.errorMessage)

        val savedUser = userRepository.findByUsername(username)
        assertNotNull(savedUser)
        createdUserIds.add(savedUser!!.id)

        val loginVm = LoginViewModel(userRepository, passwordEncoder, SessionManager)
        loginVm.onIdentifierChanged(email)
        loginVm.onPasswordChanged(password)
        loginVm.login()
        waitUntil("login completes") {
            loginVm.uiState.value.isAuthenticated || loginVm.uiState.value.errorMessage != null
        }

        assertNull(loginVm.uiState.value.errorMessage)
        assertTrue(loginVm.uiState.value.isAuthenticated)
    }

    @Test
    fun login_withWrongPassword_doesNotAuthenticate() = runBlocking {
        val username = "it_wrongpw_$stamp"
        val password = "StrongPass123!"

        val signUpVm = SignUpViewModel(userRepository, passwordEncoder)
        signUpVm.onFullNameChanged("Wrong PW User")
        signUpVm.onUsernameChanged(username)
        signUpVm.onEmailChanged("it_wrongpw_$stamp@test.com")
        signUpVm.onPasswordChanged(password)
        signUpVm.onConfirmPasswordChanged(password)
        signUpVm.signUp(useEmail = true)
        waitUntil("sign up completes") {
            signUpVm.uiState.value.isSuccess || signUpVm.uiState.value.errorMessage != null
        }

        val savedUser = userRepository.findByUsername(username)
        assertNotNull(savedUser)
        createdUserIds.add(savedUser!!.id)

        val loginVm = LoginViewModel(userRepository, passwordEncoder, SessionManager)
        loginVm.onIdentifierChanged(username)
        loginVm.onPasswordChanged("WrongPassword123!")
        loginVm.login()
        waitUntil("login completes") {
            loginVm.uiState.value.isAuthenticated || loginVm.uiState.value.errorMessage != null
        }

        assertFalse(loginVm.uiState.value.isAuthenticated)
        assertEquals("Invalid credentials.", loginVm.uiState.value.errorMessage)
    }

    @Test
    fun signUp_duplicateUsername_returnsError() = runBlocking {
        val username = "it_dup_$stamp"
        val password = "StrongPass123!"

        val signUpVm1 = SignUpViewModel(userRepository, passwordEncoder)
        signUpVm1.onFullNameChanged("First User")
        signUpVm1.onUsernameChanged(username)
        signUpVm1.onEmailChanged("it_dup1_$stamp@test.com")
        signUpVm1.onPasswordChanged(password)
        signUpVm1.onConfirmPasswordChanged(password)
        signUpVm1.signUp(useEmail = true)
        waitUntil("first sign up completes") {
            signUpVm1.uiState.value.isSuccess || signUpVm1.uiState.value.errorMessage != null
        }
        assertNull(signUpVm1.uiState.value.errorMessage)

        val savedUser = userRepository.findByUsername(username)
        assertNotNull(savedUser)
        createdUserIds.add(savedUser!!.id)

        val signUpVm2 = SignUpViewModel(userRepository, passwordEncoder)
        signUpVm2.onFullNameChanged("Second User")
        signUpVm2.onUsernameChanged(username)
        signUpVm2.onEmailChanged("it_dup2_$stamp@test.com")
        signUpVm2.onPasswordChanged(password)
        signUpVm2.onConfirmPasswordChanged(password)
        signUpVm2.signUp(useEmail = true)
        waitUntil("second sign up completes") {
            signUpVm2.uiState.value.isSuccess || signUpVm2.uiState.value.errorMessage != null
        }

        assertFalse(signUpVm2.uiState.value.isSuccess)
        assertEquals("Username is already taken.", signUpVm2.uiState.value.errorMessage)
    }

    @Test
    fun login_organizerFlag_reflectedInSession() = runBlocking {
        val username = "it_orgflag_$stamp"
        val password = "StrongPass123!"

        val signUpVm = SignUpViewModel(userRepository, passwordEncoder)
        signUpVm.onFullNameChanged("Organizer Flag User")
        signUpVm.onUsernameChanged(username)
        signUpVm.onEmailChanged("it_orgflag_$stamp@test.com")
        signUpVm.onPasswordChanged(password)
        signUpVm.onConfirmPasswordChanged(password)
        signUpVm.onIsOrganizerChanged(true)
        signUpVm.signUp(useEmail = true)
        waitUntil("sign up completes") {
            signUpVm.uiState.value.isSuccess || signUpVm.uiState.value.errorMessage != null
        }

        val savedUser = userRepository.findByUsername(username)
        assertNotNull(savedUser)
        createdUserIds.add(savedUser!!.id)

        val loginVm = LoginViewModel(userRepository, passwordEncoder, SessionManager)
        loginVm.onIdentifierChanged(username)
        loginVm.onPasswordChanged(password)
        loginVm.login()
        waitUntil("login completes") {
            loginVm.uiState.value.isAuthenticated || loginVm.uiState.value.errorMessage != null
        }

        assertTrue(loginVm.uiState.value.isOrganizer)
        assertTrue(SessionManager.state.value.isOrganizer)
    }

    private suspend fun waitUntil(
        reason: String,
        timeoutMillis: Long = 20_000,
        pollMillis: Long = 100,
        condition: () -> Boolean
    ) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            delay(pollMillis)
        }
        throw AssertionError("Timeout waiting for condition: $reason")
    }
}
