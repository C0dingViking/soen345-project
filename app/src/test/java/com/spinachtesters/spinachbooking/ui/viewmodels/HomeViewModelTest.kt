package com.spinachtesters.spinachbooking.ui.viewmodels

import androidx.navigation.safe.args.generator.models.Argument
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
import com.spinachtesters.spinachbooking.ui.screens.sampleEvents
import com.spinachtesters.spinachbooking.ui.viewmodels.EventFilter
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

        viewModel.filterEvents(filter)
        advanceUntilIdle()

        assertEquals(expectedEvents, viewModel.uiState.value.filteredEvents)
    }
    companion object {
        @JvmStatic
        fun argumentProvider(): Stream<Arguments> {
            val events = listOf(
                Event(
                    title = "Football Match",
                    ticketPrice = 50.0,
                    date = LocalDate.of(2026, 3, 29),
                    startTime = LocalDateTime.of(2026, 3, 29, 19, 0),
                    endTime = LocalDateTime.of(2026, 3, 29, 21, 0),
                    location = "Stadium",
                    details = SportDetails(
                        sportType = "Football",
                        homeTeam = "Team A",
                        visitingTeam = "Team B",
                        league = "NHL"
                    )
                ),
                Event(
                    title = "Basketball Game",
                    ticketPrice = 40.0,
                    date = LocalDate.of(2026, 4, 2),
                    startTime = LocalDateTime.of(2026, 4, 2, 18, 30),
                    endTime = LocalDateTime.of(2026, 4, 2, 20, 30),
                    location = "Arena",
                    details = SportDetails(
                        sportType = "Basketball",
                        homeTeam = "Team C",
                        visitingTeam = "Team D",
                        league = "NBA"
                    )
                ),
                Event(
                    title = "Avengers Screening",
                    ticketPrice = 15.0,
                    date = LocalDate.of(2026, 3, 30),
                    startTime = LocalDateTime.of(2026, 3, 30, 20, 0),
                    endTime = LocalDateTime.of(2026, 3, 30, 22, 0),
                    location = "Cinema",
                    details = FilmDetails(
                        director = "Joss Whedon",
                        runtimeMin = 143,
                        rating = 4,
                        genre = "Action"
                    )
                ),
                Event(
                    title = "Inception Screening",
                    ticketPrice = 18.0,
                    date = LocalDate.of(2026, 4, 3),
                    startTime = LocalDateTime.of(2026, 4, 3, 19, 30),
                    endTime = LocalDateTime.of(2026, 4, 3, 22, 0),
                    location = "Cinema",
                    details = FilmDetails(
                        director = "Christopher Nolan",
                        runtimeMin = 148,
                        rating = 5,
                        genre = "Sci-Fi"
                    )
                ),
                Event(
                    title = "Shakespeare Play",
                    ticketPrice = 30.0,
                    date = LocalDate.of(2026, 3, 31),
                    startTime = LocalDateTime.of(2026, 3, 31, 18, 0),
                    endTime = LocalDateTime.of(2026, 3, 31, 20, 30),
                    location = "Theater",
                    details = TheaterDetails(
                        writer = "Shakespeare",
                        genre = "Tragedy",
                        durationMin = 150
                    )
                ),
                Event(
                    title = "Modern Play",
                    ticketPrice = 25.0,
                    date = LocalDate.of(2026, 4, 4),
                    startTime = LocalDateTime.of(2026, 4, 4, 19, 0),
                    endTime = LocalDateTime.of(2026, 4, 4, 21, 0),
                    location = "City Theater",
                    details = TheaterDetails(
                        writer = "Arthur Miller",
                        genre = "Drama",
                        durationMin = 120
                    )
                ),
                Event(
                    title = "Rock Concert",
                    ticketPrice = 60.0,
                    date = LocalDate.of(2026, 4, 1),
                    startTime = LocalDateTime.of(2026, 4, 1, 21, 0),
                    endTime = LocalDateTime.of(2026, 4, 1, 23, 0),
                    location = "Arena",
                    details = ConcertDetails(
                        mainArtist = "Rock Band",
                        genre = "Rock"
                    )
                ),
                Event(
                    title = "Jazz Night",
                    ticketPrice = 35.0,
                    date = LocalDate.of(2026, 4, 5),
                    startTime = LocalDateTime.of(2026, 4, 5, 20, 0),
                    endTime = LocalDateTime.of(2026, 4, 5, 22, 0),
                    location = "Jazz Club",
                    details = ConcertDetails(
                        mainArtist = "Jazz Ensemble",
                        genre = "Jazz"
                    )
                )
            )

            return Stream.of(
                Arguments.of(EventFilter(), events),
                Arguments.of(EventFilter(title = "DNE"), emptyList<Event>())
            )
        }
    }
    private fun sampleEvents(): List<Event> {
        return listOf(
            Event(
                title = "Football Match",
                ticketPrice = 50.0,
                date = LocalDate.of(2026, 3, 29),
                startTime = LocalDateTime.of(2026, 3, 29, 19, 0),
                endTime = LocalDateTime.of(2026, 3, 29, 21, 0),
                location = "Stadium",
                details = SportDetails(
                    sportType = "Football",
                    homeTeam = "Team A",
                    visitingTeam = "Team B",
                    league = "NHL"
                )
            ),
            Event(
                title = "Basketball Game",
                ticketPrice = 40.0,
                date = LocalDate.of(2026, 4, 2),
                startTime = LocalDateTime.of(2026, 4, 2, 18, 30),
                endTime = LocalDateTime.of(2026, 4, 2, 20, 30),
                location = "Arena",
                details = SportDetails(
                    sportType = "Basketball",
                    homeTeam = "Team C",
                    visitingTeam = "Team D",
                    league = "NBA"
                )
            ),
            Event(
                title = "Avengers Screening",
                ticketPrice = 15.0,
                date = LocalDate.of(2026, 3, 30),
                startTime = LocalDateTime.of(2026, 3, 30, 20, 0),
                endTime = LocalDateTime.of(2026, 3, 30, 22, 0),
                location = "Cinema",
                details = FilmDetails(
                    director = "Joss Whedon",
                    runtimeMin = 143,
                    rating = 4,
                    genre = "Action"
                )
            ),
            Event(
                title = "Inception Screening",
                ticketPrice = 18.0,
                date = LocalDate.of(2026, 4, 3),
                startTime = LocalDateTime.of(2026, 4, 3, 19, 30),
                endTime = LocalDateTime.of(2026, 4, 3, 22, 0),
                location = "Cinema",
                details = FilmDetails(
                    director = "Christopher Nolan",
                    runtimeMin = 148,
                    rating = 5,
                    genre = "Sci-Fi"
                )
            ),
            Event(
                title = "Shakespeare Play",
                ticketPrice = 30.0,
                date = LocalDate.of(2026, 3, 31),
                startTime = LocalDateTime.of(2026, 3, 31, 18, 0),
                endTime = LocalDateTime.of(2026, 3, 31, 20, 30),
                location = "Theater",
                details = TheaterDetails(
                    writer = "Shakespeare",
                    genre = "Tragedy",
                    durationMin = 150
                )
            ),
            Event(
                title = "Modern Play",
                ticketPrice = 25.0,
                date = LocalDate.of(2026, 4, 4),
                startTime = LocalDateTime.of(2026, 4, 4, 19, 0),
                endTime = LocalDateTime.of(2026, 4, 4, 21, 0),
                location = "City Theater",
                details = TheaterDetails(
                    writer = "Arthur Miller",
                    genre = "Drama",
                    durationMin = 120
                )
            ),
            Event(
                title = "Rock Concert",
                ticketPrice = 60.0,
                date = LocalDate.of(2026, 4, 1),
                startTime = LocalDateTime.of(2026, 4, 1, 21, 0),
                endTime = LocalDateTime.of(2026, 4, 1, 23, 0),
                location = "Arena",
                details = ConcertDetails(
                    mainArtist = "Rock Band",
                    genre = "Rock"
                )
            ),
            Event(
                title = "Jazz Night",
                ticketPrice = 35.0,
                date = LocalDate.of(2026, 4, 5),
                startTime = LocalDateTime.of(2026, 4, 5, 20, 0),
                endTime = LocalDateTime.of(2026, 4, 5, 22, 0),
                location = "Jazz Club",
                details = ConcertDetails(
                    mainArtist = "Jazz Ensemble",
                    genre = "Jazz"
                )
            )
        )
    }
}
