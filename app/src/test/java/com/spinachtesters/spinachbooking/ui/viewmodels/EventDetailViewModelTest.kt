package com.spinachtesters.spinachbooking.ui.viewmodels

import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.testutils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mockk()
    private val bookingRepository: BookingRepository = mockk()

    @BeforeEach
    fun setup() {
        SessionManager.clearSession()
        coEvery { bookingRepository.getAll() } returns emptyList()
    }

    @Test
    fun loadEvent_withValidId_loadsEvent() = runTest {
        val expected = Event(
            id = "e1",
            title = "Canadiens vs. Rangers",
            date = LocalDate.of(2026, 12, 14),
            startTime = LocalDateTime.of(2026, 12, 14, 17, 0),
            endTime = LocalDateTime.of(2026, 12, 14, 19, 0),
            ticketPrice = 79.99,
            location = "Montreal, QC",
            status = "BOOKED",
            details = SportDetails()
        )
        coEvery { eventRepository.getById("e1") } returns expected

        val viewModel = EventDetailViewModel(
            eventRepository = eventRepository,
            bookingRepository = bookingRepository,
            sessionManager = SessionManager
        )
        viewModel.loadEvent("e1")
        advanceUntilIdle()

        assertEquals(expected, viewModel.uiState.value.event)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        coVerify(exactly = 1) { eventRepository.getById("e1") }
    }

    @Test
    fun loadEvent_withMissingId_setsNotFound() = runTest {
        val viewModel = EventDetailViewModel(
            eventRepository = eventRepository,
            bookingRepository = bookingRepository,
            sessionManager = SessionManager
        )
        viewModel.loadEvent(null)

        assertNull(viewModel.uiState.value.event)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Event not found.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun loadEvent_whenRepositoryFails_setsError() = runTest {
        coEvery { eventRepository.getById("e1") } throws RuntimeException("db down")

        val viewModel = EventDetailViewModel(
            eventRepository = eventRepository,
            bookingRepository = bookingRepository,
            sessionManager = SessionManager
        )
        viewModel.loadEvent("e1")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.event)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Could not load event.", viewModel.uiState.value.errorMessage)
    }
}
