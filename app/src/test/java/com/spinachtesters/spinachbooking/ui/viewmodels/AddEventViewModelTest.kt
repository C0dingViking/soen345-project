package com.spinachtesters.spinachbooking.ui.viewmodels

import android.util.Log
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.testutils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.Runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class AddEventValidationTests {
    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mockk(relaxed = true)

    private lateinit var viewModel: AddEventViewModel

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0

        val tomorrow = LocalDate.now().plusDays(1)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d")

        viewModel = AddEventViewModel(eventRepository)

        viewModel.onEventNameChanged("Concert Night")
        viewModel.onTicketPriceChanged("25.00")
        viewModel.onEventTypeChanged("Concert")
        viewModel.onEventLocationChanged("Montreal")
        viewModel.onEventDateChanged(tomorrow.format(dateFormatter))
        viewModel.onTimeStartChanged("18:00")
        viewModel.onTimeEndChanged("20:00")
        viewModel.onConcertArtistChanged("Artist")
        viewModel.onConcertGenreChanged("Rock")
    }

    @Test
    @DisplayName("addEvent() sets error when validation fails")
    fun addEventValidationFailsSetsError() = runTest {
        viewModel.onEventNameChanged("") // invalid

        viewModel.addEvent()

        assertEquals("Event Name is required.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 0) { eventRepository.create(any()) }
    }

    @Test
    @DisplayName("addEvent() creates event and sets success state")
    fun addEventValidInputCreatesEventAndSetsSuccess() = runTest {
        coEvery { eventRepository.create(any()) } answers {
            firstArg<Event>()
        }

        viewModel.addEvent()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)

        coVerify(exactly = 1) { eventRepository.create(any()) }
    }

    @Test
    @DisplayName("addEvent() handles repository failure and sets error")
    fun addEventRepositoryThrowsSetsError() = runTest {
        coEvery { eventRepository.create(any()) } throws RuntimeException("DB error")

        viewModel.addEvent()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Failed to create new event.", state.errorMessage)
        assertFalse(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    @DisplayName("addEvent() correctly calculates duration in minutes")
    fun addEventDurationCalculatedCorrectly() = runTest {
        viewModel.onEventTypeChanged("Film")
        viewModel.onFilmDirectorChanged("Director")
        viewModel.onFilmRatingChanged("5")
        viewModel.onFilmGenreChanged("Action")

        coEvery { eventRepository.create(any()) } answers {
            val event = firstArg<Event>()
            assertEquals(120, ((event.details as FilmDetails).runtimeMin))
            event
        }

        viewModel.addEvent()
    }

    @Test
    @DisplayName("addEvent() creates correct details subtype for Concert")
    fun addEventCreatesCorrectDetailsSubtype() = runTest {
        coEvery { eventRepository.create(any()) } answers {
            val event = firstArg<Event>()
            assertTrue(event.details is ConcertDetails)
            assertEquals("Artist", (event.details as ConcertDetails).mainArtist)
            event
        }

        viewModel.addEvent()
    }


    @Test
    @DisplayName("consumeSuccess() resets success flag")
    fun consumeSuccessResetsFlag() = runTest {
        coEvery { eventRepository.create(any()) } answers {
            firstArg<Event>()
        }

        viewModel.addEvent()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)

        viewModel.consumeSuccess()
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    @DisplayName("loadEventForEditing() pre-populates fields from repository event")
    fun loadEventForEditingPrepopulatesFields() = runTest {
        val event = Event(
            id = "event-123",
            title = "Interstellar",
            date = LocalDate.of(2026, 5, 10),
            startTime = LocalDateTime.of(2026, 5, 10, 18, 0),
            endTime = LocalDateTime.of(2026, 5, 10, 20, 30),
            ticketPrice = 42.5,
            location = "Montreal",
            status = "Open",
            details = FilmDetails(
                id = "details-123",
                director = "Nolan",
                runtimeMin = 150,
                rating = 5,
                genre = "SciFi"
            )
        )

        coEvery { eventRepository.getById("event-123") } returns event

        viewModel.loadEventForEditing("event-123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Interstellar", state.eventName)
        assertEquals("42.5", state.ticketPrice)
        assertEquals("Film", state.eventType)
        assertEquals("2026-5-10", state.date)
        assertEquals("18:00", state.timeStart)
        assertEquals("20:30", state.timeEnd)
        assertEquals("Montreal", state.location)
        assertEquals("Nolan", state.filmDirector)
        assertEquals("5", state.filmRating)
        assertEquals("SciFi", state.filmGenre)
        assertNull(state.errorMessage)
    }

    @Test
    @DisplayName("updateEvent() saves modified event with existing ids")
    fun updateEventSavesWithExistingIds() = runTest {
        val event = Event(
            id = "event-321",
            title = "Old Title",
            date = LocalDate.of(2026, 6, 1),
            startTime = LocalDateTime.of(2026, 6, 1, 18, 0),
            endTime = LocalDateTime.of(2026, 6, 1, 20, 0),
            ticketPrice = 30.0,
            location = "Old Place",
            status = "BOOKED",
            details = ConcertDetails(
                id = "details-321",
                mainArtist = "Old Artist",
                genre = "Pop"
            )
        )

        coEvery { eventRepository.getById("event-321") } returns event
        coEvery { eventRepository.deleteConcreteDetailsByType(any(), any()) } just Runs
        coEvery { eventRepository.save(any(), any()) } just Runs

        viewModel.loadEventForEditing("event-321")
        advanceUntilIdle()

        viewModel.onEventNameChanged("New Title")
        viewModel.onTicketPriceChanged("55")
        viewModel.onEventLocationChanged("Bell Centre")
        viewModel.onConcertArtistChanged("New Artist")
        viewModel.onConcertGenreChanged("Rock")

        viewModel.updateEvent()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            eventRepository.save(
                "event-321",
                match {
                    it.id == "event-321" &&
                        it.title == "New Title" &&
                        it.status == "BOOKED" &&
                        it.details.id == "details-321" &&
                        (it.details as ConcertDetails).mainArtist == "New Artist"
                }
            )
        }
        coVerify(exactly = 0) { eventRepository.deleteConcreteDetailsByType(any(), any()) }
    }

    @Test
    @DisplayName("updateEvent() deletes old concrete details when event type changes")
    fun updateEventDeletesOldConcreteDetailsWhenTypeChanges() = runTest {
        val event = Event(
            id = "event-654",
            title = "Old Match",
            date = LocalDate.of(2026, 7, 1),
            startTime = LocalDateTime.of(2026, 7, 1, 18, 0),
            endTime = LocalDateTime.of(2026, 7, 1, 20, 0),
            ticketPrice = 20.0,
            location = "Montreal",
            status = "Open",
            details = SportDetails(
                id = "details-654",
                sportType = "Hockey",
                homeTeam = "Habs",
                visitingTeam = "Rangers",
                league = "NHL"
            )
        )

        coEvery { eventRepository.getById("event-654") } returns event
        coEvery { eventRepository.deleteConcreteDetailsByType(any(), any()) } just Runs
        coEvery { eventRepository.save(any(), any()) } just Runs

        viewModel.loadEventForEditing("event-654")
        advanceUntilIdle()

        viewModel.onEventTypeChanged("Concert")
        viewModel.onConcertArtistChanged("Coldplay")
        viewModel.onConcertGenreChanged("Rock")

        viewModel.updateEvent()
        advanceUntilIdle()

        coVerify(exactly = 1) { eventRepository.deleteConcreteDetailsByType("details-654", "sport") }
        coVerify(exactly = 1) {
            eventRepository.save(
                "event-654",
                match { it.details is ConcertDetails && it.details.id == "details-654" }
            )
        }
    }

    @Test
    @DisplayName("loadEventForEditing() sets error when event id does not exist")
    fun loadEventForEditingMissingEventSetsError() = runTest {
        coEvery { eventRepository.getById("missing") } returns null

        viewModel.loadEventForEditing("missing")
        advanceUntilIdle()

        assertEquals("Event not found.", viewModel.uiState.value.errorMessage)
    }

}
