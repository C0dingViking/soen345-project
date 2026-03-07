package com.spinachtesters.spinachbooking.data

import com.spinachtesters.spinachbooking.data.models.BookingDTO
import com.spinachtesters.spinachbooking.data.models.toDomain
import com.spinachtesters.spinachbooking.data.repositories.BookingRepository
import com.spinachtesters.spinachbooking.data.repositories.FirebaseRepository
import com.spinachtesters.spinachbooking.domain.models.Booking
import com.spinachtesters.spinachbooking.domain.models.toDTO
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.LocalDate

class BookingRepositoryTest {
    private lateinit var fakeSource: FirebaseRepository<BookingDTO>
    private lateinit var repo: BookingRepository
    private lateinit var testBookingDTO: BookingDTO
    private lateinit var testBooking: Booking

    @BeforeEach
    fun setup() {
        fakeSource = mockk<FirebaseRepository<BookingDTO>>()
        repo = BookingRepository(fakeSource)

        testBooking = Booking(
            bookedBy = "bookedby123",
            bookedFor = "bookedfor123",
            dateOfBooking = LocalDate.of(2025, 3, 6),
            status = "active"
        )
        testBookingDTO = testBooking.toDTO()
    }

    @Test
    @DisplayName("save delegates to FirebaseRepository with DTO")
    fun saveDelegatesToSource() = runTest {
        coEvery { fakeSource.save("id123", testBookingDTO) } just Runs

        repo.save("id123", testBooking)

        coVerify(exactly = 1) { fakeSource.save("id123", testBookingDTO) }
    }

    @Test
    @DisplayName("getById returns mapped Booking when DTO exists")
    fun getByIdReturnsBooking() = runTest {
        coEvery { fakeSource.getById("id123") } returns testBookingDTO

        val result = repo.getById("id123")

        assertEquals(testBooking, result)
        coVerify(exactly = 1) { fakeSource.getById("id123") }
    }

    @Test
    @DisplayName("getById returns null when DTO is null")
    fun getByIdReturnsNull() = runTest {
        coEvery { fakeSource.getById("missing") } returns null

        val result = repo.getById("missing")

        assertNull(result)
        coVerify(exactly = 1) { fakeSource.getById("missing") }
    }

    @Test
    @DisplayName("getAll returns mapped list of Bookings")
    fun getAllReturnsBookings() = runTest {
        val dtoList = listOf(
            testBookingDTO,
            testBookingDTO.copy(bookedBy = "otherUser")
        )

        val expected = dtoList.map { it.toDomain() }

        coEvery { fakeSource.getAll() } returns dtoList

        val result = repo.getAll()

        assertEquals(expected, result)
        coVerify(exactly = 1) { fakeSource.getAll() }
    }

    @Test
    @DisplayName("getAll returns empty list when no DTOs exist")
    fun getAllReturnsEmptyList() = runTest {
        coEvery { fakeSource.getAll() } returns emptyList()

        val result = repo.getAll()

        assertEquals(emptyList<Booking>(), result)
        coVerify(exactly = 1) { fakeSource.getAll() }
    }
}
