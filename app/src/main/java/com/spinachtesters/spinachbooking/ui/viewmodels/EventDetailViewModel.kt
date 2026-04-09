package com.spinachtesters.spinachbooking.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinachtesters.spinachbooking.data.notifications.EmailNotificationStrategy
import com.spinachtesters.spinachbooking.data.notifications.NotificationActions
import com.spinachtesters.spinachbooking.data.notifications.NotificationRequest
import com.spinachtesters.spinachbooking.data.notifications.NotificationStrategy
import com.spinachtesters.spinachbooking.data.notifications.SmsNotificationStrategy
import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.Booking
import com.spinachtesters.spinachbooking.domain.models.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

data class EventDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isBooked: Boolean = false,
    val dialogState: EventDetailDialogState = EventDetailDialogState.None,
    val shouldNavigateHome: Boolean = false
)

sealed class EventDetailDialogState {
    object None : EventDetailDialogState()
    data class Error(val message: String) : EventDetailDialogState()
    object ConfirmBooking : EventDetailDialogState()
    object ConfirmCancel : EventDetailDialogState()
}

class EventDetailViewModel(
    private val eventRepository: EventRepository = EventRepository(),
    private val bookingRepository: BookingRepository = BookingRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val sessionManager: SessionManager = SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()
    private val notificationStrategies: List<NotificationStrategy> = listOf(
        SmsNotificationStrategy(OkHttpClient()),
        EmailNotificationStrategy(OkHttpClient())
    )

    fun loadEvent(eventId: String?) {
        if (eventId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    event = null,
                    isLoading = false,
                    errorMessage = "Event not found.",
                    dialogState = EventDetailDialogState.None
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    event = null,
                    isLoading = true,
                    errorMessage = null,
                    dialogState = EventDetailDialogState.None
                )
            }

            try {
                val event = eventRepository.getById(eventId)
                if (event == null) {
                    _uiState.update {
                        it.copy(
                            event = null,
                            isLoading = false,
                            errorMessage = "Event not found.",
                            dialogState = EventDetailDialogState.None
                        )
                    }
                } else {
                    val currentUserId = sessionManager.currentUserId
                    val isBooked = checkIfUserHasBooked(currentUserId, event.id)
                    _uiState.update {
                        it.copy(
                            event = event,
                            isLoading = false,
                            errorMessage = null,
                            isBooked = isBooked,
                            dialogState = EventDetailDialogState.None
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        event = null,
                        isLoading = false,
                        errorMessage = "Could not load event.",
                        dialogState = EventDetailDialogState.None
                    )
                }
            }
        }
    }

    fun participateInEvent() {
        val event = uiState.value.event ?: return
        val currentUserId = sessionManager.currentUserId
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            try {
                val userBookings = bookingRepository.getAll()
                    .filter { it.bookedBy == currentUserId && it.status == "ACTIVE" }

                val hasConflict = userBookings.any { existingBooking ->
                    val existingEvent = eventRepository.getById(existingBooking.bookedFor)
                    if (existingEvent != null) {
                        !(event.endTime.isBefore(existingEvent.startTime) ||
                                event.startTime.isAfter(existingEvent.endTime))
                    } else {
                        false
                    }
                }

                _uiState.update {
                    if (hasConflict) {
                        it.copy(
                            dialogState = EventDetailDialogState.Error(
                                "You have a conflicting booking during this time."
                            )
                        )
                    } else {
                        it.copy(dialogState = EventDetailDialogState.ConfirmBooking)
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        dialogState = EventDetailDialogState.Error(
                            "Could not check availability."
                        )
                    )
                }
            }
        }
    }

    fun requestCancelConfirmation() {
        _uiState.update { it.copy(dialogState = EventDetailDialogState.ConfirmCancel) }
    }

    fun confirmBooking() {
        val event = uiState.value.event ?: return
        val currentUserId = sessionManager.currentUserId
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            try {
                val bookingId = generateBookingId(currentUserId, event.id)
                val booking = Booking(
                    bookedBy = currentUserId,
                    bookedFor = event.id,
                    dateOfBooking = java.time.LocalDate.now(),
                    status = "ACTIVE"
                )
                bookingRepository.save(bookingId, booking)

                val notificationError = enqueueNotificationSafely(
                    userId = currentUserId,
                    event = event,
                    action = BOOKING_REGISTERED_ACTION
                )
                _uiState.update {
                    if (notificationError == null) {
                        it.copy(
                            isBooked = true,
                            dialogState = EventDetailDialogState.None,
                            shouldNavigateHome = true
                        )
                    } else {
                        it.copy(
                            isBooked = true,
                            shouldNavigateHome = false,
                            dialogState = EventDetailDialogState.Error(
                                "Booking completed, but notification failed: $notificationError"
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        dialogState = EventDetailDialogState.Error(
                            "Could not complete booking."
                        )
                    )
                }
            }
        }
    }

    fun cancelBooking() {
        val event = uiState.value.event ?: return
        val currentUserId = sessionManager.currentUserId
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            try {
                val userBooking = bookingRepository.getAll()
                    .firstOrNull {
                        it.bookedBy == currentUserId &&
                                it.bookedFor == event.id &&
                                it.status == "ACTIVE"
                    }

                if (userBooking != null) {
                    val updatedBooking = userBooking.copy(status = "CANCELLED")
                    val bookingId = generateBookingId(currentUserId, event.id)
                    bookingRepository.save(bookingId, updatedBooking)

                    val notificationError = enqueueNotificationSafely(
                        userId = currentUserId,
                        event = event,
                        action = BOOKING_CANCELLED_ACTION
                    )

                    _uiState.update {
                        if (notificationError == null) {
                            it.copy(
                                isBooked = false,
                                dialogState = EventDetailDialogState.None,
                                shouldNavigateHome = true
                            )
                        } else {
                            it.copy(
                                isBooked = false,
                                shouldNavigateHome = false,
                                dialogState = EventDetailDialogState.Error(
                                    "Cancellation completed, but notification failed: $notificationError"
                                )
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(dialogState = EventDetailDialogState.None)
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        dialogState = EventDetailDialogState.Error(
                            "Could not cancel booking."
                        )
                    )
                }
            }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = EventDetailDialogState.None) }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(shouldNavigateHome = false) }
    }

    private suspend fun checkIfUserHasBooked(userId: String, eventId: String): Boolean {
        if (userId.isBlank()) return false
        return bookingRepository.getAll()
            .any { it.bookedBy == userId && it.bookedFor == eventId && it.status == "ACTIVE" }
    }

    private fun generateBookingId(userId: String, eventId: String): String {
        return "$userId-$eventId"
    }

    private suspend fun enqueueNotificationSafely(
        userId: String,
        event: Event,
        action: String
    ): String? {
        val user = userRepository.getById(userId) ?: return "Could not find user."
        return runCatching {
            val request = NotificationRequest(
                phoneNumber = user.phoneNb,
                email = user.email,
                event = event,
                action = action
            )
            val strategy = notificationStrategies.firstOrNull { it.canSend(request) }
            if (strategy == null) {
                return@runCatching null
            }
            strategy.send(request)
            null
        }.getOrElse { throwable ->
            throwable.message?.takeIf { it.isNotBlank() } ?: "Unknown notification error."
        }
    }

    private companion object {
        const val BOOKING_REGISTERED_ACTION = NotificationActions.BOOKING_REGISTERED
        const val BOOKING_CANCELLED_ACTION = NotificationActions.BOOKING_CANCELLED
    }
}
