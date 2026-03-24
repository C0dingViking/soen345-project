package com.spinachtesters.spinachbooking.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageEventsUiState(
    val events: List<Event>? = null
)

class ManageEventsViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageEventsUiState())
    val uiState: StateFlow<ManageEventsUiState> = _uiState.asStateFlow()

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(events = eventRepository.getAll()) }
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.deleteById(event.id)
            loadEvents()
        }
    }
}
