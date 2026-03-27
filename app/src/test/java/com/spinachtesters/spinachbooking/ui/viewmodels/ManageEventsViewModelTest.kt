package com.spinachtesters.spinachbooking.ui.viewmodels

import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.testutils.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ManageEventsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: EventRepository
    private lateinit var viewModel: ManageEventsViewModel
    private lateinit var testEventList: List<Event>

    @BeforeEach
    fun setup() {
        fakeRepo = mockk<EventRepository>()
        viewModel = ManageEventsViewModel(fakeRepo)
        testEventList = listOf(
            Event(
                id = "3",
                title = "Drake Concert",
                date = LocalDate.of(2024, 1, 23),
                startTime = LocalDateTime.of(2024, 1, 23, 20, 0),
                endTime = LocalDateTime.of(2024, 1, 23, 23, 0),
                ticketPrice = 200.0,
                location = "Laval, QC",
                status = "BOOKED",
                details = ConcertDetails()
            ),
            Event(
                id = "4",
                title = "Canadiens vs Rangers",
                date = LocalDate.of(2024, 1, 23),
                startTime = LocalDateTime.of(2024, 1, 23, 20, 0),
                endTime = LocalDateTime.of(2024, 1, 23, 23, 0),
                ticketPrice = 200.0,
                location = "Laval, QC",
                status = "BOOKED",
                details = SportDetails()
            )
        );
    }

    @Test
    @DisplayName("loadEvents properly updates the state with the list of events in the repo")
    fun loadEventsUpdatesState() = runTest {
        coEvery { fakeRepo.getAll() } returns testEventList

        viewModel.loadEvents()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(testEventList, state.events)

        coVerify(exactly = 1) { fakeRepo.getAll() }
    }

    @Test
    @DisplayName("deleteEvent deletes the requested event and reloads the list of events")
    fun deleteEventUpdatesState() = runTest {
        val testEvent = testEventList[0];

        coEvery { fakeRepo.deleteById(testEvent.id) } just  Runs
        coEvery { fakeRepo.getAll() } returns testEventList

        // Act
        viewModel.deleteEvent(testEvent)
        advanceUntilIdle()


        coVerify(exactly = 1) { fakeRepo.deleteById(testEvent.id) }
        coVerify(exactly = 1) { fakeRepo.getAll() }

        val state = viewModel.uiState.value
        assertEquals(testEventList, state.events)
    }
}
