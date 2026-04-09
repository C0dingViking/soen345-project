package com.spinachtesters.spinachbooking.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.session.SessionManager
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.domain.models.TheaterDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class HomeUiState(
    val events: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val upcomingBookings: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFilterActive: Boolean = false
)

class HomeViewModel(
    private val eventRepository: EventRepository = EventRepository(),
    private val bookingRepository: BookingRepository = BookingRepository(),
    private val sessionManager: SessionManager = SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val allEvents = eventRepository.getAll()
                val upcomingBookings = getUpcomingBookingsForUser(allEvents)

                _uiState.update {
                    it.copy(
                        events = allEvents,
                        upcomingBookings = upcomingBookings,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not load home data."
                    )
                }
            }
        }
    }

    private suspend fun getUpcomingBookingsForUser(
        allEvents: List<Event>
    ): List<Event> {
        val currentUserId = sessionManager.currentUserId
        if (currentUserId.isBlank()) return emptyList()

        val eventById = allEvents.associateBy { it.id }
        val now = LocalDateTime.now()

        return bookingRepository.getAll()
            .asSequence()
            .filter { it.bookedBy == currentUserId && it.status == "ACTIVE" }
            .mapNotNull { booking -> eventById[booking.bookedFor] }
            .filter { event -> !event.startTime.isBefore(now) }
            .distinctBy { it.id }
            .sortedBy { it.startTime }
            .toList()
    }

    fun filterEvents(filter: EventFilter) {
        _uiState.update { it.copy(
            isFilterActive = true,
            filteredEvents = it.events.filter { event ->  isValidEvent(event, filter)}
        ) }
    }

    fun clearFilteredEvents() {
        _uiState.update { it.copy(
            isFilterActive = false,
            filteredEvents = emptyList()
        ) }
    }

    /**
     * Will determine if the provided event has the required values based on the provided filter.
     * > If the filter's attribute is null, then it will not be considered during the filtering
     */
    private fun isValidEvent(event: Event, filter: EventFilter): Boolean {
        // First check if attribute is null to ensure null attributes do not affect the filtering
        val titleMatch = filter.title.isBlank() || event.title.contains(filter.title, ignoreCase = true)
        val maxPriceMatch = filter.maxPrice == null || event.ticketPrice <= filter.maxPrice
        val minPriceMatch = filter.minPrice == null || event.ticketPrice >= filter.minPrice
        val dateMatch = filter.date.isBlank() || event.date == LocalDate.parse(filter.date)

        val filterStartTime = filter.startTime.takeIf { it.isNotBlank() }?.let {
            LocalTime.parse(it)
        }
        val startTimeMatch = filterStartTime == null || run {
            val lowerBound = filterStartTime.minusMinutes(10)
            val upperBound = filterStartTime.plusMinutes(10)

            event.startTime.toLocalTime() in lowerBound..upperBound
        }

        val filterEndTime = filter.endTime.takeIf { it.isNotBlank()}?.let {
            LocalTime.parse(it)
        }
        val endTimeMatch = filterEndTime == null || run {
            val lowerBound = filterEndTime.minusMinutes(10)
            val upperBound = filterEndTime.plusMinutes(10)

            event.endTime.toLocalTime() in lowerBound..upperBound
        }

        val locationMatch = filter.location.isBlank() || event.location.contains(filter.location, ignoreCase = true)

        val statusMatch = !filter.isOpenOnly || event.status.equals("Open", ignoreCase = true)

        val typeMatch = filter.eventType.isBlank() || filter.eventType.equals(event.details.detailType, ignoreCase = true)

        val typeSpecificMatch =  if (typeMatch) when (event.details.detailType.lowercase()) {
            "sport" -> {
                val details = event.details as? SportDetails
                details != null &&
                        (filter.sportType.isBlank() ||
                                details.sportType.contains(filter.sportType, ignoreCase = true)) &&
                        (filter.sportHomeTeam.isBlank() ||
                                details.homeTeam.contains(filter.sportHomeTeam,ignoreCase = true)) &&
                        (filter.sportVisitingTeam.isBlank() ||
                                details.visitingTeam.contains(filter.sportVisitingTeam,ignoreCase = true)) &&
                        (filter.sportLeague.isBlank() ||
                                details.league.contains(filter.sportLeague, ignoreCase = true))
            }
            "film" -> {
                val details = event.details as? FilmDetails
                details != null &&
                        (filter.filmDirector.isBlank() ||
                                details.director.contains(filter.filmDirector, ignoreCase = true)) &&
                        (filter.filmRuntime == null ||
                                details.runtimeMin in (filter.filmRuntime - 5)..(filter.filmRuntime + 5)) &&
                        (filter.filmRating == null ||
                                details.rating == filter.filmRating) &&
                        (filter.filmGenre.isBlank() ||
                                details.genre.contains(filter.filmGenre, ignoreCase = true))
            }
            "concert" -> {
                val details = event.details as? ConcertDetails
                details != null &&
                        (filter.concertMainArtist.isBlank() ||
                                details.mainArtist.contains(filter.concertMainArtist, ignoreCase = true)) &&
                        (filter.concertGenre.isBlank() ||
                                details.genre.contains(filter.concertGenre, ignoreCase = true))
            }
            "theater" -> {
                val details = event.details as? TheaterDetails
                details != null &&
                        (filter.theaterWriter.isBlank() ||
                                details.writer.contains(filter.theaterWriter, ignoreCase = true)) &&
                        (filter.theaterGenre.isBlank() ||
                                details.genre.contains(filter.theaterGenre, ignoreCase = true)) &&
                        (filter.theaterDuration == null ||
                                details.durationMin in (filter.theaterDuration - 5)..(filter.theaterDuration + 5))
            }
            else -> false //should not be reachable but present for safety
            }
            else false

        return titleMatch &&
                maxPriceMatch &&
                minPriceMatch &&
                dateMatch &&
                startTimeMatch &&
                endTimeMatch &&
                locationMatch &&
                statusMatch &&
                typeMatch &&
                typeSpecificMatch
    }
}
