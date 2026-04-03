package com.spinachtesters.spinachbooking.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.security.Pbkdf2PasswordEncoder
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.User
import com.spinachtesters.spinachbooking.ui.viewmodels.EventDetailDialogState
import com.spinachtesters.spinachbooking.ui.viewmodels.EventDetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class BookingIntegrationTest {

    private val eventRepository = EventRepository()
    private val bookingRepository = BookingRepository()
    private val userRepository = UserRepository()
    private val createdEventIds = mutableListOf<String>()
    private val createdBookingIds = mutableListOf<String>()
    private val createdUserIds = mutableListOf<String>()
    private val stamp = System.currentTimeMillis().toString().takeLast(7)

    private lateinit var userId: String

    @Before
    fun setUp() = runBlocking {
        val encoder = Pbkdf2PasswordEncoder()
        val hashResult = encoder.hash("StrongPass123!")
        val user = userRepository.create(
            User(
                fullName = "Booking Test User",
                username = "it_booking_$stamp",
                passwordHash = hashResult.hash,
                passwordSalt = hashResult.salt,
                passwordIterations = hashResult.iterations,
                email = "it_booking_$stamp@test.com"
            )
        )
        userId = user.id
        createdUserIds.add(userId)
        SessionManager.startSession(userId = userId, isOrganizer = false)
    }

    @After
    fun tearDown() = runBlocking {
        for (id in createdBookingIds) {
            try { bookingRepository.deleteById(id) } catch (_: Exception) {}
        }
        for (id in createdEventIds) {
            try { eventRepository.deleteById(id) } catch (_: Exception) {}
        }
        for (id in createdUserIds) {
            try { userRepository.deleteById(id) } catch (_: Exception) {}
        }
        SessionManager.clearSession()
    }

    private suspend fun createEvent(
        titleSuffix: String,
        startHour: Int,
        endHour: Int,
        daysFromNow: Long = 3
    ): Event {
        val event = eventRepository.create(
            Event(
                title = "it_${titleSuffix}_$stamp",
                date = LocalDate.now().plusDays(daysFromNow),
                startTime = LocalDateTime.now().plusDays(daysFromNow).withHour(startHour).withMinute(0).withSecond(0).withNano(0),
                endTime = LocalDateTime.now().plusDays(daysFromNow).withHour(endHour).withMinute(0).withSecond(0).withNano(0),
                ticketPrice = 25.0,
                location = "Montreal",
                status = "Open",
                details = ConcertDetails(mainArtist = "Artist", genre = "Rock")
            )
        )
        createdEventIds.add(event.id)
        return event
    }

    @Test
    fun loadEvent_showsNotBookedInitially() = runBlocking {
        val event = createEvent("notbooked", 18, 20)

        val vm = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm.loadEvent(event.id)
        waitUntil("event loaded") {
            vm.uiState.value.event != null || vm.uiState.value.errorMessage != null
        }

        assertNull(vm.uiState.value.errorMessage)
        assertNotNull(vm.uiState.value.event)
        assertFalse(vm.uiState.value.isBooked)
    }

    @Test
    fun confirmBooking_persistsBookingInFirebase() = runBlocking {
        val event = createEvent("book", 18, 20)
        val bookingId = "$userId-${event.id}"
        createdBookingIds.add(bookingId)

        val vm = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm.loadEvent(event.id)
        waitUntil("event loaded") { vm.uiState.value.event != null }

        vm.participateInEvent()
        waitUntil("participate checked") {
            vm.uiState.value.dialogState is EventDetailDialogState.ConfirmBooking ||
                    vm.uiState.value.dialogState is EventDetailDialogState.Error
        }
        assertTrue(vm.uiState.value.dialogState is EventDetailDialogState.ConfirmBooking)

        vm.confirmBooking()
        waitUntil("booking confirmed") { vm.uiState.value.isBooked }

        assertTrue(vm.uiState.value.isBooked)

        val booking = bookingRepository.getById(bookingId)
        assertNotNull(booking)
        assertEquals(userId, booking!!.bookedBy)
        assertEquals(event.id, booking.bookedFor)
        assertEquals("ACTIVE", booking.status)
    }

    @Test
    fun cancelBooking_setsStatusToCancelled() = runBlocking {
        val event = createEvent("cancel", 18, 20)
        val bookingId = "$userId-${event.id}"
        createdBookingIds.add(bookingId)

        val vm = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm.loadEvent(event.id)
        waitUntil("event loaded") { vm.uiState.value.event != null }

        vm.participateInEvent()
        waitUntil("participate checked") {
            vm.uiState.value.dialogState is EventDetailDialogState.ConfirmBooking
        }
        vm.confirmBooking()
        waitUntil("booking confirmed") { vm.uiState.value.isBooked }

        // Reload to get fresh state
        val vm2 = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm2.loadEvent(event.id)
        waitUntil("event reloaded") { vm2.uiState.value.event != null }
        assertTrue(vm2.uiState.value.isBooked)

        vm2.requestCancelConfirmation()
        assertEquals(EventDetailDialogState.ConfirmCancel, vm2.uiState.value.dialogState)

        vm2.cancelBooking()
        waitUntil("booking cancelled") { !vm2.uiState.value.isBooked }

        assertFalse(vm2.uiState.value.isBooked)

        val booking = bookingRepository.getById(bookingId)
        assertNotNull(booking)
        assertEquals("CANCELLED", booking!!.status)
    }

    @Test
    fun participateInEvent_withTimeConflict_showsError() = runBlocking {
        val event1 = createEvent("conflict1", 18, 20)
        val bookingId1 = "$userId-${event1.id}"
        createdBookingIds.add(bookingId1)

        // Overlapping event: 19:00-21:00 overlaps with 18:00-20:00
        val event2 = createEvent("conflict2", 19, 21)

        // Book the first event
        val vm1 = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm1.loadEvent(event1.id)
        waitUntil("event1 loaded") { vm1.uiState.value.event != null }
        vm1.participateInEvent()
        waitUntil("participate1 checked") {
            vm1.uiState.value.dialogState is EventDetailDialogState.ConfirmBooking
        }
        vm1.confirmBooking()
        waitUntil("booking1 confirmed") { vm1.uiState.value.isBooked }

        // Try to book the overlapping event
        val vm2 = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm2.loadEvent(event2.id)
        waitUntil("event2 loaded") { vm2.uiState.value.event != null }
        vm2.participateInEvent()
        waitUntil("participate2 checked") {
            vm2.uiState.value.dialogState is EventDetailDialogState.ConfirmBooking ||
                    vm2.uiState.value.dialogState is EventDetailDialogState.Error
        }

        assertTrue(vm2.uiState.value.dialogState is EventDetailDialogState.Error)
        val errorState = vm2.uiState.value.dialogState as EventDetailDialogState.Error
        assertTrue(errorState.message.contains("conflicting"))
    }

    @Test
    fun participateInEvent_noConflict_allowsBooking() = runBlocking {
        val event1 = createEvent("noconflict1", 10, 12)
        val bookingId1 = "$userId-${event1.id}"
        createdBookingIds.add(bookingId1)

        // Non-overlapping event: 14:00-16:00
        val event2 = createEvent("noconflict2", 14, 16)
        val bookingId2 = "$userId-${event2.id}"
        createdBookingIds.add(bookingId2)

        // Book the first event
        val vm1 = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm1.loadEvent(event1.id)
        waitUntil("event1 loaded") { vm1.uiState.value.event != null }
        vm1.participateInEvent()
        waitUntil("participate1 checked") {
            vm1.uiState.value.dialogState is EventDetailDialogState.ConfirmBooking
        }
        vm1.confirmBooking()
        waitUntil("booking1 confirmed") { vm1.uiState.value.isBooked }

        // Book the non-overlapping event
        val vm2 = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm2.loadEvent(event2.id)
        waitUntil("event2 loaded") { vm2.uiState.value.event != null }
        vm2.participateInEvent()
        waitUntil("participate2 checked") {
            vm2.uiState.value.dialogState is EventDetailDialogState.ConfirmBooking ||
                    vm2.uiState.value.dialogState is EventDetailDialogState.Error
        }

        assertTrue(vm2.uiState.value.dialogState is EventDetailDialogState.ConfirmBooking)
    }

    @Test
    fun loadEvent_afterBooking_showsIsBooked() = runBlocking {
        val event = createEvent("isbooked", 18, 20)
        val bookingId = "$userId-${event.id}"
        createdBookingIds.add(bookingId)

        val vm = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm.loadEvent(event.id)
        waitUntil("event loaded") { vm.uiState.value.event != null }
        vm.participateInEvent()
        waitUntil("participate checked") {
            vm.uiState.value.dialogState is EventDetailDialogState.ConfirmBooking
        }
        vm.confirmBooking()
        waitUntil("booking confirmed") { vm.uiState.value.isBooked }

        // Load with fresh ViewModel — should detect existing booking
        val vm2 = EventDetailViewModel(eventRepository, bookingRepository, userRepository, SessionManager)
        vm2.loadEvent(event.id)
        waitUntil("event reloaded") {
            vm2.uiState.value.event != null && !vm2.uiState.value.isLoading
        }

        assertTrue(vm2.uiState.value.isBooked)
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
