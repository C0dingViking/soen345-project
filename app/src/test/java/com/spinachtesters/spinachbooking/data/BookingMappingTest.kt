package com.spinachtesters.spinachbooking.data

import com.spinachtesters.spinachbooking.data.models.BookingDTO
import com.spinachtesters.spinachbooking.data.models.toDomain
import com.spinachtesters.spinachbooking.domain.models.Booking
import com.spinachtesters.spinachbooking.domain.models.toDTO
import org.junit.jupiter.api.Assertions
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

        val dto = booking.toDTO()

        Assertions.assertEquals(dto.bookedBy, "bookedby123")
        Assertions.assertEquals(dto.bookedFor, "bookedfor123")
        Assertions.assertEquals(dto.dateOfBooking, testBookingDateAsLong)
        Assertions.assertEquals(dto.status, "active")
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

        Assertions.assertEquals(booking.bookedBy, "bookedby123")
        Assertions.assertEquals(booking.bookedFor, "bookedfor123")
        Assertions.assertEquals(booking.dateOfBooking, testBookingDateAsObject)
        Assertions.assertEquals(booking.status, "active")
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
        val roundtripDTO = booking.toDTO()

        Assertions.assertEquals(roundtripDTO.bookedBy, "bookedby123")
        Assertions.assertEquals(roundtripDTO.bookedFor, "bookedfor123")
        Assertions.assertEquals(roundtripDTO.dateOfBooking, testBookingDateAsLong)
        Assertions.assertEquals(roundtripDTO.status, "active")
    }
}
