package com.spinachtesters.spinachbooking.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.ui.viewmodels.AddEventViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ModifyEventIntegrationTest {

    private val eventRepository = EventRepository()

    @Test
    fun updateEvent_changeTypeConcertToFilm_persistsFilmDetailsInRepository() = runBlocking {
        val stamp = System.currentTimeMillis().toString().takeLast(7)
        val originalTitle = "it_concert_$stamp"
        val updatedTitle = "it_film_$stamp"

        val initialEvent = eventRepository.create(
            Event(
                title = originalTitle,
                date = LocalDate.now().plusDays(2),
                startTime = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0),
                endTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0),
                ticketPrice = 25.0,
                location = "Montreal",
                status = "Open",
                details = ConcertDetails(mainArtist = "Coldplay", genre = "Rock")
            )
        )

        try {
            val viewModel = AddEventViewModel(eventRepository)

            viewModel.loadEventForEditing(initialEvent.id)
            waitUntil("event loaded") {
                viewModel.uiState.value.eventName == originalTitle && !viewModel.uiState.value.isLoading
            }

            viewModel.onEventNameChanged(updatedTitle)
            viewModel.onTicketPriceChanged("55")
            viewModel.onEventLocationChanged("Quebec")
            viewModel.onEventTypeChanged("Film")
            viewModel.onFilmDirectorChanged("Villeneuve")
            viewModel.onFilmRatingChanged("4")
            viewModel.onFilmGenreChanged("Drama")

            viewModel.updateEvent()
            waitUntil("event updated") {
                viewModel.uiState.value.isSuccess || viewModel.uiState.value.errorMessage != null
            }

            assertEquals(null, viewModel.uiState.value.errorMessage)

            val updatedEvent = eventRepository.getById(initialEvent.id)
            assertNotNull(updatedEvent)

            updatedEvent!!
            assertEquals(updatedTitle, updatedEvent.title)
            assertEquals(55.0, updatedEvent.ticketPrice, 0.0)
            assertEquals("Quebec", updatedEvent.location)
            assertEquals("film", updatedEvent.details.detailType)
            assertTrue(updatedEvent.details is FilmDetails)

            val filmDetails = updatedEvent.details as FilmDetails
            assertEquals("Villeneuve", filmDetails.director)
            assertEquals(4, filmDetails.rating)
            assertEquals("Drama", filmDetails.genre)
        } finally {
            eventRepository.deleteById(initialEvent.id)
        }
    }

    private suspend fun waitUntil(
        reason: String,
        timeoutMillis: Long = 20_000,
        pollMillis: Long = 100,
        condition: () -> Boolean
    ) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            delay(pollMillis)
        }
        throw AssertionError("Timeout waiting for condition: $reason")
    }
}

