package com.spinachtesters.spinachbooking.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.domain.models.TheaterDetails
import com.spinachtesters.spinachbooking.ui.viewmodels.AddEventViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class AddEventIntegrationTest {

    private val eventRepository = EventRepository()
    private val createdEventIds = mutableListOf<String>()
    private val stamp = System.currentTimeMillis().toString().takeLast(7)
    private val futureDate = LocalDate.now().plusDays(5)
        .format(DateTimeFormatter.ofPattern("yyyy-M-d"))

    @After
    fun tearDown() = runBlocking {
        for (id in createdEventIds) {
            try { eventRepository.deleteById(id) } catch (_: Exception) {}
        }
    }

    @Test
    fun addEvent_concert_persistsInFirebase() = runBlocking {
        val title = "it_concert_$stamp"
        val vm = AddEventViewModel(eventRepository)

        vm.onEventNameChanged(title)
        vm.onTicketPriceChanged("30")
        vm.onEventTypeChanged("Concert")
        vm.onEventDateChanged(futureDate)
        vm.onTimeStartChanged("18:00")
        vm.onTimeEndChanged("20:00")
        vm.onEventLocationChanged("Montreal")
        vm.onConcertArtistChanged("Coldplay")
        vm.onConcertGenreChanged("Rock")

        vm.addEvent()
        waitUntil("event created") {
            vm.uiState.value.isSuccess || vm.uiState.value.errorMessage != null
        }
        assertNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.isSuccess)

        val allEvents = eventRepository.getAll()
        val created = allEvents.find { it.title == title }
        assertNotNull(created)
        createdEventIds.add(created!!.id)

        assertEquals(30.0, created.ticketPrice, 0.0)
        assertEquals("Montreal", created.location)
        assertEquals("Open", created.status)
        assertTrue(created.details is ConcertDetails)

        val details = created.details as ConcertDetails
        assertEquals("Coldplay", details.mainArtist)
        assertEquals("Rock", details.genre)
    }

    @Test
    fun addEvent_sport_persistsInFirebase() = runBlocking {
        val title = "it_sport_$stamp"
        val vm = AddEventViewModel(eventRepository)

        vm.onEventNameChanged(title)
        vm.onTicketPriceChanged("50")
        vm.onEventTypeChanged("Sports")
        vm.onEventDateChanged(futureDate)
        vm.onTimeStartChanged("19:00")
        vm.onTimeEndChanged("21:30")
        vm.onEventLocationChanged("Toronto")
        vm.onSportTypeChanged("Hockey")
        vm.onSportHomeTeamChanged("Canadiens")
        vm.onSportVisitingTeamChanged("Maple Leafs")
        vm.onSportLeagueChanged("NHL")

        vm.addEvent()
        waitUntil("event created") {
            vm.uiState.value.isSuccess || vm.uiState.value.errorMessage != null
        }
        assertNull(vm.uiState.value.errorMessage)

        val allEvents = eventRepository.getAll()
        val created = allEvents.find { it.title == title }
        assertNotNull(created)
        createdEventIds.add(created!!.id)

        assertTrue(created.details is SportDetails)
        val details = created.details as SportDetails
        assertEquals("Hockey", details.sportType)
        assertEquals("Canadiens", details.homeTeam)
        assertEquals("Maple Leafs", details.visitingTeam)
        assertEquals("NHL", details.league)
    }

    @Test
    fun addEvent_film_persistsInFirebase() = runBlocking {
        val title = "it_film_$stamp"
        val vm = AddEventViewModel(eventRepository)

        vm.onEventNameChanged(title)
        vm.onTicketPriceChanged("15")
        vm.onEventTypeChanged("Film")
        vm.onEventDateChanged(futureDate)
        vm.onTimeStartChanged("20:00")
        vm.onTimeEndChanged("22:30")
        vm.onEventLocationChanged("Vancouver")
        vm.onFilmDirectorChanged("Villeneuve")
        vm.onFilmRatingChanged("5")
        vm.onFilmGenreChanged("Sci-Fi")

        vm.addEvent()
        waitUntil("event created") {
            vm.uiState.value.isSuccess || vm.uiState.value.errorMessage != null
        }
        assertNull(vm.uiState.value.errorMessage)

        val allEvents = eventRepository.getAll()
        val created = allEvents.find { it.title == title }
        assertNotNull(created)
        createdEventIds.add(created!!.id)

        assertTrue(created.details is FilmDetails)
        val details = created.details as FilmDetails
        assertEquals("Villeneuve", details.director)
        assertEquals(5, details.rating)
        assertEquals("Sci-Fi", details.genre)
        assertEquals(150, details.runtimeMin) // 20:00 to 22:30 = 150 min
    }

    @Test
    fun addEvent_theater_persistsInFirebase() = runBlocking {
        val title = "it_theater_$stamp"
        val vm = AddEventViewModel(eventRepository)

        vm.onEventNameChanged(title)
        vm.onTicketPriceChanged("45")
        vm.onEventTypeChanged("Theater")
        vm.onEventDateChanged(futureDate)
        vm.onTimeStartChanged("14:00")
        vm.onTimeEndChanged("16:00")
        vm.onEventLocationChanged("Ottawa")
        vm.onTheaterWriterChanged("Shakespeare")
        vm.onTheaterGenreChanged("Drama")

        vm.addEvent()
        waitUntil("event created") {
            vm.uiState.value.isSuccess || vm.uiState.value.errorMessage != null
        }
        assertNull(vm.uiState.value.errorMessage)

        val allEvents = eventRepository.getAll()
        val created = allEvents.find { it.title == title }
        assertNotNull(created)
        createdEventIds.add(created!!.id)

        assertTrue(created.details is TheaterDetails)
        val details = created.details as TheaterDetails
        assertEquals("Shakespeare", details.writer)
        assertEquals("Drama", details.genre)
        assertEquals(120, details.durationMin) // 14:00 to 16:00 = 120 min
    }

    @Test
    fun addEvent_thenLoadForEditing_populatesUiState() = runBlocking {
        val title = "it_edit_load_$stamp"
        val vm = AddEventViewModel(eventRepository)

        vm.onEventNameChanged(title)
        vm.onTicketPriceChanged("25")
        vm.onEventTypeChanged("Concert")
        vm.onEventDateChanged(futureDate)
        vm.onTimeStartChanged("18:00")
        vm.onTimeEndChanged("20:00")
        vm.onEventLocationChanged("Montreal")
        vm.onConcertArtistChanged("Radiohead")
        vm.onConcertGenreChanged("Alt Rock")

        vm.addEvent()
        waitUntil("event created") {
            vm.uiState.value.isSuccess || vm.uiState.value.errorMessage != null
        }

        val allEvents = eventRepository.getAll()
        val created = allEvents.find { it.title == title }
        assertNotNull(created)
        createdEventIds.add(created!!.id)

        val editVm = AddEventViewModel(eventRepository)
        editVm.loadEventForEditing(created.id)
        waitUntil("event loaded for editing") {
            editVm.uiState.value.eventName == title && !editVm.uiState.value.isLoading
        }

        val state = editVm.uiState.value
        assertEquals(title, state.eventName)
        assertEquals("25", state.ticketPrice)
        assertEquals("Concert", state.eventType)
        assertEquals("Montreal", state.location)
        assertEquals("Radiohead", state.concertMainArtist)
        assertEquals("Alt Rock", state.concertGenre)
    }

    @Test
    fun addEvent_thenUpdate_persistsChanges() = runBlocking {
        val originalTitle = "it_update_$stamp"
        val updatedTitle = "it_updated_$stamp"
        val vm = AddEventViewModel(eventRepository)

        vm.onEventNameChanged(originalTitle)
        vm.onTicketPriceChanged("20")
        vm.onEventTypeChanged("Concert")
        vm.onEventDateChanged(futureDate)
        vm.onTimeStartChanged("18:00")
        vm.onTimeEndChanged("20:00")
        vm.onEventLocationChanged("Montreal")
        vm.onConcertArtistChanged("Coldplay")
        vm.onConcertGenreChanged("Pop")

        vm.addEvent()
        waitUntil("event created") {
            vm.uiState.value.isSuccess || vm.uiState.value.errorMessage != null
        }

        val allEvents = eventRepository.getAll()
        val created = allEvents.find { it.title == originalTitle }
        assertNotNull(created)
        createdEventIds.add(created!!.id)

        val editVm = AddEventViewModel(eventRepository)
        editVm.loadEventForEditing(created.id)
        waitUntil("event loaded") {
            editVm.uiState.value.eventName == originalTitle && !editVm.uiState.value.isLoading
        }

        editVm.onEventNameChanged(updatedTitle)
        editVm.onTicketPriceChanged("35")
        editVm.onEventLocationChanged("Quebec")

        editVm.updateEvent()
        waitUntil("event updated") {
            editVm.uiState.value.isSuccess || editVm.uiState.value.errorMessage != null
        }
        assertNull(editVm.uiState.value.errorMessage)

        val updatedEvent = eventRepository.getById(created.id)
        assertNotNull(updatedEvent)
        assertEquals(updatedTitle, updatedEvent!!.title)
        assertEquals(35.0, updatedEvent.ticketPrice, 0.0)
        assertEquals("Quebec", updatedEvent.location)
        assertTrue(updatedEvent.details is ConcertDetails)
        assertEquals("Coldplay", (updatedEvent.details as ConcertDetails).mainArtist)
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
