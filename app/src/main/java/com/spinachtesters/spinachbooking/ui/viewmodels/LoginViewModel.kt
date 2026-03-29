package com.spinachtesters.spinachbooking.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.security.Pbkdf2PasswordEncoder
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.security.PasswordEncoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val isOrganizer: Boolean = false
)

class LoginViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val passwordEncoder: PasswordEncoder = Pbkdf2PasswordEncoder(),
    private val sessionManager: SessionManager = SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChanged(value: String) {
        _uiState.update { it.copy(identifier = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.identifier.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please provide your username/email/phone and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    isAuthenticated = false
                )
            }
            sessionManager.clearSession()
            try {
                val user = userRepository.findByLoginIdentifier(state.identifier)
                if (user == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Invalid credentials."
                        )
                    }
                    return@launch
                }

                val isPasswordValid = passwordEncoder.verify(
                    plainTextPassword = state.password,
                    hash = user.passwordHash,
                    salt = user.passwordSalt,
                    iterations = user.passwordIterations
                )

                if (!isPasswordValid) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Invalid credentials."
                        )
                    }
                    return@launch
                }

                sessionManager.startSession(userId = user.id, isOrganizer = user.organizer)
                _uiState.update {
                    it.copy(
                        password = "",
                        isLoading = false,
                        isAuthenticated = true,
                        errorMessage = null,
                        isOrganizer = user.organizer
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not complete login."
                    )
                }
                sessionManager.clearSession()
            }
        }
    }

    fun consumeAuthenticationSuccess() {
        _uiState.update { it.copy(isAuthenticated = false) }
    }
}
