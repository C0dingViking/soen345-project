package com.spinachtesters.spinachbooking.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class FilterEvent(
    val name: String = "",
    val maxPrice: Double? = null,
    val minPrice: Double? = null,
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val eventType: String = "",

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
    val concertGenre: String = "",
)

data class FilterEventUiState(
    val name: String = "",
    val maxPrice: String = "",
    val minPrice: String = "",
    val date: String = "",
    val start: String = "",
    val end: String = "",
    val location: String = "",
    val eventType: String = "",

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
    val concertGenre: String = "",

    val isLoading: Boolean = false
)

class FilterEventViewModel(): ViewModel() {

    private val _uiState = MutableStateFlow(FilterEventUiState())
    val uiState: StateFlow<FilterEventUiState> = _uiState

    fun onNameChanged(value: String) = update { it.copy(name = value) }
    fun onMaxPriceChanged(value: String) = update { it.copy(maxPrice = value) }
    fun onMinPriceChanged(value: String) = update { it.copy(minPrice = value) }
    fun onEventTypeChanged(value: String) = update { it.copy(eventType = value) }
    fun onDateChanged(value: String) = update { it.copy(date = value) }
    fun onStartChanged(value: String) = update { it.copy(start = value) }
    fun onEndChanged(value: String) = update { it.copy(end = value) }
    fun onLocationChanged(value: String) = update { it.copy(location = value) }
    fun onTheaterWriterChanged(value: String) { _uiState.update { it.copy(theaterWriter = value) } }
    fun onTheaterGenreChanged(value: String) { _uiState.update { it.copy(theaterGenre = value) } }
    fun onSportTypeChanged(value: String) { _uiState.update { it.copy(sportType = value) } }
    fun onSportHomeTeamChanged(value: String) { _uiState.update { it.copy(sportHomeTeam = value) } }
    fun onSportVisitingTeamChanged(value: String) { _uiState.update { it.copy(sportVisitingTeam = value) } }
    fun onSportLeagueChanged(value: String) { _uiState.update { it.copy(sportLeague = value) } }
    fun onFilmDirectorChanged(value: String) { _uiState.update { it.copy(filmDirector = value) } }
    fun onFilmRatingChanged(value: String) { _uiState.update { it.copy(filmRating = value) } }
    fun onFilmGenreChanged(value: String) { _uiState.update { it.copy(filmGenre = value) } }
    fun onConcertArtistChanged(value: String) { _uiState.update { it.copy(concertMainArtist = value) } }
    fun onConcertGenreChanged(value: String) { _uiState.update { it.copy(concertGenre = value) } }


    private fun update(transform: (FilterEventUiState) -> FilterEventUiState) {
        _uiState.update(transform)
    }

    fun buildFilter(): FilterEvent {
        val value = _uiState.value

        return FilterEvent(
            name = value.name,
            maxPrice = value.maxPrice.toDoubleOrNull(),
            minPrice = value.minPrice.toDoubleOrNull(),
            eventType = value.eventType,
            date = value.date,
            startTime = value.start,
            endTime = value.end,
            location = value.location,
        )
    }

    fun reset() {
        _uiState.value = FilterEventUiState()
    }
}