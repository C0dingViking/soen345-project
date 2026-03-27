package com.spinachtesters.spinachbooking.ui.viewmodels

import android.util.Log
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.testutils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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

}
