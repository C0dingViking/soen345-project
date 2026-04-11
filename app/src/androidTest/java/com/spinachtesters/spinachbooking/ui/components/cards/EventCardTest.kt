package com.spinachtesters.spinachbooking.ui.components.cards

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.R
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class EventCardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clickingEventCard_invokesOnClickCallback() {
        var clickCount = 0

        composeRule.setContent {
            EventCard(
                event = sampleEvent(),
                onClick = { clickCount++ }
            )
        }

        composeRule.onNodeWithTag("available_event_card").performClick()

        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }

    @Test
    fun resolveEventImageRes_mapsFilmAndCinemaToCinemaDrawable() {
        assertEquals(R.drawable.cinema, resolve("film"))
        assertEquals(R.drawable.cinema, resolve("cinema"))
        assertEquals(R.drawable.cinema, resolve("CINEMA"))
    }

    @Test
    fun resolveEventImageRes_unknownTypeFallsBackToPlaceholder() {
        assertEquals(R.drawable.placeholder, resolve("expo"))
    }

    private fun sampleEvent(): Event {
        return Event(
            id = "e1",
            title = "Canadiens vs. Rangers",
            date = LocalDate.of(2026, 12, 14),
            startTime = LocalDateTime.of(2026, 12, 14, 17, 0),
            endTime = LocalDateTime.of(2026, 12, 14, 19, 0),
            ticketPrice = 79.99,
            location = "Montreal, QC",
            status = "AVAILABLE",
            details = SportDetails()
        )
    }

    private fun resolve(detailType: String): Int {
        val method = Class
            .forName("com.spinachtesters.spinachbooking.ui.components.cards.EventCardKt")
            .getDeclaredMethod("resolveEventImageRes", String::class.java)
        method.isAccessible = true
        return method.invoke(null, detailType) as Int
    }
}
