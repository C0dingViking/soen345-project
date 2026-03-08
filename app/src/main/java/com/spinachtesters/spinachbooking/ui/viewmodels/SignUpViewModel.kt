package com.spinachtesters.spinachbooking.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseException
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.security.Pbkdf2PasswordEncoder
import com.spinachtesters.spinachbooking.domain.models.User
import com.spinachtesters.spinachbooking.domain.security.PasswordEncoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException

data class SignUpUiState(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isOrganizer: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class SignUpViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val passwordEncoder: PasswordEncoder = Pbkdf2PasswordEncoder()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFullNameChanged(value: String) = _uiState.update { it.copy(fullName = value, errorMessage = null) }
    fun onUsernameChanged(value: String) = _uiState.update { it.copy(username = value, errorMessage = null) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPhoneNumberChanged(value: String) = _uiState.update { it.copy(phoneNumber = value, errorMessage = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChanged(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun onIsOrganizerChanged(value: Boolean) = _uiState.update { it.copy(isOrganizer = value, errorMessage = null) }

    fun signUp(useEmail: Boolean) {
        val state = _uiState.value
        if (state.isLoading) return

        val validationError = validate(state, useEmail)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            try {
                if (userRepository.usernameExists(state.username.trim())) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Username is already taken.") }
                    return@launch
                }

                if (useEmail && userRepository.emailExists(state.email.trim())) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Email is already registered.") }
                    return@launch
                }

                if (!useEmail && userRepository.phoneExists(state.phoneNumber.trim())) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Phone number is already registered.") }
                    return@launch
                }

                val hashResult = passwordEncoder.hash(state.password)
                val newUser = User(
                    fullName = state.fullName.trim(),
                    username = state.username.trim(),
                    passwordHash = hashResult.hash,
                    passwordSalt = hashResult.salt,
                    passwordIterations = hashResult.iterations,
                    email = if (useEmail) state.email.trim() else "",
                    phoneNb = if (useEmail) "" else state.phoneNumber.trim(),
                    isOrganizer = state.isOrganizer
                )

                userRepository.create(newUser)
                _uiState.update {
                    it.copy(
                        password = "",
                        confirmPassword = "",
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Sign up failed", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = userFriendlyError(exception)
                    )
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    private fun validate(state: SignUpUiState, useEmail: Boolean): String? {
        if (state.fullName.isBlank()) return "Full name is required."
        if (state.username.isBlank()) return "Username is required."
        if (state.username.length < 4) return "Username must be at least 4 characters long."

        if (useEmail) {
            if (state.email.isBlank()) return "Email is required."
            if (!EMAIL_REGEX.matches(state.email.trim())) return "Please enter a valid email address."
        } else {
            if (state.phoneNumber.isBlank()) return "Phone number is required."
            if (!PHONE_REGEX.matches(state.phoneNumber.trim())) return "Please enter a valid phone number."
        }

        if (state.password != state.confirmPassword) return "Passwords do not match."

        val passwordError = validatePasswordStrength(state.password)
        return passwordError
    }

    private fun validatePasswordStrength(password: String): String? {
        if (password.length < 12) return "Password must be at least 12 characters long."
        if (!password.any { it.isUpperCase() }) return "Password must include an uppercase letter."
        if (!password.any { it.isLowerCase() }) return "Password must include a lowercase letter."
        if (!password.any { it.isDigit() }) return "Password must include a number."
        if (!password.any { !it.isLetterOrDigit() }) return "Password must include a special character."
        return null
    }

    private fun userFriendlyError(exception: Exception): String {
        return when {
            exception is UnknownHostException || exception.cause is UnknownHostException -> {
                "No internet connection. Please connect and try again."
            }
            exception is DatabaseException -> {
                "Database write failed. Please check Firebase rules and try again."
            }
            exception.message?.contains("PBKDF2", ignoreCase = true) == true -> {
                "Device security provider is not supported for password hashing."
            }
            else -> "Could not complete sign up."
        }
    }

    private companion object {
        const val TAG = "SignUpViewModel"
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}$")
    }
}
