package com.spinachtesters.spinachbooking

import com.spinachtesters.spinachbooking.data.models.BookingDTO
import com.spinachtesters.spinachbooking.data.models.toDomain
import com.spinachtesters.spinachbooking.domain.models.Booking
import com.spinachtesters.spinachbooking.domain.models.ToDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BookingMappingTest {
    private lateinit var testBookingDateAsObject: LocalDate
    private var testBookingDateAsLong: Long = 0L

    @BeforeEach
    fun setUp() {
        testBookingDateAsObject = LocalDate.of(2025, 3, 6)
        testBookingDateAsLong = testBookingDateAsObject.toEpochDay()
    }

    @Test
    @DisplayName("correctly maps a domain Booking to a BookingDTO")
    fun bookingDomainToDTOMappingIsCorrect() {
        val booking = Booking(
            bookedBy = "bookedby123",
            bookedFor = "bookedfor123",
            dateOfBooking = testBookingDateAsObject,
            status = "active"
        )

        val dto = booking.ToDTO()

        assertEquals(dto.bookedBy, "bookedby123")
        assertEquals(dto.bookedFor, "bookedfor123")
        assertEquals(dto.dateOfBooking, testBookingDateAsLong)
        assertEquals(dto.status, "active")
    }

    @Test
    @DisplayName("correctly maps a BookingDTO to a domain Booking")
    fun bookingDTOToDomainMappingIsCorrect() {
        val dto = BookingDTO(
            bookedBy = "bookedby123",
            bookedFor = "bookedfor123",
            dateOfBooking = testBookingDateAsLong,
            status = "active"
        )

        val booking = dto.toDomain()

        assertEquals(booking.bookedBy, "bookedby123")
        assertEquals(booking.bookedFor, "bookedfor123")
        assertEquals(booking.dateOfBooking, testBookingDateAsObject)
        assertEquals(booking.status, "active")
    }

    @Test
    @DisplayName("values are preserved when converting from BookingDTO to Booking and back")
    fun bookingRoundTripConversionPreservesValues() {
        val dto = BookingDTO(
            bookedBy = "bookedby123",
            bookedFor = "bookedfor123",
            dateOfBooking = testBookingDateAsLong,
            status = "active"
        )

        val booking = dto.toDomain()
        val roundtripDTO = booking.ToDTO()

        assertEquals(roundtripDTO.bookedBy, "bookedby123")
        assertEquals(roundtripDTO.bookedFor, "bookedfor123")
        assertEquals(roundtripDTO.dateOfBooking, testBookingDateAsLong)
        assertEquals(roundtripDTO.status, "active")
    }
}
