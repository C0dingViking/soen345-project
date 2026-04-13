package com.spinachtesters.spinachbooking.ui.viewmodels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.format.DateTimeParseException

class FilterEventViewModelTest {

    @Test
    fun onFieldChanges_updatesStateAndClearsError() {
        val viewModel = FilterEventViewModel()

        viewModel.onMaxPriceChanged("1")
        viewModel.onMinPriceChanged("5")
        viewModel.buildFilterObject()
        assertTrue(viewModel.uiState.value.isError)

        viewModel.onTitleChanged("  Big Event  ")
        viewModel.onMaxPriceChanged("40.5")
        viewModel.onMinPriceChanged("10.5")
        viewModel.onEventTypeChanged("film")
        viewModel.onDateChanged("2026-04-11")
        viewModel.onStartChanged("10:00")
        viewModel.onEndChanged("11:00")
        viewModel.onLocationChanged("  Downtown  ")
        viewModel.onTheaterWriterChanged("writer")
        viewModel.onTheaterGenreChanged("genre")
        viewModel.onTheaterDurationChanged("120")
        viewModel.onSportTypeChanged("sport")
        viewModel.onSportHomeTeamChanged("home")
        viewModel.onSportVisitingTeamChanged("away")
        viewModel.onSportLeagueChanged("league")
        viewModel.onFilmDirectorChanged("director")
        viewModel.onFilmRuntimeChanged("90")
        viewModel.onFilmRatingChanged("4")
        viewModel.onFilmGenreChanged("drama")
        viewModel.onConcertArtistChanged("artist")
        viewModel.onConcertGenreChanged("jazz")
        viewModel.onIsOnlyOpenChanged(false)

        val state = viewModel.uiState.value
        assertFalse(state.isError)
        assertEquals("  Big Event  ", state.title)
        assertEquals("40.5", state.maxPrice)
        assertEquals("10.5", state.minPrice)
        assertEquals("film", state.eventType)
        assertEquals("2026-04-11", state.date)
        assertEquals("10:00", state.start)
        assertEquals("11:00", state.end)
        assertEquals("  Downtown  ", state.location)
        assertEquals("writer", state.theaterWriter)
        assertEquals("genre", state.theaterGenre)
        assertEquals("120", state.theaterDuration)
        assertEquals("sport", state.sportType)
        assertEquals("home", state.sportHomeTeam)
        assertEquals("away", state.sportVisitingTeam)
        assertEquals("league", state.sportLeague)
        assertEquals("director", state.filmDirector)
        assertEquals("90", state.filmRuntime)
        assertEquals("4", state.filmRating)
        assertEquals("drama", state.filmGenre)
        assertEquals("artist", state.concertMainArtist)
        assertEquals("jazz", state.concertGenre)
        assertFalse(state.isOpenOnly)
    }

    @Test
    fun buildFilterObject_withValidData_returnsTrimmedParsedValuesWithoutError() {
        val viewModel = FilterEventViewModel()

        viewModel.onTitleChanged("  Title  ")
        viewModel.onMaxPriceChanged("99.99")
        viewModel.onMinPriceChanged("19.99")
        viewModel.onDateChanged("2026-06-01")
        viewModel.onStartChanged("09:15")
        viewModel.onEndChanged("10:45")
        viewModel.onLocationChanged("  Montreal  ")
        viewModel.onEventTypeChanged("sport")
        viewModel.onSportTypeChanged("  football  ")
        viewModel.onFilmRuntimeChanged("100")
        viewModel.onFilmRatingChanged("5")
        viewModel.onTheaterDurationChanged("150")

        val filter = viewModel.buildFilterObject()

        assertFalse(viewModel.uiState.value.isError)
        assertEquals("Title", filter.title)
        assertEquals(99.99, filter.maxPrice)
        assertEquals(19.99, filter.minPrice)
        assertEquals("2026-06-01", filter.date)
        assertEquals("09:15", filter.startTime)
        assertEquals("10:45", filter.endTime)
        assertEquals("Montreal", filter.location)
        assertEquals("sport", filter.eventType)
        assertEquals("football", filter.sportType)
        assertEquals(100, filter.filmRuntime)
        assertEquals(5, filter.filmRating)
        assertEquals(150, filter.theaterDuration)
    }

    @Test
    fun buildFilterObject_whenPriceRangeInvalid_setsError() {
        val viewModel = FilterEventViewModel()

        viewModel.onMaxPriceChanged("5")
        viewModel.onMinPriceChanged("10")

        viewModel.buildFilterObject()

        assertTrue(viewModel.uiState.value.isError)
        assertEquals(
            "Max Price must be greater than min price",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun buildFilterObject_whenStartAfterEnd_setsError() {
        val viewModel = FilterEventViewModel()

        viewModel.onStartChanged("14:00")
        viewModel.onEndChanged("12:00")

        viewModel.buildFilterObject()

        assertTrue(viewModel.uiState.value.isError)
        assertEquals("Start time must be before the end time", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun buildFilterObject_whenTheaterDurationIsNonPositive_setsError() {
        val viewModel = FilterEventViewModel()

        viewModel.onTheaterDurationChanged("0")

        viewModel.buildFilterObject()

        assertTrue(viewModel.uiState.value.isError)
        assertEquals(
            "Duration must be a positive number greater than 0",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun buildFilterObject_whenFilmRuntimeIsNonPositive_setsError() {
        val viewModel = FilterEventViewModel()

        viewModel.onFilmRuntimeChanged("-3")

        viewModel.buildFilterObject()

        assertTrue(viewModel.uiState.value.isError)
        assertEquals(
            "Runtime must be a positive number greater than 0",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun buildFilterObject_whenFilmRatingOutOfRange_setsError() {
        val viewModel = FilterEventViewModel()

        viewModel.onFilmRatingChanged("8")

        viewModel.buildFilterObject()

        assertTrue(viewModel.uiState.value.isError)
        assertEquals(
            "Runtime must be a between 0 and 5 inclusively",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun buildFilterObject_whenNumericFieldsCannotParse_setsNullWithoutError() {
        val viewModel = FilterEventViewModel()

        viewModel.onMaxPriceChanged("abc")
        viewModel.onMinPriceChanged("def")
        viewModel.onFilmRuntimeChanged("ghi")
        viewModel.onFilmRatingChanged("jkl")
        viewModel.onTheaterDurationChanged("mno")

        val filter = viewModel.buildFilterObject()

        assertFalse(viewModel.uiState.value.isError)
        assertNull(filter.maxPrice)
        assertNull(filter.minPrice)
        assertNull(filter.filmRuntime)
        assertNull(filter.filmRating)
        assertNull(filter.theaterDuration)
    }

    @Test
    fun buildFilterObject_whenInvalidTimeFormat_throwsParseException() {
        val viewModel = FilterEventViewModel()

        viewModel.onStartChanged("invalid")
        viewModel.onEndChanged("10:00")

        assertThrows(DateTimeParseException::class.java) {
            viewModel.buildFilterObject()
        }
    }

    @Test
    fun reset_restoresDefaultState() {
        val viewModel = FilterEventViewModel()

        viewModel.onTitleChanged("Any")
        viewModel.onFilmRatingChanged("2")
        viewModel.onIsOnlyOpenChanged(false)
        viewModel.onMaxPriceChanged("1")
        viewModel.onMinPriceChanged("5")
        viewModel.buildFilterObject()

        viewModel.reset()

        assertEquals(FilterEventUiState(), viewModel.uiState.value)
    }
}

