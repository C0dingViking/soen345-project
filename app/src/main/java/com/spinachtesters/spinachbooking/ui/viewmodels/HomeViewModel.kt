package com.spinachtesters.spinachbooking.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class HomeUiState(
    val events: List<Event> = emptyList(),
    val upcomingBookings: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val eventRepository: EventRepository = EventRepository(),
    private val bookingRepository: BookingRepository = BookingRepository(),
    private val sessionManager: SessionManager = SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val allEvents = eventRepository.getAll()
                val upcomingBookings = getUpcomingBookingsForUser(allEvents)

                _uiState.update {
                    it.copy(
                        events = allEvents,
                        upcomingBookings = upcomingBookings,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not load home data."
                    )
                }
            }
        }
    }

    private suspend fun getUpcomingBookingsForUser(
        allEvents: List<Event>
    ): List<Event> {
        val currentUserId = sessionManager.currentUserId
        if (currentUserId.isBlank()) return emptyList()

        val eventById = allEvents.associateBy { it.id }
        val now = LocalDateTime.now()

        return bookingRepository.getAll()
            .asSequence()
            .filter { it.bookedBy == currentUserId }
            .mapNotNull { booking -> eventById[booking.bookedFor] }
            .filter { event -> !event.startTime.isBefore(now) }
            .distinctBy { it.id }
            .sortedBy { it.startTime }
            .toList()
    }
}
