package com.spinachtesters.spinachbooking.ui.viewmodels

import android.util.Log
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.EventDetails
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.domain.models.TheaterDetails
import com.spinachtesters.spinachbooking.testutils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.Runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class AddEventValidationTests {
    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mockk(relaxed = true)

    private lateinit var viewModel: AddEventViewModel

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0

        val tomorrow = LocalDate.now().plusDays(1)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d")

        viewModel = AddEventViewModel(eventRepository)

        viewModel.onEventNameChanged("Concert Night")
        viewModel.onTicketPriceChanged("25.00")
        viewModel.onEventTypeChanged("Concert")
        viewModel.onEventLocationChanged("Montreal")
        viewModel.onEventDateChanged(tomorrow.format(dateFormatter))
        viewModel.onTimeStartChanged("18:00")
        viewModel.onTimeEndChanged("20:00")
        viewModel.onConcertArtistChanged("Artist")
        viewModel.onConcertGenreChanged("Rock")
    }

    @Test
    @DisplayName("addEvent() sets error when validation fails")
    fun addEventValidationFailsSetsError() = runTest {
        viewModel.onEventNameChanged("") // invalid

        viewModel.addEvent()

        assertEquals("Event Name is required.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 0) { eventRepository.create(any()) }
    }

    @Test
    @DisplayName("addEvent() creates event and sets success state")
    fun addEventValidInputCreatesEventAndSetsSuccess() = runTest {
        coEvery { eventRepository.create(any()) } answers {
            firstArg<Event>()
        }

        viewModel.addEvent()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)

        coVerify(exactly = 1) { eventRepository.create(any()) }
    }

    @Test
    @DisplayName("addEvent() handles repository failure and sets error")
    fun addEventRepositoryThrowsSetsError() = runTest {
        coEvery { eventRepository.create(any()) } throws RuntimeException("DB error")

        viewModel.addEvent()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Failed to create new event.", state.errorMessage)
        assertFalse(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    @DisplayName("addEvent() correctly calculates duration in minutes")
    fun addEventDurationCalculatedCorrectly() = runTest {
        viewModel.onEventTypeChanged("Film")
        viewModel.onFilmDirectorChanged("Director")
        viewModel.onFilmRatingChanged("5")
        viewModel.onFilmGenreChanged("Action")

        coEvery { eventRepository.create(any()) } answers {
            val event = firstArg<Event>()
            assertEquals(120, ((event.details as FilmDetails).runtimeMin))
            event
        }

        viewModel.addEvent()
    }

    @Test
    @DisplayName("addEvent() creates correct details subtype for Concert")
    fun addEventCreatesCorrectDetailsSubtype() = runTest {
        coEvery { eventRepository.create(any()) } answers {
            val event = firstArg<Event>()
            assertTrue(event.details is ConcertDetails)
            assertEquals("Artist", (event.details as ConcertDetails).mainArtist)
            event
        }

        viewModel.addEvent()
    }


    @Test
    @DisplayName("consumeSuccess() resets success flag")
    fun consumeSuccessResetsFlag() = runTest {
        coEvery { eventRepository.create(any()) } answers {
            firstArg<Event>()
        }

        viewModel.addEvent()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)

        viewModel.consumeSuccess()
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    @DisplayName("loadEventForEditing() pre-populates fields from repository event")
    fun loadEventForEditingPrepopulatesFields() = runTest {
        val event = Event(
            id = "event-123",
            title = "Interstellar",
            date = LocalDate.of(2026, 5, 10),
            startTime = LocalDateTime.of(2026, 5, 10, 18, 0),
            endTime = LocalDateTime.of(2026, 5, 10, 20, 30),
            ticketPrice = 42.5,
            location = "Montreal",
            status = "Open",
            details = FilmDetails(
                id = "details-123",
                director = "Nolan",
                runtimeMin = 150,
                rating = 5,
                genre = "SciFi"
            )
        )

        coEvery { eventRepository.getById("event-123") } returns event

        viewModel.loadEventForEditing("event-123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Interstellar", state.eventName)
        assertEquals("42.5", state.ticketPrice)
        assertEquals("Film", state.eventType)
        assertEquals("2026-5-10", state.date)
        assertEquals("18:00", state.timeStart)
        assertEquals("20:30", state.timeEnd)
        assertEquals("Montreal", state.location)
        assertEquals("Nolan", state.filmDirector)
        assertEquals("5", state.filmRating)
        assertEquals("SciFi", state.filmGenre)
        assertNull(state.errorMessage)
    }

    @Test
    @DisplayName("updateEvent() saves modified event with existing ids")
    fun updateEventSavesWithExistingIds() = runTest {
        val event = Event(
            id = "event-321",
            title = "Old Title",
            date = LocalDate.of(2026, 6, 1),
            startTime = LocalDateTime.of(2026, 6, 1, 18, 0),
            endTime = LocalDateTime.of(2026, 6, 1, 20, 0),
            ticketPrice = 30.0,
            location = "Old Place",
            status = "BOOKED",
            details = ConcertDetails(
                id = "details-321",
                mainArtist = "Old Artist",
                genre = "Pop"
            )
        )

        coEvery { eventRepository.getById("event-321") } returns event
        coEvery { eventRepository.deleteConcreteDetailsByType(any(), any()) } just Runs
        coEvery { eventRepository.save(any(), any()) } just Runs

        viewModel.loadEventForEditing("event-321")
        advanceUntilIdle()

        viewModel.onEventNameChanged("New Title")
        viewModel.onTicketPriceChanged("55")
        viewModel.onEventLocationChanged("Bell Centre")
        viewModel.onConcertArtistChanged("New Artist")
        viewModel.onConcertGenreChanged("Rock")

        viewModel.updateEvent()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            eventRepository.save(
                "event-321",
                match {
                    it.id == "event-321" &&
                            it.title == "New Title" &&
                            it.status == "BOOKED" &&
                            it.details.id == "details-321" &&
                            (it.details as ConcertDetails).mainArtist == "New Artist"
                }
            )
        }
        coVerify(exactly = 0) { eventRepository.deleteConcreteDetailsByType(any(), any()) }
    }

    @Test
    @DisplayName("updateEvent() deletes old concrete details when event type changes")
    fun updateEventDeletesOldConcreteDetailsWhenTypeChanges() = runTest {
        val event = Event(
            id = "event-654",
            title = "Old Match",
            date = LocalDate.of(2026, 7, 1),
            startTime = LocalDateTime.of(2026, 7, 1, 18, 0),
            endTime = LocalDateTime.of(2026, 7, 1, 20, 0),
            ticketPrice = 20.0,
            location = "Montreal",
            status = "Open",
            details = SportDetails(
                id = "details-654",
                sportType = "Hockey",
                homeTeam = "Habs",
                visitingTeam = "Rangers",
                league = "NHL"
            )
        )

        coEvery { eventRepository.getById("event-654") } returns event
        coEvery { eventRepository.deleteConcreteDetailsByType(any(), any()) } just Runs
        coEvery { eventRepository.save(any(), any()) } just Runs

        viewModel.loadEventForEditing("event-654")
        advanceUntilIdle()

        viewModel.onEventTypeChanged("Concert")
        viewModel.onConcertArtistChanged("Coldplay")
        viewModel.onConcertGenreChanged("Rock")

        viewModel.updateEvent()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            eventRepository.deleteConcreteDetailsByType(
                "details-654",
                "sport"
            )
        }
        coVerify(exactly = 1) {
            eventRepository.save(
                "event-654",
                match { it.details is ConcertDetails && it.details.id == "details-654" }
            )
        }
    }

    @Test
    @DisplayName("loadEventForEditing() sets error when event id does not exist")
    fun loadEventForEditingMissingEventSetsError() = runTest {
        coEvery { eventRepository.getById("missing") } returns null

        viewModel.loadEventForEditing("missing")
        advanceUntilIdle()

        assertEquals("Event not found.", viewModel.uiState.value.errorMessage)
    }

    @Test
    @DisplayName("validate() rejects non-numeric ticket price")
    fun validateRejectsNonNumericTicketPrice() = runTest {
        viewModel.onTicketPriceChanged("abc")

        viewModel.addEvent()

        assertEquals(
            "Ticket Price must be a properly formatted number.",
            viewModel.uiState.value.errorMessage
        )
        coVerify(exactly = 0) { eventRepository.create(any()) }
    }

    @Test
    @DisplayName("validate() requires event type")
    fun validateRequiresEventType() = runTest {
        viewModel.onEventTypeChanged("")

        viewModel.addEvent()

        assertEquals("Event Type is required.", viewModel.uiState.value.errorMessage)
    }

    @Test
    @DisplayName("validate() rejects past event date")
    fun validateRejectsPastDate() = runTest {
        val yesterday = LocalDate.now().minusDays(1)
        viewModel.onEventDateChanged(yesterday.format(DateTimeFormatter.ofPattern("yyyy-M-d")))

        viewModel.addEvent()

        assertEquals("Event date must be after today.", viewModel.uiState.value.errorMessage)
    }

    @Test
    @DisplayName("validate() rejects ending time before start")
    fun validateRejectsEndingTimeBeforeStart() = runTest {
        viewModel.onTimeStartChanged("18:00")
        viewModel.onTimeEndChanged("17:59")

        viewModel.addEvent()

        assertEquals(
            "Ending time must be after the start time.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    @DisplayName("validate() enforces theater writer")
    fun validateTheaterWriterRequired() = runTest {
        viewModel.onEventTypeChanged("Theater")
        viewModel.onTheaterWriterChanged("")
        viewModel.onTheaterGenreChanged("Drama")

        viewModel.addEvent()

        assertEquals("Theater Writer is required.", viewModel.uiState.value.errorMessage)
    }

    @Test
    @DisplayName("validate() enforces sports league")
    fun validateSportsLeagueRequired() = runTest {
        viewModel.onEventTypeChanged("Sports")
        viewModel.onSportTypeChanged("Hockey")
        viewModel.onSportHomeTeamChanged("Habs")
        viewModel.onSportVisitingTeamChanged("Leafs")
        viewModel.onSportLeagueChanged("")

        viewModel.addEvent()

        assertEquals("Sports League is required.", viewModel.uiState.value.errorMessage)
    }

    @Test
    @DisplayName("validate() enforces film rating integer")
    fun validateFilmRatingMustBeWholeNumber() = runTest {
        viewModel.onEventTypeChanged("Film")
        viewModel.onFilmDirectorChanged("Nolan")
        viewModel.onFilmRatingChanged("4.5")
        viewModel.onFilmGenreChanged("SciFi")

        viewModel.addEvent()

        assertEquals("Film Rating must be a whole number.", viewModel.uiState.value.errorMessage)
    }

    @Test
    @DisplayName("validate() malformed date throws parse exception")
    fun validateMalformedDateThrows() = runTest {
        viewModel.onEventDateChanged("bad-date")

        assertThrows(java.time.format.DateTimeParseException::class.java) {
            viewModel.validate(viewModel.uiState.value)
        }
    }

    @Test
    @DisplayName("addEvent() with unsupported type reaches details-null failure branch")
    fun addEventUnsupportedTypeSetsFailure() = runTest {
        viewModel.onEventTypeChanged("Comedy")

        viewModel.addEvent()
        advanceUntilIdle()

        assertEquals("Failed to create new event.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { eventRepository.create(any()) }
    }

    @Test
    @DisplayName("loadEventForEditing() with null or blank id does not query repository")
    fun loadEventForEditingBlankOrNullNoOp() = runTest {
        viewModel.loadEventForEditing(null)
        viewModel.loadEventForEditing("   ")
        advanceUntilIdle()

        coVerify(exactly = 0) { eventRepository.getById(any()) }
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    @DisplayName("loadEventForEditing() skips duplicate load for already loaded event")
    fun loadEventForEditingSkipsWhenAlreadyLoaded() = runTest {
        val event = Event(
            id = "event-same",
            title = "Same Event",
            date = LocalDate.of(2026, 8, 1),
            startTime = LocalDateTime.of(2026, 8, 1, 10, 0),
            endTime = LocalDateTime.of(2026, 8, 1, 12, 0),
            ticketPrice = 10.0,
            location = "Montreal",
            status = "Open",
            details = ConcertDetails(id = "details-same", mainArtist = "A", genre = "Rock")
        )
        coEvery { eventRepository.getById("event-same") } returns event

        viewModel.loadEventForEditing("event-same")
        advanceUntilIdle()
        viewModel.loadEventForEditing("event-same")
        advanceUntilIdle()

        coVerify(exactly = 1) { eventRepository.getById("event-same") }
    }

    @Test
    @DisplayName("loadEventForEditing() handles repository exception")
    fun loadEventForEditingRepositoryThrowsSetsError() = runTest {
        coEvery { eventRepository.getById("boom") } throws RuntimeException("db down")

        viewModel.loadEventForEditing("boom")
        advanceUntilIdle()

        assertEquals("Failed to load event.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    @DisplayName("loadEventForEditing() maps sport details to ui state")
    fun loadEventForEditingMapsSportDetails() = runTest {
        val event = Event(
            id = "event-sport",
            title = "Hockey Night",
            date = LocalDate.of(2026, 9, 1),
            startTime = LocalDateTime.of(2026, 9, 1, 19, 0),
            endTime = LocalDateTime.of(2026, 9, 1, 21, 0),
            ticketPrice = 20.0,
            location = "Bell Centre",
            status = "Open",
            details = SportDetails(
                id = "details-sport",
                sportType = "Hockey",
                homeTeam = "Habs",
                visitingTeam = "Leafs",
                league = "NHL"
            )
        )
        coEvery { eventRepository.getById("event-sport") } returns event

        viewModel.loadEventForEditing("event-sport")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Sports", state.eventType)
        assertEquals("Hockey", state.sportType)
        assertEquals("Habs", state.sportHomeTeam)
        assertEquals("Leafs", state.sportVisitingTeam)
        assertEquals("NHL", state.sportLeague)
    }

    @Test
    @DisplayName("loadEventForEditing() maps theater details to ui state")
    fun loadEventForEditingMapsTheaterDetails() = runTest {
        val event = Event(
            id = "event-theater",
            title = "Hamlet",
            date = LocalDate.of(2026, 10, 1),
            startTime = LocalDateTime.of(2026, 10, 1, 20, 0),
            endTime = LocalDateTime.of(2026, 10, 1, 22, 0),
            ticketPrice = 30.0,
            location = "Place des Arts",
            status = "Open",
            details = TheaterDetails(
                id = "details-theater",
                writer = "Shakespeare",
                genre = "Drama",
                durationMin = 120
            )
        )
        coEvery { eventRepository.getById("event-theater") } returns event

        viewModel.loadEventForEditing("event-theater")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Theater", state.eventType)
        assertEquals("Shakespeare", state.theaterWriter)
        assertEquals("Drama", state.theaterGenre)
    }

    @Test
    @DisplayName("loadEventForEditing() maps unknown details type to empty form type")
    fun loadEventForEditingMapsUnknownDetailsTypeToEmpty() = runTest {
        val unknownDetails = object : EventDetails(id = "d-unknown", detailType = "mystery") {
            override fun copyWithId(newId: String): EventDetails = this
        }
        val event = Event(
            id = "event-unknown",
            title = "Unknown",
            date = LocalDate.of(2026, 11, 1),
            startTime = LocalDateTime.of(2026, 11, 1, 9, 0),
            endTime = LocalDateTime.of(2026, 11, 1, 10, 0),
            ticketPrice = 15.0,
            location = "Somewhere",
            status = "Open",
            details = unknownDetails
        )
        coEvery { eventRepository.getById("event-unknown") } returns event

        viewModel.loadEventForEditing("event-unknown")
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.eventType)
    }

    @Test
    @DisplayName("updateEvent() returns error when nothing is loaded for editing")
    fun updateEventWithoutLoadedEventSetsError() = runTest {
        viewModel.updateEvent()

        assertEquals("Missing event to modify.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { eventRepository.save(any(), any()) }
    }

    @Test
    @DisplayName("updateEvent() validation failure prevents save")
    fun updateEventValidationFailurePreventsSave() = runTest {
        val event = Event(
            id = "event-v",
            title = "Old",
            date = LocalDate.of(2026, 12, 1),
            startTime = LocalDateTime.of(2026, 12, 1, 10, 0),
            endTime = LocalDateTime.of(2026, 12, 1, 12, 0),
            ticketPrice = 10.0,
            location = "Mtl",
            status = "Open",
            details = ConcertDetails(id = "details-v", mainArtist = "A", genre = "Rock")
        )
        coEvery { eventRepository.getById("event-v") } returns event

        viewModel.loadEventForEditing("event-v")
        advanceUntilIdle()
        viewModel.onEventNameChanged("")

        viewModel.updateEvent()

        assertEquals("Event Name is required.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { eventRepository.save(any(), any()) }
    }

    @Test
    @DisplayName("updateEvent() handles save exception")
    fun updateEventSaveThrowsSetsError() = runTest {
        val event = Event(
            id = "event-save-fail",
            title = "Old",
            date = LocalDate.of(2026, 12, 2),
            startTime = LocalDateTime.of(2026, 12, 2, 10, 0),
            endTime = LocalDateTime.of(2026, 12, 2, 12, 0),
            ticketPrice = 10.0,
            location = "Mtl",
            status = "Open",
            details = ConcertDetails(id = "details-save-fail", mainArtist = "A", genre = "Rock")
        )
        coEvery { eventRepository.getById("event-save-fail") } returns event
        coEvery { eventRepository.save(any(), any()) } throws RuntimeException("save fail")

        viewModel.loadEventForEditing("event-save-fail")
        advanceUntilIdle()

        viewModel.updateEvent()
        advanceUntilIdle()

        assertEquals("Failed to update event.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    @DisplayName("updateEvent() handles delete old details exception when changing type")
    fun updateEventDeleteOldDetailsThrowsSetsError() = runTest {
        val event = Event(
            id = "event-delete-fail",
            title = "Old",
            date = LocalDate.of(2026, 12, 3),
            startTime = LocalDateTime.of(2026, 12, 3, 10, 0),
            endTime = LocalDateTime.of(2026, 12, 3, 12, 0),
            ticketPrice = 10.0,
            location = "Mtl",
            status = "Open",
            details = SportDetails(
                id = "details-delete-fail",
                sportType = "Hockey",
                homeTeam = "A",
                visitingTeam = "B",
                league = "NHL"
            )
        )
        coEvery { eventRepository.getById("event-delete-fail") } returns event
        coEvery {
            eventRepository.deleteConcreteDetailsByType("details-delete-fail", "sport")
        } throws RuntimeException("delete fail")

        viewModel.loadEventForEditing("event-delete-fail")
        advanceUntilIdle()
        viewModel.onEventTypeChanged("Concert")
        viewModel.onConcertArtistChanged("Coldplay")
        viewModel.onConcertGenreChanged("Rock")

        viewModel.updateEvent()
        advanceUntilIdle()

        assertEquals("Failed to update event.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { eventRepository.save(any(), any()) }
    }

    @Test
    @DisplayName("updateEvent() unsupported type reaches details-null failure branch")
    fun updateEventUnsupportedTypeSetsFailure() = runTest {
        val event = Event(
            id = "event-unsupported",
            title = "Old",
            date = LocalDate.of(2026, 12, 4),
            startTime = LocalDateTime.of(2026, 12, 4, 10, 0),
            endTime = LocalDateTime.of(2026, 12, 4, 12, 0),
            ticketPrice = 10.0,
            location = "Mtl",
            status = "Open",
            details = ConcertDetails(id = "details-unsupported", mainArtist = "A", genre = "Rock")
        )
        coEvery { eventRepository.getById("event-unsupported") } returns event

        viewModel.loadEventForEditing("event-unsupported")
        advanceUntilIdle()
        viewModel.onEventTypeChanged("Comedy")

        viewModel.updateEvent()
        advanceUntilIdle()

        assertEquals("Failed to update event.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { eventRepository.save(any(), any()) }
    }

    @Test
    @DisplayName("mapDetailTypeToFormType and mapFormTypeToDetailType handle unknown values")
    fun mapTypeHelpersHandleUnknownValues() {
        assertEquals("", AddEventViewModel.mapDetailTypeToFormType("unknown"))
        assertEquals("", AddEventViewModel.mapFormTypeToDetailType("Unknown"))
    }

    @Test
    @DisplayName("validate() returns required-field errors for remaining common blanks")
    fun validateCommonBlankFieldBranches() {
        val base = viewModel.uiState.value

        val cases = listOf(
            base.copy(ticketPrice = "") to "Ticket Price is required.",
            base.copy(location = "") to "Event Location is required.",
            base.copy(date = "") to "Event Date is required.",
            base.copy(timeStart = "") to "Time Start is required.",
            base.copy(timeEnd = "") to "Time End is required."
        )

        cases.forEach { (state, expected) ->
            assertEquals(expected, viewModel.validate(state))
        }
    }

    @Test
    @DisplayName("validate() returns required-field errors for remaining type-specific blanks")
    fun validateTypeSpecificBlankFieldBranches() {
        val base = viewModel.uiState.value

        val cases = listOf(
            base.copy(
                eventType = "Theater",
                theaterWriter = "Writer",
                theaterGenre = ""
            ) to "Theater Genre is required.",
            base.copy(
                eventType = "Sports",
                sportType = "",
                sportHomeTeam = "Habs",
                sportVisitingTeam = "Leafs",
                sportLeague = "NHL"
            ) to "Sports Type is required.",
            base.copy(
                eventType = "Sports",
                sportType = "Hockey",
                sportHomeTeam = "",
                sportVisitingTeam = "Leafs",
                sportLeague = "NHL"
            ) to "Home Team is required.",
            base.copy(
                eventType = "Sports",
                sportType = "Hockey",
                sportHomeTeam = "Habs",
                sportVisitingTeam = "",
                sportLeague = "NHL"
            ) to "Visiting Team is required.",
            base.copy(
                eventType = "Film",
                filmDirector = "",
                filmRating = "5",
                filmGenre = "Drama"
            ) to "Film Director is required.",
            base.copy(
                eventType = "Film",
                filmDirector = "Nolan",
                filmRating = "",
                filmGenre = "Drama"
            ) to "Film Rating is required.",
            base.copy(
                eventType = "Film",
                filmDirector = "Nolan",
                filmRating = "5",
                filmGenre = ""
            ) to "Film Genre is required.",
            base.copy(
                eventType = "Concert",
                concertMainArtist = "",
                concertGenre = "Rock"
            ) to "Concert Artist is required.",
            base.copy(
                eventType = "Concert",
                concertMainArtist = "Artist",
                concertGenre = ""
            ) to "ConcertGenre is required."
        )

        cases.forEach { (state, expected) ->
            assertEquals(expected, viewModel.validate(state))
        }
    }

    @Test
    @DisplayName("updateEvent() builds and saves SportDetails when form type is Sports")
    fun updateEventBuildsSportDetailsBranch() = runTest {
        val event = Event(
            id = "event-sports-branch",
            title = "Old",
            date = LocalDate.of(2026, 12, 10),
            startTime = LocalDateTime.of(2026, 12, 10, 10, 0),
            endTime = LocalDateTime.of(2026, 12, 10, 12, 0),
            ticketPrice = 10.0,
            location = "Mtl",
            status = "Open",
            details = SportDetails(
                id = "details-sports-branch",
                sportType = "Old",
                homeTeam = "A",
                visitingTeam = "B",
                league = "L"
            )
        )
        coEvery { eventRepository.getById("event-sports-branch") } returns event
        coEvery { eventRepository.save(any(), any()) } just Runs

        viewModel.loadEventForEditing("event-sports-branch")
        advanceUntilIdle()
        viewModel.onEventTypeChanged("Sports")
        viewModel.onSportTypeChanged("Hockey")
        viewModel.onSportHomeTeamChanged("Habs")
        viewModel.onSportVisitingTeamChanged("Leafs")
        viewModel.onSportLeagueChanged("NHL")

        viewModel.updateEvent()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            eventRepository.save(
                "event-sports-branch",
                match {
                    it.details is SportDetails &&
                            it.details.id == "details-sports-branch" &&
                            (it.details as SportDetails).sportType == "Hockey"
                }
            )
        }
    }

    @Test
    @DisplayName("updateEvent() builds and saves FilmDetails when form type is Film")
    fun updateEventBuildsFilmDetailsBranch() = runTest {
        val event = Event(
            id = "event-film-branch",
            title = "Old",
            date = LocalDate.of(2026, 12, 11),
            startTime = LocalDateTime.of(2026, 12, 11, 10, 0),
            endTime = LocalDateTime.of(2026, 12, 11, 12, 30),
            ticketPrice = 10.0,
            location = "Mtl",
            status = "Open",
            details = FilmDetails(
                id = "details-film-branch",
                director = "Old",
                runtimeMin = 90,
                rating = 3,
                genre = "Old"
            )
        )
        coEvery { eventRepository.getById("event-film-branch") } returns event
        coEvery { eventRepository.save(any(), any()) } just Runs

        viewModel.loadEventForEditing("event-film-branch")
        advanceUntilIdle()
        viewModel.onEventTypeChanged("Film")
        viewModel.onFilmDirectorChanged("Nolan")
        viewModel.onFilmRatingChanged("5")
        viewModel.onFilmGenreChanged("SciFi")

        viewModel.updateEvent()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            eventRepository.save(
                "event-film-branch",
                match {
                    it.details is FilmDetails &&
                            it.details.id == "details-film-branch" &&
                            (it.details as FilmDetails).director == "Nolan"
                }
            )
        }
    }

    @Test
    @DisplayName("updateEvent() builds and saves TheaterDetails when form type is Theater")
    fun updateEventBuildsTheaterDetailsBranch() = runTest {
        val event = Event(
            id = "event-theater-branch",
            title = "Old",
            date = LocalDate.of(2026, 12, 12),
            startTime = LocalDateTime.of(2026, 12, 12, 10, 0),
            endTime = LocalDateTime.of(2026, 12, 12, 11, 45),
            ticketPrice = 10.0,
            location = "Mtl",
            status = "Open",
            details = TheaterDetails(
                id = "details-theater-branch",
                writer = "Old",
                genre = "Old",
                durationMin = 60
            )
        )
        coEvery { eventRepository.getById("event-theater-branch") } returns event
        coEvery { eventRepository.save(any(), any()) } just Runs

        viewModel.loadEventForEditing("event-theater-branch")
        advanceUntilIdle()
        viewModel.onEventTypeChanged("Theater")
        viewModel.onTheaterWriterChanged("Shakespeare")
        viewModel.onTheaterGenreChanged("Drama")

        viewModel.updateEvent()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            eventRepository.save(
                "event-theater-branch",
                match {
                    it.details is TheaterDetails &&
                            it.details.id == "details-theater-branch" &&
                            (it.details as TheaterDetails).writer == "Shakespeare"
                }
            )
        }
    }
}
