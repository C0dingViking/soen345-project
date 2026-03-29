package com.spinachtesters.spinachbooking.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SessionState(
    val userId: String = "",
    val isOrganizer: Boolean = false
) {
    val isAuthenticated: Boolean
        get() = userId.isNotBlank()
}

object SessionManager {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    val currentUserId: String
        get() = _state.value.userId

    fun startSession(userId: String, isOrganizer: Boolean) {
        _state.value = SessionState(userId = userId, isOrganizer = isOrganizer)
    }

    fun clearSession() {
        _state.value = SessionState()
    }
}

