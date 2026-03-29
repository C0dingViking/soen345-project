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


data class  HomeUiState (
    val events: List<Event>? = null,
    val availableEvents: List<Event>? = null
)
class HomeViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadEvents() {
        viewModelScope.launch {
            val allEvents  = eventRepository.getAll()
            _uiState.update { it.copy(
                events = allEvents,
                availableEvents = allEvents.filter { event ->
                    event.status.equals("open", ignoreCase = true)
                }
            ) }

        }
    }
}