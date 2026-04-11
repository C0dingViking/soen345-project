package com.spinachtesters.spinachbooking.data

import com.spinachtesters.spinachbooking.data.models.EventDTO
import com.spinachtesters.spinachbooking.data.models.EventDetailsDTO
import com.spinachtesters.spinachbooking.data.repositories.EventRepository
import com.spinachtesters.spinachbooking.data.repositories.FirebaseRepository
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.domain.models.TheaterDetails
import com.spinachtesters.spinachbooking.domain.models.toDTO
import com.spinachtesters.spinachbooking.domain.models.toGenericDTO
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.LocalDate
import java.time.LocalDateTime

class EventRepositoryTest {
    private lateinit var fakeEventSrc: FirebaseRepository<EventDTO>
    private lateinit var fakeDetailsSrc: FirebaseRepository<EventDetailsDTO>
    private lateinit var fakeSportSrc: FirebaseRepository<SportDetails>
    private lateinit var fakeFilmSrc: FirebaseRepository<FilmDetails>
    private lateinit var fakeTheaterSrc: FirebaseRepository<TheaterDetails>
    private lateinit var fakeConcertSrc: FirebaseRepository<ConcertDetails>

    private lateinit var repo: EventRepository

    private lateinit var testEvent: Event
    private lateinit var testEventDTO: EventDTO
    private lateinit var testEventDetailsDTO: EventDetailsDTO
    private lateinit var testFilmDetails: FilmDetails

    @BeforeEach
    fun setup() {
        fakeEventSrc = mockk<FirebaseRepository<EventDTO>>()
        fakeDetailsSrc = mockk<FirebaseRepository<EventDetailsDTO>>()
        fakeSportSrc = mockk<FirebaseRepository<SportDetails>>()
        fakeFilmSrc = mockk<FirebaseRepository<FilmDetails>>()
        fakeTheaterSrc = mockk<FirebaseRepository<TheaterDetails>>()
        fakeConcertSrc = mockk<FirebaseRepository<ConcertDetails>>()

        repo = EventRepository(
            fakeEventSrc, fakeDetailsSrc, fakeSportSrc, fakeFilmSrc, fakeTheaterSrc, fakeConcertSrc
        )

        testFilmDetails = FilmDetails(
            id = "testdetails123",
            director = "testdirector",
            runtimeMin = 120,
            rating = 5,
            genre = "testgenre"
        )

        testEvent = Event(
            id = "testevent123",
            title = "testtitle123",
            date = LocalDate.of(2025, 3, 6),
            startTime = LocalDateTime.of(2025, 3, 6, 18, 0),
            endTime = LocalDateTime.of(2025, 3, 6, 21, 0),
            ticketPrice = 15.0,
            location = "testlocation",
            status = "testactive",
            details = testFilmDetails
        )

        testEventDTO = testEvent.toDTO()
        testEventDetailsDTO = testFilmDetails.toGenericDTO()
    }

    @Test
    @DisplayName("save delegates the action to store an event, generic details, and concrete details")
    fun saveDelegatesToSource() = runTest {
        coEvery { fakeEventSrc.save("testevent123", testEventDTO) } just Runs
        coEvery { fakeDetailsSrc.save("testdetails123", testEventDetailsDTO) } just Runs
        coEvery { fakeFilmSrc.save("testdetails123", testFilmDetails) } just Runs

        repo.save("testevent123", testEvent)

        coVerify { fakeEventSrc.save("testevent123", testEventDTO) }
        coVerify { fakeDetailsSrc.save("testdetails123", testEventDetailsDTO) }
        coVerify { fakeFilmSrc.save("testdetails123", testFilmDetails) }

        // ensure no actions were delegated to other detail sources
        coVerify(exactly = 0) { fakeSportSrc.save(any(), any()) }
        coVerify(exactly = 0) { fakeTheaterSrc.save(any(), any()) }
        coVerify(exactly = 0) { fakeConcertSrc.save(any(), any()) }
    }

    @Test
    @DisplayName("getById returns a full Event with details when DTO exists")
    fun getByIdReturnsBooking() = runTest {
        coEvery { fakeEventSrc.getById("testevent123") } returns testEventDTO
        coEvery { fakeDetailsSrc.getById("testdetails123") } returns testEventDetailsDTO
        coEvery { fakeFilmSrc.getById("testdetails123") } returns testFilmDetails

        val result = repo.getById("testevent123")

        assertEquals(testEvent, result)
        assertEquals(testFilmDetails, result!!.details)
        assertEquals(testEventDetailsDTO, result.details.toGenericDTO())

        coVerify(exactly = 1) { fakeEventSrc.getById("testevent123") }
        coVerify(exactly = 1) { fakeDetailsSrc.getById("testdetails123") }
        coVerify(exactly = 1) { fakeFilmSrc.getById("testdetails123") }

        // ensure no actions were delegated to other detail sources
        coVerify(exactly = 0) { fakeSportSrc.save(any(), any()) }
        coVerify(exactly = 0) { fakeTheaterSrc.save(any(), any()) }
        coVerify(exactly = 0) { fakeConcertSrc.save(any(), any()) }
    }

    @Test
    @DisplayName("getById returns null when the event id is not found")
    fun getByIdReturnsNullIfEventMissing() = runTest {
        coEvery { fakeEventSrc.getById("missing") } returns null

        val result = repo.getById("missing")

        assertNull(result)
        coVerify(exactly = 1) { fakeEventSrc.getById("missing") }
    }

    @Test
    @DisplayName("getById returns null when the event details DTO is not found")
    fun getByIdReturnsNullIfDetailsDTOMissing() = runTest {
        coEvery { fakeEventSrc.getById("testevent123") } returns testEventDTO
        coEvery { fakeDetailsSrc.getById("testdetails123") } returns null

        val result = repo.getById("testevent123")

        assertNull(result)
        coVerify(exactly = 1) { fakeEventSrc.getById("testevent123") }
        coVerify(exactly = 1) { fakeDetailsSrc.getById("testdetails123") }
    }

    @Test
    @DisplayName("getById returns null when the concrete details DTO is not found")
    fun getByIdReturnsNullIfConcreteDetailsMissing() = runTest {
        coEvery { fakeEventSrc.getById("testevent123") } returns testEventDTO
        coEvery { fakeDetailsSrc.getById("testdetails123") } returns testEventDetailsDTO
        coEvery { fakeFilmSrc.getById("testdetails123") } returns null

        val result = repo.getById("testevent123")

        assertNull(result)
        coVerify(exactly = 1) { fakeEventSrc.getById("testevent123") }
        coVerify(exactly = 1) { fakeDetailsSrc.getById("testdetails123") }
        coVerify(exactly = 1) { fakeFilmSrc.getById("testdetails123") }
    }

    @Test
    @DisplayName("getById returns null when generic detail type is unknown")
    fun getByIdReturnsNullForUnknownDetailType() = runTest {
        val unknownEventDetailsDTO = testEventDetailsDTO.copy(detailType = "mystery")

        coEvery { fakeEventSrc.getById("testevent123") } returns testEventDTO
        coEvery { fakeDetailsSrc.getById("testdetails123") } returns unknownEventDetailsDTO

        val result = repo.getById("testevent123")

        assertNull(result)
        coVerify(exactly = 0) { fakeFilmSrc.getById(any()) }
        coVerify(exactly = 0) { fakeSportSrc.getById(any()) }
        coVerify(exactly = 0) { fakeTheaterSrc.getById(any()) }
        coVerify(exactly = 0) { fakeConcertSrc.getById(any()) }
    }

    @Test
    @DisplayName("getAll returns mapped list of full Events with details")
    fun getAllReturnsEvents() = runTest {
        coEvery { fakeEventSrc.getAll() } returns listOf(
            testEventDTO,
            testEventDTO.copy(id = "copy")
        )
        coEvery { fakeDetailsSrc.getById("testdetails123") } returns testEventDetailsDTO
        coEvery { fakeDetailsSrc.getById("copy") } returns testEventDetailsDTO.copy(id = "copy")
        coEvery { fakeFilmSrc.getById("testdetails123") } returns testFilmDetails
        coEvery { fakeFilmSrc.getById("copy") } returns testFilmDetails.copy(id = "copy")

        val result = repo.getAll()
        val expected = listOf(testEvent, testEvent.copy(id = "copy"))

        assertEquals(expected, result)
        coVerify(exactly = 1) { fakeEventSrc.getAll() }
        coVerify(exactly = 2) { fakeDetailsSrc.getById("testdetails123") }
        coVerify(exactly = 2) { fakeFilmSrc.getById("testdetails123") }
    }

    @Test
    @DisplayName("getAll returns empty list when no DTOs exist")
    fun getAllReturnsEmptyList() = runTest {
        coEvery { fakeEventSrc.getAll() } returns emptyList()

        val result = repo.getAll()

        assertEquals(emptyList<Event>(), result)
        coVerify(exactly = 1) { fakeEventSrc.getAll() }
    }

    @Test
    @DisplayName("deleteById deletes event, generic details, and concrete details")
    fun deleteByIdDeletesEverything() = runTest {
        coEvery { fakeEventSrc.getById("testevent123") } returns testEventDTO
        coEvery { fakeDetailsSrc.getById("testdetails123") } returns testEventDetailsDTO
        coEvery { fakeFilmSrc.deleteById("testdetails123") } just Runs
        coEvery { fakeDetailsSrc.deleteById("testdetails123") } just Runs
        coEvery { fakeEventSrc.deleteById("testevent123") } just Runs

        repo.deleteById("testevent123")

        coVerify(exactly = 1) { fakeEventSrc.getById("testevent123") }
        coVerify(exactly = 1) { fakeDetailsSrc.getById("testdetails123") }

        coVerify(exactly = 1) { fakeFilmSrc.deleteById("testdetails123") }
        coVerify(exactly = 1) { fakeDetailsSrc.deleteById("testdetails123") }
        coVerify(exactly = 1) { fakeEventSrc.deleteById("testevent123") }

        coVerify(exactly = 0) { fakeSportSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeTheaterSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeConcertSrc.deleteById(any()) }
    }

    @Test
    @DisplayName("deleteById does nothing when event does not exist")
    fun deleteByIdDoesNothingWhenEventMissing() = runTest {
        coEvery { fakeEventSrc.getById("missing") } returns null

        repo.deleteById("missing")

        coVerify(exactly = 1) { fakeEventSrc.getById("missing") }
        coVerify(exactly = 0) { fakeEventSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeDetailsSrc.getById(any()) }
        coVerify(exactly = 0) { fakeDetailsSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeFilmSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeSportSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeTheaterSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeConcertSrc.deleteById(any()) }
    }

    @Test
    @DisplayName("deleteConcreteDetailsByType deletes only the requested concrete detail source")
    fun deleteConcreteDetailsByTypeDeletesOnlyTargetSource() = runTest {
        coEvery { fakeTheaterSrc.deleteById("details-55") } just Runs

        repo.deleteConcreteDetailsByType("details-55", "theater")

        coVerify(exactly = 1) { fakeTheaterSrc.deleteById("details-55") }
        coVerify(exactly = 0) { fakeSportSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeFilmSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeConcertSrc.deleteById(any()) }
        coVerify(exactly = 0) { fakeDetailsSrc.deleteById(any()) }
    }

    @Test
    @DisplayName("deleteConcreteDetailsByType dispatches each known type and returns for unknown")
    fun deleteConcreteDetailsByTypeDispatchesAllKnownAndUnknown() = runTest {
        coEvery { fakeSportSrc.deleteById("s1") } just Runs
        coEvery { fakeFilmSrc.deleteById("f1") } just Runs
        coEvery { fakeTheaterSrc.deleteById("t1") } just Runs
        coEvery { fakeConcertSrc.deleteById("c1") } just Runs

        repo.deleteConcreteDetailsByType("s1", "sport")
        repo.deleteConcreteDetailsByType("f1", "film")
        repo.deleteConcreteDetailsByType("t1", "theater")
        repo.deleteConcreteDetailsByType("c1", "concert")
        repo.deleteConcreteDetailsByType("x1", "unknown")

        coVerify(exactly = 1) { fakeSportSrc.deleteById("s1") }
        coVerify(exactly = 1) { fakeFilmSrc.deleteById("f1") }
        coVerify(exactly = 1) { fakeTheaterSrc.deleteById("t1") }
        coVerify(exactly = 1) { fakeConcertSrc.deleteById("c1") }
        coVerify(exactly = 0) { fakeDetailsSrc.deleteById(any()) }
    }

}
