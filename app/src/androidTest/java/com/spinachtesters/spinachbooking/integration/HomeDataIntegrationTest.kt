package com.spinachtesters.spinachbooking.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.data.security.Pbkdf2PasswordEncoder
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.Booking
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.domain.models.User
import com.spinachtesters.spinachbooking.ui.viewmodels.EventFilter
import com.spinachtesters.spinachbooking.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class HomeDataIntegrationTest {

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
                fullName = "Home Test User",
                username = "it_home_$stamp",
                passwordHash = hashResult.hash,
                passwordSalt = hashResult.salt,
                passwordIterations = hashResult.iterations,
                email = "it_home_$stamp@test.com"
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
        price: Double = 25.0,
        location: String = "Montreal",
        status: String = "Open",
        daysFromNow: Long = 3,
        details: com.spinachtesters.spinachbooking.domain.models.EventDetails =
            ConcertDetails(mainArtist = "Artist", genre = "Rock")
    ): Event {
        val event = eventRepository.create(
            Event(
                title = "it_${titleSuffix}_$stamp",
                date = LocalDate.now().plusDays(daysFromNow),
                startTime = LocalDateTime.now().plusDays(daysFromNow).withHour(startHour).withMinute(0).withSecond(0).withNano(0),
                endTime = LocalDateTime.now().plusDays(daysFromNow).withHour(endHour).withMinute(0).withSecond(0).withNano(0),
                ticketPrice = price,
                location = location,
                status = status,
                details = details
            )
        )
        createdEventIds.add(event.id)
        return event
    }

    private suspend fun createBooking(eventId: String): String {
        val bookingId = "$userId-$eventId"
        bookingRepository.save(
            bookingId,
            Booking(
                bookedBy = userId,
                bookedFor = eventId,
                dateOfBooking = LocalDate.now(),
                status = "ACTIVE"
            )
        )
        createdBookingIds.add(bookingId)
        return bookingId
    }

    @Test
    fun loadHomeData_loadsEventsFromFirebase() = runBlocking {
        val event = createEvent("homeload", 18, 20)

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        assertNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.events.any { it.id == event.id })
    }

    @Test
    fun loadHomeData_showsUpcomingBookingsForCurrentUser() = runBlocking {
        val event = createEvent("homebooking", 18, 20)
        createBooking(event.id)

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        assertNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.upcomingBookings.any { it.id == event.id })
    }

    @Test
    fun loadHomeData_cancelledBooking_notInUpcoming() = runBlocking {
        val event = createEvent("homecancelled", 18, 20)
        val bookingId = "$userId-${event.id}"
        bookingRepository.save(
            bookingId,
            Booking(
                bookedBy = userId,
                bookedFor = event.id,
                dateOfBooking = LocalDate.now(),
                status = "CANCELLED"
            )
        )
        createdBookingIds.add(bookingId)

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        assertTrue(vm.uiState.value.upcomingBookings.none { it.id == event.id })
    }

    @Test
    fun loadHomeData_pastBooking_notInUpcoming() = runBlocking {
        // Create event in the past
        val event = createEvent("homepast", 10, 12, daysFromNow = -2)
        createBooking(event.id)

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        assertTrue(vm.uiState.value.upcomingBookings.none { it.id == event.id })
    }

    @Test
    fun filterEvents_byLocation_returnsMatchingEvents() = runBlocking {
        val montrealEvent = createEvent("filterloc1", 18, 20, location = "Montreal")
        val torontoEvent = createEvent("filterloc2", 18, 20, location = "Toronto")

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        vm.filterEvents(EventFilter(location = "Montreal"))

        assertTrue(vm.uiState.value.isFilterActive)
        assertTrue(vm.uiState.value.filteredEvents.any { it.id == montrealEvent.id })
        assertTrue(vm.uiState.value.filteredEvents.none { it.id == torontoEvent.id })
    }

    @Test
    fun filterEvents_byPriceRange_returnsMatchingEvents() = runBlocking {
        val cheapEvent = createEvent("filterprice1", 18, 20, price = 10.0)
        val expensiveEvent = createEvent("filterprice2", 18, 20, price = 100.0)

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        vm.filterEvents(EventFilter(minPrice = 5.0, maxPrice = 50.0, isOpenOnly = false))

        assertTrue(vm.uiState.value.filteredEvents.any { it.id == cheapEvent.id })
        assertTrue(vm.uiState.value.filteredEvents.none { it.id == expensiveEvent.id })
    }

    @Test
    fun filterEvents_byEventType_returnsMatchingEvents() = runBlocking {
        val concertEvent = createEvent("filtertype1", 18, 20,
            details = ConcertDetails(mainArtist = "Band", genre = "Jazz"))
        val sportEvent = createEvent("filtertype2", 18, 20,
            details = SportDetails(sportType = "Hockey", homeTeam = "A", visitingTeam = "B", league = "NHL"))

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        vm.filterEvents(EventFilter(eventType = "concert", isOpenOnly = false))

        assertTrue(vm.uiState.value.filteredEvents.any { it.id == concertEvent.id })
        assertTrue(vm.uiState.value.filteredEvents.none { it.id == sportEvent.id })
    }

    @Test
    fun filterEvents_byOpenStatus_excludesClosedEvents() = runBlocking {
        val openEvent = createEvent("filterstatus1", 18, 20, status = "Open")
        val closedEvent = createEvent("filterstatus2", 18, 20, status = "Closed")

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        vm.filterEvents(EventFilter(isOpenOnly = true))

        assertTrue(vm.uiState.value.filteredEvents.any { it.id == openEvent.id })
        assertTrue(vm.uiState.value.filteredEvents.none { it.id == closedEvent.id })
    }

    @Test
    fun clearFilteredEvents_resetsFilterState() = runBlocking {
        createEvent("filterclear", 18, 20)

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        vm.filterEvents(EventFilter(location = "Montreal"))
        assertTrue(vm.uiState.value.isFilterActive)

        vm.clearFilteredEvents()
        assertFalse(vm.uiState.value.isFilterActive)
        assertTrue(vm.uiState.value.filteredEvents.isEmpty())
    }

    @Test
    fun filterEvents_byTitle_returnsMatchingEvents() = runBlocking {
        val event = createEvent("filtertitle", 18, 20)

        val vm = HomeViewModel(eventRepository, bookingRepository, SessionManager)
        vm.loadHomeData()
        waitUntil("home data loaded") {
            !vm.uiState.value.isLoading && vm.uiState.value.events.isNotEmpty()
        }

        vm.filterEvents(EventFilter(title = "filtertitle", isOpenOnly = false))

        assertTrue(vm.uiState.value.filteredEvents.any { it.id == event.id })
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
