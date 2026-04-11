package com.spinachtesters.spinachbooking.ui.viewmodels

import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.Booking
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.domain.models.User
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
import org.junit.jupiter.api.Assertions.assertTrue
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
    private val userRepository: UserRepository = mockk()

    @BeforeEach
    fun setup() {
        SessionManager.clearSession()
        coEvery { bookingRepository.getAll() } returns emptyList()
    }

    @Test
    fun loadEvent_withValidId_loadsEventAndRecognizesExistingBooking() = runTest {
        val event = sampleEvent(id = "e1")
        coEvery { eventRepository.getById("e1") } returns event
        coEvery { bookingRepository.getAll() } returns listOf(
            Booking(
                bookedBy = "u1",
                bookedFor = "e1",
                dateOfBooking = LocalDate.of(2026, 12, 1),
                status = "ACTIVE"
            )
        )
        SessionManager.startSession(userId = "u1", isOrganizer = false)

        val viewModel = createViewModel()
        viewModel.loadEvent("e1")
        advanceUntilIdle()

        assertEquals(event, viewModel.uiState.value.event)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.isBooked)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
        coVerify(exactly = 1) { eventRepository.getById("e1") }
    }

    @Test
    fun loadEvent_withBlankId_setsNotFound() = runTest {
        val viewModel = createViewModel()
        viewModel.loadEvent("   ")

        assertNull(viewModel.uiState.value.event)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Event not found.", viewModel.uiState.value.errorMessage)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun loadEvent_withMissingId_setsNotFound() = runTest {
        val viewModel = createViewModel()
        viewModel.loadEvent(null)

        assertNull(viewModel.uiState.value.event)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Event not found.", viewModel.uiState.value.errorMessage)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun loadEvent_whenRepositoryReturnsNull_setsNotFound() = runTest {
        coEvery { eventRepository.getById("e1") } returns null

        val viewModel = createViewModel()
        viewModel.loadEvent("e1")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.event)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Event not found.", viewModel.uiState.value.errorMessage)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun loadEvent_whenRepositoryFails_setsError() = runTest {
        coEvery { eventRepository.getById("e1") } throws RuntimeException("db down")

        val viewModel = createViewModel()
        viewModel.loadEvent("e1")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.event)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Could not load event.", viewModel.uiState.value.errorMessage)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun loadEvent_withBlankSessionUser_keepsBookedFalse() = runTest {
        val event = sampleEvent()
        coEvery { eventRepository.getById(event.id) } returns event
        val viewModel = createViewModel()

        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        assertEquals(event, viewModel.uiState.value.event)
        assertFalse(viewModel.uiState.value.isBooked)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun participateInEvent_withoutLoadedEvent_doesNothing() = runTest {
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        val viewModel = createViewModel()

        viewModel.participateInEvent()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.event)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun participateInEvent_withBlankSessionUser_doesNothing() = runTest {
        val event = sampleEvent()
        coEvery { eventRepository.getById(event.id) } returns event
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.participateInEvent()
        advanceUntilIdle()

        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun participateInEvent_withNonConflictingBooking_requestsConfirmation() = runTest {
        val event = sampleEvent()
        val existingEvent = sampleEvent(
            id = "existing",
            startTime = LocalDateTime.of(2026, 12, 14, 10, 0),
            endTime = LocalDateTime.of(2026, 12, 14, 11, 0)
        )
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { eventRepository.getById(event.id) } returns event
        coEvery { eventRepository.getById("existing") } returns existingEvent
        coEvery { bookingRepository.getAll() } returns listOf(
            Booking(
                bookedBy = "u1",
                bookedFor = "existing",
                dateOfBooking = LocalDate.of(2026, 12, 1),
                status = "ACTIVE"
            )
        )
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.participateInEvent()
        advanceUntilIdle()

        assertEquals(EventDetailDialogState.ConfirmBooking, viewModel.uiState.value.dialogState)
    }

    @Test
    fun participateInEvent_withConflictingBooking_setsError() = runTest {
        val event = sampleEvent()
        val existingEvent = sampleEvent(
            id = "existing",
            startTime = LocalDateTime.of(2026, 12, 14, 18, 0),
            endTime = LocalDateTime.of(2026, 12, 14, 20, 0)
        )
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { eventRepository.getById(event.id) } returns event
        coEvery { eventRepository.getById("existing") } returns existingEvent
        coEvery { bookingRepository.getAll() } returns listOf(
            Booking(
                bookedBy = "u1",
                bookedFor = "existing",
                dateOfBooking = LocalDate.of(2026, 12, 1),
                status = "ACTIVE"
            )
        )
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.participateInEvent()
        advanceUntilIdle()

        assertEquals(
            EventDetailDialogState.Error("You have a conflicting booking during this time."),
            viewModel.uiState.value.dialogState
        )
    }

    @Test
    fun participateInEvent_whenRepositoryFails_setsError() = runTest {
        val event = sampleEvent()
        coEvery { eventRepository.getById(event.id) } returns event
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { bookingRepository.getAll() } throws RuntimeException("db down")

        viewModel.participateInEvent()
        advanceUntilIdle()

        assertEquals(
            EventDetailDialogState.Error("Could not check availability."),
            viewModel.uiState.value.dialogState
        )
    }

    @Test
    fun confirmBooking_savesBookingAndNavigatesHome() = runTest {
        val event = sampleEvent()
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { userRepository.getById("u1") } returns sampleUser(phoneNb = "", email = "")
        coEvery { eventRepository.getById(event.id) } returns event
        coEvery { bookingRepository.save(any(), any()) } returns Unit
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.confirmBooking()
        advanceUntilIdle()

        coVerify(exactly = 1) { bookingRepository.save("u1-e1", any()) }
        assertTrue(viewModel.uiState.value.isBooked)
        assertTrue(viewModel.uiState.value.shouldNavigateHome)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun confirmBooking_withBlankSessionUser_doesNothing() = runTest {
        val event = sampleEvent()
        coEvery { eventRepository.getById(event.id) } returns event
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.confirmBooking()
        advanceUntilIdle()

        coVerify(exactly = 0) { bookingRepository.save(any(), any()) }
        assertFalse(viewModel.uiState.value.shouldNavigateHome)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun confirmBooking_whenUserIsMissing_setsError() = runTest {
        val event = sampleEvent()
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { userRepository.getById("u1") } returns null
        coEvery { eventRepository.getById(event.id) } returns event
        coEvery { bookingRepository.save(any(), any()) } returns Unit
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.confirmBooking()
        advanceUntilIdle()

        assertEquals(
            EventDetailDialogState.Error("Booking completed, but notification failed: Could not find user."),
            viewModel.uiState.value.dialogState
        )
        assertTrue(viewModel.uiState.value.isBooked)
        assertFalse(viewModel.uiState.value.shouldNavigateHome)
    }

    @Test
    fun confirmBooking_withNoNotificationChannelStillSucceeds() = runTest {
        val event = sampleEvent()
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { userRepository.getById("u1") } returns sampleUser(phoneNb = "", email = "")
        coEvery { eventRepository.getById(event.id) } returns event
        coEvery { bookingRepository.save(any(), any()) } returns Unit
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.confirmBooking()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isBooked)
        assertTrue(viewModel.uiState.value.shouldNavigateHome)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun confirmBooking_whenSaveFails_setsError() = runTest {
        val event = sampleEvent()
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { userRepository.getById("u1") } returns sampleUser()
        coEvery { bookingRepository.save(any(), any()) } throws RuntimeException("save failed")
        coEvery { eventRepository.getById(event.id) } returns event
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.confirmBooking()
        advanceUntilIdle()

        assertEquals(
            EventDetailDialogState.Error("Could not complete booking."),
            viewModel.uiState.value.dialogState
        )
    }

    @Test
    fun cancelBooking_withActiveBookingCancelsAndNavigatesHome() = runTest {
        val event = sampleEvent()
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { userRepository.getById("u1") } returns sampleUser(phoneNb = "", email = "")
        coEvery { bookingRepository.getAll() } returns listOf(
            Booking(
                bookedBy = "u1",
                bookedFor = "e1",
                dateOfBooking = LocalDate.of(2026, 12, 1),
                status = "ACTIVE"
            )
        )
        coEvery { eventRepository.getById(event.id) } returns event
        coEvery { bookingRepository.save(any(), any()) } returns Unit
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.cancelBooking()
        advanceUntilIdle()

        coVerify(exactly = 1) { bookingRepository.save("u1-e1", any()) }
        assertFalse(viewModel.uiState.value.isBooked)
        assertTrue(viewModel.uiState.value.shouldNavigateHome)
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun cancelBooking_withNoActiveBooking_doesNothing() = runTest {
        val event = sampleEvent()
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { eventRepository.getById(event.id) } returns event
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.cancelBooking()
        advanceUntilIdle()

        coVerify(exactly = 0) { bookingRepository.save(any(), any()) }
        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
        assertFalse(viewModel.uiState.value.shouldNavigateHome)
    }

    @Test
    fun cancelBooking_whenUserIsMissing_setsError() = runTest {
        val event = sampleEvent()
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { userRepository.getById("u1") } returns null
        coEvery { bookingRepository.getAll() } returns listOf(
            Booking(
                bookedBy = "u1",
                bookedFor = "e1",
                dateOfBooking = LocalDate.of(2026, 12, 1),
                status = "ACTIVE"
            )
        )
        coEvery { eventRepository.getById(event.id) } returns event
        coEvery { bookingRepository.save(any(), any()) } returns Unit
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.cancelBooking()
        advanceUntilIdle()

        assertEquals(
            EventDetailDialogState.Error("Cancellation completed, but notification failed: Could not find user."),
            viewModel.uiState.value.dialogState
        )
        assertFalse(viewModel.uiState.value.isBooked)
        assertFalse(viewModel.uiState.value.shouldNavigateHome)
    }

    @Test
    fun cancelBooking_whenSaveFails_setsError() = runTest {
        val event = sampleEvent()
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { userRepository.getById("u1") } returns sampleUser()
        coEvery { bookingRepository.getAll() } returns listOf(
            Booking(
                bookedBy = "u1",
                bookedFor = "e1",
                dateOfBooking = LocalDate.of(2026, 12, 1),
                status = "ACTIVE"
            )
        )
        coEvery { bookingRepository.save(any(), any()) } throws RuntimeException("save failed")
        coEvery { eventRepository.getById(event.id) } returns event
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.cancelBooking()
        advanceUntilIdle()

        assertEquals(
            EventDetailDialogState.Error("Could not cancel booking."),
            viewModel.uiState.value.dialogState
        )
    }

    @Test
    fun dismissDialog_andResetNavigation_clearUiFlags() = runTest {
        val event = sampleEvent()
        SessionManager.startSession(userId = "u1", isOrganizer = false)
        coEvery { userRepository.getById("u1") } returns sampleUser()
        coEvery { eventRepository.getById(event.id) } returns event
        coEvery { bookingRepository.save(any(), any()) } returns Unit
        val viewModel = createViewModel()
        viewModel.loadEvent(event.id)
        advanceUntilIdle()

        viewModel.requestCancelConfirmation()
        viewModel.confirmBooking()
        advanceUntilIdle()

        viewModel.resetNavigation()
        viewModel.dismissDialog()

        assertEquals(EventDetailDialogState.None, viewModel.uiState.value.dialogState)
        assertFalse(viewModel.uiState.value.shouldNavigateHome)
    }

    private fun createViewModel(): EventDetailViewModel {
        return EventDetailViewModel(
            eventRepository = eventRepository,
            bookingRepository = bookingRepository,
            userRepository = userRepository,
            sessionManager = SessionManager
        )
    }

    private fun sampleEvent(
        id: String = "e1",
        startTime: LocalDateTime = LocalDateTime.of(2026, 12, 14, 17, 0),
        endTime: LocalDateTime = LocalDateTime.of(2026, 12, 14, 19, 0)
    ): Event {
        return Event(
            id = id,
            title = "Canadiens vs. Rangers",
            date = LocalDate.of(2026, 12, 14),
            startTime = startTime,
            endTime = endTime,
            ticketPrice = 79.99,
            location = "Montreal, QC",
            status = "BOOKED",
            details = SportDetails()
        )
    }

    private fun sampleUser(
        id: String = "u1",
        phoneNb: String = "5145551234",
        email: String = "fan@example.com"
    ): User {
        return User(
            id = id,
            fullName = "Test User",
            username = "testuser",
            email = email,
            phoneNb = phoneNb,
            organizer = false
        )
    }
}
