package com.spinachtesters.spinachbooking.data.notifications

import com.spinachtesters.spinachbooking.domain.models.Event
import java.time.format.DateTimeFormatter

object NotificationActions {
    const val BOOKING_REGISTERED = "booking_registered"
    const val BOOKING_CANCELLED = "booking_cancelled"
}

data class NotificationRequest(
    val phoneNumber: String,
    val email: String,
    val event: Event,
    val action: String
)

interface NotificationStrategy {
    fun canSend(request: NotificationRequest): Boolean
    suspend fun send(request: NotificationRequest)

    fun buildMessage(request: NotificationRequest): String {
        val eventDate = request.event.date.format(EVENT_DATE_FORMATTER)
        val eventTime = request.event.startTime.format(EVENT_TIME_FORMATTER)
        val eventTitle = request.event.title

        return when (request.action) {
            NotificationActions.BOOKING_CANCELLED -> {
                "Your booking has been canceled. Title: \"$eventTitle\", Date: $eventDate, Time: $eventTime."
            }

            NotificationActions.BOOKING_REGISTERED -> {
                "You are registered for this event. Title: \"$eventTitle\", Date: $eventDate, Time: $eventTime."
            }

            else -> {
                "Booking update. Title: \"$eventTitle\", Date: $eventDate, Time: $eventTime."
            }
        }
    }

    private companion object {
        val EVENT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val EVENT_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
