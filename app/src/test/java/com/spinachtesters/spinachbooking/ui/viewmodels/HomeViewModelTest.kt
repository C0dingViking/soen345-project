package com.spinachtesters.spinachbooking.ui.viewmodels

import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.Booking
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mockk()
    private val bookingRepository: BookingRepository = mockk()

    @BeforeEach
    fun resetSession() {
        SessionManager.clearSession()
    }

    @Test
    fun loadHomeData_loadsAllEvents_andOnlyUpcomingBookingsForCurrentUser() = runTest {
        val now = LocalDateTime.now()
        val upcomingForCurrentUser = Event(
            id = "e1",
            title = "Upcoming 1",
            date = now.toLocalDate(),
            startTime = now.plusHours(2),
            endTime = now.plusHours(3),
            ticketPrice = 25.0,
            location = "Montreal",
            status = "AVAILABLE",
            details = SportDetails()
        )
        val pastForCurrentUser = Event(
            id = "e2",
            title = "Past",
            date = now.minusDays(1).toLocalDate(),
            startTime = now.minusDays(1),
            endTime = now.minusDays(1).plusHours(2),
            ticketPrice = 25.0,
            location = "Montreal",
            status = "AVAILABLE",
            details = SportDetails()
        )
        val upcomingForOtherUser = Event(
            id = "e3",
            title = "Other user",
            date = now.toLocalDate(),
            startTime = now.plusHours(4),
            endTime = now.plusHours(5),
            ticketPrice = 25.0,
            location = "Montreal",
            status = "AVAILABLE",
            details = SportDetails()
        )

        val allEvents = listOf(upcomingForCurrentUser, pastForCurrentUser, upcomingForOtherUser)
        val bookings = listOf(
            Booking(
                bookedBy = "u1",
                bookedFor = "e1",
                dateOfBooking = LocalDate.now(),
                status = "ACTIVE"
            ),
            Booking(
                bookedBy = "u1",
                bookedFor = "e2",
                dateOfBooking = LocalDate.now(),
                status = "ACTIVE"
            ),
            Booking(
                bookedBy = "u2",
                bookedFor = "e3",
                dateOfBooking = LocalDate.now(),
                status = "ACTIVE"
            ),
            Booking(
                bookedBy = "u1",
                bookedFor = "missing-event",
                dateOfBooking = LocalDate.now(),
                status = "ACTIVE"
            )
        )

        coEvery { eventRepository.getAll() } returns allEvents
        coEvery { bookingRepository.getAll() } returns bookings

        SessionManager.startSession(userId = "u1", isOrganizer = false)
        val viewModel = HomeViewModel(eventRepository, bookingRepository, SessionManager)

        viewModel.loadHomeData()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(allEvents, state.events)
        assertEquals(listOf(upcomingForCurrentUser), state.upcomingBookings)
        assertEquals(null, state.errorMessage)

        coVerify(exactly = 1) { eventRepository.getAll() }
        coVerify(exactly = 1) { bookingRepository.getAll() }
    }

    @Test
    fun loadHomeData_whenEventRepositoryFails_setsErrorState() = runTest {
        coEvery { eventRepository.getAll() } throws RuntimeException("db down")

        SessionManager.startSession(userId = "u1", isOrganizer = false)
        val viewModel = HomeViewModel(eventRepository, bookingRepository, SessionManager)

        viewModel.loadHomeData()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.events.isEmpty())
        assertTrue(state.upcomingBookings.isEmpty())
        assertEquals("Could not load home data.", state.errorMessage)
    }
}
