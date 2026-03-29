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

data class EventDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EventDetailViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    fun loadEvent(eventId: String?) {
        if (eventId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    event = null,
                    isLoading = false,
                    errorMessage = "Event not found."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(event = null, isLoading = true, errorMessage = null) }

            try {
                val event = eventRepository.getById(eventId)
                if (event == null) {
                    _uiState.update {
                        it.copy(
                            event = null,
                            isLoading = false,
                            errorMessage = "Event not found."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            event = event,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        event = null,
                        isLoading = false,
                        errorMessage = "Could not load event."
                    )
                }
            }
        }
    }
}

