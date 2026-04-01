package com.spinachtesters.spinachbooking.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.ui.viewmodels.ManageEventsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ManageEventsIntegrationTest {

    private val eventRepository = EventRepository()
    private val createdEventIds = mutableListOf<String>()
    private val stamp = System.currentTimeMillis().toString().takeLast(7)

    @After
    fun tearDown() = runBlocking {
        for (id in createdEventIds) {
            try { eventRepository.deleteById(id) } catch (_: Exception) {}
        }
    }

    private suspend fun createEvent(titleSuffix: String): Event {
        val event = eventRepository.create(
            Event(
                title = "it_manage_${titleSuffix}_$stamp",
                date = LocalDate.now().plusDays(3),
                startTime = LocalDateTime.now().plusDays(3).withHour(18).withMinute(0).withSecond(0).withNano(0),
                endTime = LocalDateTime.now().plusDays(3).withHour(20).withMinute(0).withSecond(0).withNano(0),
                ticketPrice = 25.0,
                location = "Montreal",
                status = "Open",
                details = ConcertDetails(mainArtist = "Artist", genre = "Rock")
            )
        )
        createdEventIds.add(event.id)
        return event
    }

    @Test
    fun loadEvents_returnsCreatedEvents() = runBlocking {
        val event1 = createEvent("load1")
        val event2 = createEvent("load2")

        val vm = ManageEventsViewModel(eventRepository)
        vm.loadEvents()
        waitUntil("events loaded") {
            vm.uiState.value.events != null && vm.uiState.value.events!!.isNotEmpty()
        }

        val events = vm.uiState.value.events!!
        assertTrue(events.any { it.id == event1.id })
        assertTrue(events.any { it.id == event2.id })
    }

    @Test
    fun deleteEvent_removesFromFirebase() = runBlocking {
        val event = createEvent("delete")

        // Verify it exists
        val fetched = eventRepository.getById(event.id)
        assertNotNull(fetched)

        val vm = ManageEventsViewModel(eventRepository)
        vm.deleteEvent(event)
        waitUntil("event deleted and reloaded") {
            vm.uiState.value.events != null
        }

        // Remove from cleanup list since it's already deleted
        createdEventIds.remove(event.id)

        // Verify it's gone from Firebase
        val deleted = eventRepository.getById(event.id)
        assertNull(deleted)
    }

    @Test
    fun deleteEvent_doesNotAffectOtherEvents() = runBlocking {
        val eventToDelete = createEvent("del_target")
        val eventToKeep = createEvent("del_keep")

        val vm = ManageEventsViewModel(eventRepository)
        vm.deleteEvent(eventToDelete)
        waitUntil("reloaded after delete") {
            vm.uiState.value.events != null
        }

        createdEventIds.remove(eventToDelete.id)

        val kept = eventRepository.getById(eventToKeep.id)
        assertNotNull(kept)

        val deleted = eventRepository.getById(eventToDelete.id)
        assertNull(deleted)
    }

    @Test
    fun loadEvents_afterDelete_reflectsRemoval() = runBlocking {
        val event1 = createEvent("afterdel1")
        val event2 = createEvent("afterdel2")

        val vm = ManageEventsViewModel(eventRepository)
        vm.deleteEvent(event1)
        waitUntil("reloaded after delete") {
            vm.uiState.value.events != null
        }

        createdEventIds.remove(event1.id)

        val events = vm.uiState.value.events!!
        assertTrue(events.none { it.id == event1.id })
        assertTrue(events.any { it.id == event2.id })
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
