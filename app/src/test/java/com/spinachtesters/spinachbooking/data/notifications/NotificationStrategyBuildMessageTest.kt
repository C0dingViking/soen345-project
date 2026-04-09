package com.spinachtesters.spinachbooking.data.notifications

import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class NotificationStrategyBuildMessageTest {

    private val strategy = object : NotificationStrategy {
        override fun canSend(request: NotificationRequest): Boolean = true
        override suspend fun send(request: NotificationRequest) = Unit
    }

    private val event = Event(
        id = "event-1",
        title = "Gnomeo and Juliet",
        date = LocalDate.of(2026, 3, 29),
        startTime = LocalDateTime.of(2026, 3, 29, 5, 21),
        endTime = LocalDateTime.of(2026, 3, 29, 7, 0),
        ticketPrice = 10.0,
        location = "Montreal",
        status = "ACTIVE",
        details = SportDetails()
    )

    @Test
    fun buildMessage_withBookingRegisteredAction_returnsRegisteredTemplate() {
        val request = NotificationRequest(
            phoneNumber = "14384997127",
            email = "",
            event = event,
            action = NotificationActions.BOOKING_REGISTERED
        )

        val message = strategy.buildMessage(request)

        assertEquals(
            "You are registered for this event. Title: \"Gnomeo and Juliet\", Date: 29/03/2026, Time: 05:21.",
            message
        )
    }

    @Test
    fun buildMessage_withBookingCancelledAction_returnsCancelledTemplate() {
        val request = NotificationRequest(
            phoneNumber = "14384997127",
            email = "",
            event = event,
            action = NotificationActions.BOOKING_CANCELLED
        )

        val message = strategy.buildMessage(request)

        assertEquals(
            "Your booking has been canceled. Title: \"Gnomeo and Juliet\", Date: 29/03/2026, Time: 05:21.",
            message
        )
    }
}

