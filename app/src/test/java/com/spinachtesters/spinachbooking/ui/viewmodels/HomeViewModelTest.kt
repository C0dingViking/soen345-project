package com.spinachtesters.spinachbooking.ui.viewmodels

import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.Booking
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.domain.models.TheaterDetails
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.stream.Stream

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

    @ParameterizedTest
    @MethodSource("argumentProvider")
    fun filterEvents_givenFilter_setsFilteredEventsAccordingToCriteria(
        filter: EventFilter,
        expectedEvents: List<Event>
    ) = runTest {
        val viewModel = HomeViewModel(eventRepository, bookingRepository)
        val sampleEvents = sampleEvents()

        coEvery { eventRepository.getAll() } returns sampleEvents
        coEvery { bookingRepository.getAll() } returns emptyList()

        viewModel.loadHomeData()
        advanceUntilIdle()

        if (filter.eventType == "film") {
            print("") // for break point
        }

        viewModel.filterEvents(filter)
        advanceUntilIdle()

        assertEquals(expectedEvents, viewModel.uiState.value.filteredEvents)
    }
    companion object {
        @JvmStatic
        fun argumentProvider(): Stream<Arguments> {
            val events: List<Event> = sampleEvents()
            val noEvents: List<Event> = emptyList()

            return Stream.of(
                Arguments.of(EventFilter(), selectEvents(events, 0,1,2,3,4,6)),
                Arguments.of(EventFilter(isOpenOnly = false), events),
                Arguments.of(EventFilter(title = "DNE"), noEvents),
                Arguments.of(EventFilter(title = "Football Match"), selectEvents(events, 0)),
                Arguments.of(EventFilter(title = "Jazz Night"), noEvents),
                Arguments.of(EventFilter(title = "Jazz Night", isOpenOnly = false), selectEvents(events, 7)),
                Arguments.of(EventFilter(maxPrice = 50.0, isOpenOnly = false), selectEvents(events, 0,1,2,3,4,5,7)),
                Arguments.of(EventFilter(minPrice = 50.0, isOpenOnly = false), selectEvents(events, 0,6)),
                Arguments.of(EventFilter(maxPrice = 50.0, minPrice = 25.0, isOpenOnly = false), selectEvents(events, 0,1,4,5,7)),
                Arguments.of(EventFilter(date = LocalDate.of(2026, 3, 29).toString()), selectEvents(events,0)),
                Arguments.of(EventFilter(startTime = LocalTime.of(18,0).toString()), selectEvents(events, 4)),
                Arguments.of(EventFilter(endTime = LocalTime.of(23,0).toString()), selectEvents(events,6)),
                Arguments.of(EventFilter(startTime = LocalTime.of(18, 25).toString(), endTime = LocalTime.of(20, 35).toString()), selectEvents(events, 1)),
                Arguments.of(EventFilter(location = "cinema"), selectEvents(events,2,3)),
                Arguments.of(EventFilter(location = "theater", isOpenOnly = false), selectEvents(events,4,5)),
                Arguments.of(EventFilter(eventType = "sport"), selectEvents(events, 0,1)),
                Arguments.of(EventFilter(eventType = "concert", isOpenOnly = false), selectEvents(events, 6,7)),
                Arguments.of(EventFilter(eventType = "theater", theaterWriter = "Shake", theaterGenre = "", theaterDuration = null), selectEvents(events, 4)),
                Arguments.of(EventFilter(eventType = "theater", theaterWriter = "", theaterGenre = "Tragedy", theaterDuration = null), selectEvents(events, 4)),
                Arguments.of(EventFilter(eventType = "theater", theaterWriter = "", theaterGenre = "", theaterDuration = 150), selectEvents(events, 4)),
                Arguments.of(EventFilter(eventType = "theater", theaterWriter = "DNE", theaterGenre = "", theaterDuration = null), noEvents),
                Arguments.of(EventFilter(eventType = "theater", theaterWriter = "", theaterGenre = "DNE", theaterDuration = null), noEvents),
                Arguments.of(EventFilter(eventType = "theater", theaterWriter = "", theaterGenre = "", theaterDuration = 999), noEvents),
                Arguments.of(EventFilter(eventType = "sport", sportType = "Basketball", sportHomeTeam = "", sportVisitingTeam = "", sportLeague = ""), selectEvents(events, 1)),
                Arguments.of(EventFilter(eventType = "sport", sportType = "", sportHomeTeam = "C", sportVisitingTeam = "", sportLeague = ""), selectEvents(events, 1)),
                Arguments.of(EventFilter(eventType = "sport", sportType = "", sportHomeTeam = "", sportVisitingTeam = "D", sportLeague = ""), selectEvents(events, 1)),
                Arguments.of(EventFilter(eventType = "sport", sportType = "", sportHomeTeam = "", sportVisitingTeam = "", sportLeague = "NBA"), selectEvents(events, 1)),
                Arguments.of(EventFilter(eventType = "sport", sportType = "DNE", sportHomeTeam = "", sportVisitingTeam = "", sportLeague = ""), noEvents),
                Arguments.of(EventFilter(eventType = "sport", sportType = "", sportHomeTeam = "DNE", sportVisitingTeam = "", sportLeague = ""), noEvents),
                Arguments.of(EventFilter(eventType = "sport", sportType = "", sportHomeTeam = "", sportVisitingTeam = "DNE", sportLeague = ""), noEvents),
                Arguments.of(EventFilter(eventType = "sport", sportType = "", sportHomeTeam = "", sportVisitingTeam = "", sportLeague = "DNE"), noEvents),
                Arguments.of(EventFilter(eventType = "film", filmDirector = "Nolan", filmRuntime = null, filmRating = null, filmGenre = ""), selectEvents(events, 3)),
                Arguments.of(EventFilter(eventType = "film", filmDirector = "", filmRuntime = 145, filmRating = null, filmGenre = ""), selectEvents(events, 2, 3)),
                Arguments.of(EventFilter(eventType = "film", filmDirector = "", filmRuntime = null, filmRating = 5, filmGenre = ""), selectEvents(events, 3)),
                Arguments.of(EventFilter(eventType = "film", filmDirector = "", filmRuntime = null, filmRating = null, filmGenre = "sci-fi"), selectEvents(events, 3)),
                Arguments.of(EventFilter(eventType = "film", filmDirector = "DNE", filmRuntime = null, filmRating = null, filmGenre = ""), noEvents),
                Arguments.of(EventFilter(eventType = "film", filmDirector = "", filmRuntime = -999, filmRating = null, filmGenre = ""), noEvents),
                Arguments.of(EventFilter(eventType = "film", filmDirector = "", filmRuntime = null, filmRating = -99, filmGenre = ""), noEvents),
                Arguments.of(EventFilter(eventType = "film", filmDirector = "", filmRuntime = null, filmRating = null, filmGenre = "DNE"), noEvents),
                Arguments.of(EventFilter(eventType = "concert", concertMainArtist = "Rock band", concertGenre = ""), selectEvents(events, 6)),
                Arguments.of(EventFilter(eventType = "concert", concertMainArtist = "", concertGenre = "rock"), selectEvents(events, 6)),
                Arguments.of(EventFilter(eventType = "concert", concertMainArtist = "DNE", concertGenre = ""), noEvents),
                Arguments.of(EventFilter(eventType = "concert", concertMainArtist = "", concertGenre = "DNE"), noEvents)
            )
        }
    }
}
// needs to be static to be usable in `companion object`
private fun sampleEvents(): List<Event> {
    return listOf(
        //0
        Event(
            title = "Football Match",
            ticketPrice = 50.0,
            date = LocalDate.of(2026, 3, 29),
            startTime = LocalDateTime.of(2026, 3, 29, 19, 0),
            endTime = LocalDateTime.of(2026, 3, 29, 21, 0),
            location = "Stadium",
            status = "Open",
            details = SportDetails(
                sportType = "Football",
                homeTeam = "Team A",
                visitingTeam = "Team B",
                league = "NHL"
            )
        ),
        //1
        Event(
            title = "Basketball Game",
            ticketPrice = 40.0,
            date = LocalDate.of(2026, 4, 2),
            startTime = LocalDateTime.of(2026, 4, 2, 18, 30),
            endTime = LocalDateTime.of(2026, 4, 2, 20, 30),
            location = "Arena",
            status = "Open",
            details = SportDetails(
                sportType = "Basketball",
                homeTeam = "Team C",
                visitingTeam = "Team D",
                league = "NBA"
            )
        ),
        //2
        Event(
            title = "Avengers Screening",
            ticketPrice = 15.0,
            date = LocalDate.of(2026, 3, 30),
            startTime = LocalDateTime.of(2026, 3, 30, 20, 0),
            endTime = LocalDateTime.of(2026, 3, 30, 22, 0),
            location = "Cinema",
            status = "Open",
            details = FilmDetails(
                director = "Joss Whedon",
                runtimeMin = 143,
                rating = 4,
                genre = "Action"
            )
        ),
        //3
        Event(
            title = "Inception Screening",
            ticketPrice = 18.0,
            date = LocalDate.of(2026, 4, 3),
            startTime = LocalDateTime.of(2026, 4, 3, 19, 30),
            endTime = LocalDateTime.of(2026, 4, 3, 22, 0),
            location = "Cinema",
            status = "Open",
            details = FilmDetails(
                director = "Christopher Nolan",
                runtimeMin = 148,
                rating = 5,
                genre = "Sci-Fi"
            )
        ),
        //4
        Event(
            title = "Shakespeare Play",
            ticketPrice = 30.0,
            date = LocalDate.of(2026, 3, 31),
            startTime = LocalDateTime.of(2026, 3, 31, 18, 0),
            endTime = LocalDateTime.of(2026, 3, 31, 20, 30),
            location = "Theater",
            status = "Open",
            details = TheaterDetails(
                writer = "Shakespeare",
                genre = "Tragedy",
                durationMin = 150
            )
        ),
        //5
        Event(
            title = "Modern Play",
            ticketPrice = 25.0,
            date = LocalDate.of(2026, 4, 4),
            startTime = LocalDateTime.of(2026, 4, 4, 19, 0),
            endTime = LocalDateTime.of(2026, 4, 4, 21, 0),
            location = "City Theater",
            status = "Closed",
            details = TheaterDetails(
                writer = "Arthur Miller",
                genre = "Drama",
                durationMin = 120
            )
        ),
        //6
        Event(
            title = "Rock Concert",
            ticketPrice = 60.0,
            date = LocalDate.of(2026, 4, 1),
            startTime = LocalDateTime.of(2026, 4, 1, 21, 0),
            endTime = LocalDateTime.of(2026, 4, 1, 23, 0),
            location = "Arena",
            status = "Open",
            details = ConcertDetails(
                mainArtist = "Rock Band",
                genre = "Rock n Roll"
            )
        ),
        //7
        Event(
            title = "Jazz Night",
            ticketPrice = 35.0,
            date = LocalDate.of(2026, 4, 5),
            startTime = LocalDateTime.of(2026, 4, 5, 20, 0),
            endTime = LocalDateTime.of(2026, 4, 5, 22, 0),
            location = "Jazz Club",
            status = "Closed",
            details = ConcertDetails(
                mainArtist = "Jazz Ensemble",
                genre = "Jazz"
            )
        )
    )
}

fun selectEvents(events: List<Event>, vararg indices: Int): List<Event> {
    return indices.map { events[it] }
}
