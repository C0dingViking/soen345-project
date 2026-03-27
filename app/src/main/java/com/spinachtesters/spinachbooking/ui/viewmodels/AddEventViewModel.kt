package com.spinachtesters.spinachbooking.ui.viewmodels;

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AddEventUiState(
    val eventName: String = "",
    val ticketPrice: String = "",
    val eventType: String = "",
    val date: String = "",
    val timeStart: String = "",
    val timeEnd: String = "",
    val duration: String = "",
    val location: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,

    val theaterWriter: String = "",
    val theaterGenre: String = "",

    val sportType: String = "",
    val sportHomeTeam: String = "",
    val sportVisitingTeam: String = "",
    val sportLeague: String = "",

    val filmDirector: String = "",
    val filmRating: String = "",
    val filmGenre: String = "",

    val concertMainArtist: String = "",
    val concertGenre: String = ""
)


class AddEventViewModel(
    private val eventRepository: EventRepository = EventRepository()
): ViewModel() {
    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState: StateFlow<AddEventUiState> = _uiState.asStateFlow()

    fun onEventNameChanged(value: String) { _uiState.update { it.copy(eventName = value, errorMessage = null) } }
    fun onTicketPriceChanged(value: String) { _uiState.update { it.copy(ticketPrice = value, errorMessage = null) } }
    fun onEventTypeChanged(value: String) { _uiState.update { it.copy(eventType = value, errorMessage = null) } }
    fun onEventDateChanged(value: String) { _uiState.update { it.copy(date = value, errorMessage = null) } }
    fun onTimeStartChanged(value: String) { _uiState.update { it.copy(timeStart = value, errorMessage = null) } }
    fun onTimeEndChanged(value: String) { _uiState.update { it.copy(timeEnd = value, errorMessage = null) } }
    fun onEventLocationChanged(value: String) { _uiState.update { it.copy(location = value, errorMessage = null) } }
    fun onTheaterWriterChanged(value: String) { _uiState.update { it.copy(theaterWriter = value, errorMessage = null) } }
    fun onTheaterGenreChanged(value: String) { _uiState.update { it.copy(theaterGenre = value, errorMessage = null) } }
    fun onSportTypeChanged(value: String) { _uiState.update { it.copy(sportType = value, errorMessage = null) } }
    fun onSportHomeTeamChanged(value: String) { _uiState.update { it.copy(sportHomeTeam = value, errorMessage = null) } }
    fun onSportVisitingTeamChanged(value: String) { _uiState.update { it.copy(sportVisitingTeam = value, errorMessage = null) } }
    fun onSportLeagueChanged(value: String) { _uiState.update { it.copy(sportLeague = value, errorMessage = null) } }
    fun onFilmDirectorChanged(value: String) { _uiState.update { it.copy(filmDirector = value, errorMessage = null) } }
    fun onFilmRatingChanged(value: String) { _uiState.update { it.copy(filmRating = value, errorMessage = null) } }
    fun onFilmGenreChanged(value: String) { _uiState.update { it.copy(filmGenre = value, errorMessage = null) } }
    fun onConcertArtistChanged(value: String) { _uiState.update { it.copy(concertMainArtist = value, errorMessage = null) } }
    fun onConcertGenreChanged(value: String) { _uiState.update { it.copy(concertGenre = value, errorMessage = null) } }

    fun addEvent() {
        val state = _uiState.value
        if (state.isLoading) return

        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            try {
                val date = LocalDate.parse(state.date, DATE_FORMATTER)
                val startTime = LocalTime.parse(state.timeStart, TIME_FORMATTER)
                val endTime = LocalTime.parse(state.timeEnd, TIME_FORMATTER)
                val durationMin = Duration.between(startTime, endTime).toMinutes().toInt()

                val details = when (state.eventType) {
                    "Sports" -> SportDetails(
                        sportType = state.sportType.trim(),
                        homeTeam = state.sportHomeTeam.trim(),
                        visitingTeam = state.sportVisitingTeam.trim(),
                        league = state.sportLeague.trim()
                    )
                    "Film" -> FilmDetails(
                        director = state.filmDirector.trim(),
                        runtimeMin = durationMin,
                        rating = state.filmRating.toInt(),
                        genre = state.filmGenre.trim()
                    )
                    "Concert" -> ConcertDetails(
                        mainArtist = state.concertMainArtist.trim(),
                        genre = state.concertGenre.trim()
                    )
                    "Theater" -> TheaterDetails(
                        writer = state.theaterWriter.trim(),
                        genre = state.theaterGenre.trim(),
                        durationMin = durationMin
                    )
                    else -> null
                }

                if (details == null) throw Exception("Failed making the concrete event details.")

                val event = Event(
                    title = state.eventName.trim(),
                    date = date,
                    startTime = LocalDateTime.of(date, startTime),
                    endTime = LocalDateTime.of(date, endTime),
                    ticketPrice = state.ticketPrice.toDouble(),
                    location = state.location.trim(),
                    status = "Open",
                    details = details
                )

                eventRepository.create(event)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Add Event failed", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to create new event."
                    )
                }
            }
        }
    }

    fun validate(state: AddEventUiState): String? {
        if (state.eventName.isBlank()) return "Event Name is required."
        if (state.ticketPrice.isBlank()) return "Ticket Price is required."
        if (state.ticketPrice.toDoubleOrNull() == null) return "Ticket Price must be a properly formatted number."
        if (state.eventType.isBlank()) return "Event Type is required."
        if (state.location.isBlank()) return "Event Location is required."

        // no need to validate syntax since they use the android widgets
        if (state.date.isBlank()) return "Event Date is required."
        if (LocalDate.parse(state.date, DATE_FORMATTER).isBefore(LocalDate.now()))
            return "Event date must be after today."
        if (state.timeStart.isBlank()) return "Time Start is required."
        if (state.timeEnd.isBlank()) return "Time End is required."
        if (LocalTime.parse(state.timeEnd, TIME_FORMATTER).isBefore(LocalTime.parse(state.timeStart, TIME_FORMATTER)))
            return "Ending time must be after the start time."

        if (state.eventType == "Theater") {
            if (state.theaterWriter.isBlank()) return "Theater Writer is required."
            if (state.theaterGenre.isBlank()) return "Theater Genre is required."
        }
        else if (state.eventType == "Sports") {
            if (state.sportType.isBlank()) return "Sports Type is required."
            if (state.sportHomeTeam.isBlank()) return "Home Team is required."
            if (state.sportVisitingTeam.isBlank()) return "Visiting Team is required."
            if (state.sportLeague.isBlank()) return "Sports League is required."
        }
        else if (state.eventType == "Film") {
            if (state.filmDirector.isBlank()) return "Film Director is required."
            if (state.filmRating.isBlank()) return "Film Rating is required."
            if (state.filmRating.toIntOrNull() == null) return "Film Rating must be a whole number."
            if (state.filmGenre.isBlank()) return "Film Genre is required."
        } else if (state.eventType == "Concert") {
            if (state.concertMainArtist.isBlank()) return "Concert Artist is required."
            if (state.concertGenre.isBlank()) return "ConcertGenre is required."
        }

        return null;
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    private companion object {
        const val TAG = "AddEventViewModel"
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

}
