package com.spinachtesters.spinachbooking.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime

data class EventFilter(
    val title: String = "",
    val maxPrice: Double? = null,
    val minPrice: Double? = null,
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val isOpenOnly: Boolean = true,
    val eventType: String = "",

    val theaterWriter: String = "",
    val theaterGenre: String = "",
    val theaterDuration: Int? = null,

    val sportType: String = "",
    val sportHomeTeam: String = "",
    val sportVisitingTeam: String = "",
    val sportLeague: String = "",

    val filmDirector: String = "",
    val filmRuntime: Int? = null,
    val filmRating: Int? = null,
    val filmGenre: String = "",

    val concertMainArtist: String = "",
    val concertGenre: String = "",
)

data class FilterEventUiState(
    val title: String = "",
    val maxPrice: String = "",
    val minPrice: String = "",
    val date: String = "",
    val start: String = "",
    val end: String = "",
    val location: String = "",
    val isOpenOnly: Boolean = true,
    val eventType: String = "",

    val theaterWriter: String = "",
    val theaterGenre: String = "",
    val theaterDuration: String = "",

    val sportType: String = "",
    val sportHomeTeam: String = "",
    val sportVisitingTeam: String = "",
    val sportLeague: String = "",

    val filmDirector: String = "",
    val filmRuntime: String = "",
    val filmRating: String = "",
    val filmGenre: String = "",

    val concertMainArtist: String = "",
    val concertGenre: String = "",

    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

class FilterEventViewModel(): ViewModel() {

    private val _uiState = MutableStateFlow(FilterEventUiState())
    val uiState: StateFlow<FilterEventUiState> = _uiState

    fun onTitleChanged(value: String) = update { it.copy(title = value,  isError = false) }
    fun onMaxPriceChanged(value: String) = update { it.copy(maxPrice = value,  isError = false) }
    fun onMinPriceChanged(value: String) = update { it.copy(minPrice = value,  isError = false) }
    fun onEventTypeChanged(value: String) = update { it.copy(eventType = value,  isError = false) }
    fun onDateChanged(value: String) = update { it.copy(date = value,  isError = false) }
    fun onStartChanged(value: String) = update { it.copy(start = value,  isError = false) }
    fun onEndChanged(value: String) = update { it.copy(end = value,  isError = false) }
    fun onLocationChanged(value: String) = update { it.copy(location = value,  isError = false) }
    fun onTheaterWriterChanged(value: String) { _uiState.update { it.copy(theaterWriter = value,  isError = false) } }
    fun onTheaterGenreChanged(value: String) { _uiState.update { it.copy(theaterGenre = value,  isError = false) } }
    fun onTheaterDurationChanged(value: String) { _uiState.update { it.copy(theaterDuration = value,  isError = false) } }
    fun onSportTypeChanged(value: String) { _uiState.update { it.copy(sportType = value,  isError = false) } }
    fun onSportHomeTeamChanged(value: String) { _uiState.update { it.copy(sportHomeTeam = value,  isError = false) } }
    fun onSportVisitingTeamChanged(value: String) { _uiState.update { it.copy(sportVisitingTeam = value,  isError = false) } }
    fun onSportLeagueChanged(value: String) { _uiState.update { it.copy(sportLeague = value,  isError = false) } }
    fun onFilmDirectorChanged(value: String) { _uiState.update { it.copy(filmDirector = value,  isError = false) } }
    fun onFilmRuntimeChanged(value: String) { _uiState.update { it.copy(filmRuntime = value,  isError = false) } }
    fun onFilmRatingChanged(value: String) { _uiState.update { it.copy(filmRating = value,  isError = false) } }
    fun onFilmGenreChanged(value: String) { _uiState.update { it.copy(filmGenre = value,  isError = false) } }
    fun onConcertArtistChanged(value: String) { _uiState.update { it.copy(concertMainArtist = value,  isError = false) } }
    fun onConcertGenreChanged(value: String) { _uiState.update { it.copy(concertGenre = value,  isError = false) } }
    fun onIsOnlyOpenChanged(value: Boolean) { _uiState.update { it.copy(isOpenOnly = value, isError = false) }}

    private fun update(transform: (FilterEventUiState) -> FilterEventUiState) {
        _uiState.update(transform)
    }

    fun buildFilterObject(): EventFilter {
        val value = _uiState.value

        val maxPrice = value.maxPrice.toDoubleOrNull()
        val minPrice = value.minPrice.toDoubleOrNull()

        if (maxPrice != null && minPrice != null && maxPrice < minPrice) {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "Max Price must be greater than min price")
            }

        }

        val startTime = value.start
        val endTime = value.end

        if (startTime.isNotBlank() && endTime.isNotBlank() && LocalTime.parse(startTime).isAfter(LocalTime.parse(endTime))) {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "Start time must be before the end time")
            }
        }

        val theaterDuration = value.theaterDuration.toIntOrNull()

        if (theaterDuration != null && theaterDuration <= 0) {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "Duration must be a positive number greater than 0")
            }
        }

        val runtime = value.filmRuntime.toIntOrNull()

        if (runtime != null && runtime <= 0) {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "Runtime must be a positive number greater than 0")
            }
        }

        val rating = value.filmRating.toIntOrNull()

        if (rating != null && rating !in 0..5) {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "Runtime must be a between 0 and 5 inclusively")
            }
        }

        return EventFilter(
            title = value.title.trim(),
            maxPrice = maxPrice,
            minPrice = minPrice,
            date = value.date,
            startTime = startTime,
            endTime = endTime,
            location = value.location.trim(),
            isOpenOnly = value.isOpenOnly,
            eventType = value.eventType,

            theaterWriter = value.theaterWriter.trim(),
            theaterGenre = value.theaterGenre.trim(),
            theaterDuration = theaterDuration,

            sportType = value.sportType.trim(),
            sportHomeTeam = value.sportHomeTeam.trim(),
            sportVisitingTeam = value.sportVisitingTeam.trim(),
            sportLeague = value.sportLeague.trim(),

            filmDirector = value.filmDirector.trim(),
            filmRuntime = runtime,
            filmRating = rating,
            filmGenre = value.filmGenre.trim(),

            concertMainArtist = value.concertMainArtist.trim(),
            concertGenre = value.concertGenre.trim()
        )
    }

    fun reset() {
        _uiState.value = FilterEventUiState()
    }
}