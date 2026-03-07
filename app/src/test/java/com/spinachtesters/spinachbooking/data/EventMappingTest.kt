package com.spinachtesters.spinachbooking.data

import com.spinachtesters.spinachbooking.data.models.EventDTO
import com.spinachtesters.spinachbooking.data.models.toDomain
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.EventDetails
import com.spinachtesters.spinachbooking.domain.models.toDTO
import com.spinachtesters.spinachbooking.domain.models.toGenericDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class EventMappingTest {
    data class FakeEventDetails(override val id: String) : EventDetails(id, "test")

    private lateinit var testDetails: FakeEventDetails
    private lateinit var testDateAsObject: LocalDate
    private lateinit var testStartAsObject: LocalDateTime
    private lateinit var testEndAsObject: LocalDateTime

    private var testDateAsLong: Long = 0
    private var testStartAsLong: Long = 0
    private var testEndAsLong: Long = 0

    private var zoneUTC = ZoneId.systemDefault()

    @BeforeEach
    fun setUp() {
        testDetails = FakeEventDetails("testdetails12345")
        zoneUTC = ZoneId.of("UTC")

        testDateAsObject = LocalDate.of(2025, 3, 6)
        testStartAsObject = LocalDateTime.of(2025, 3, 6, 5, 5, 5)
        testEndAsObject = LocalDateTime.of(2025, 3, 6, 10, 10, 10)

        testDateAsLong = testDateAsObject.toEpochDay()
        testStartAsLong = testStartAsObject.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        testEndAsLong = testEndAsObject.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }

    @Test
    @DisplayName("correctly maps a domain Event to an EventDTO")
    fun eventDomainToDTOMappingIsCorrect() {
        val event = Event(
            id = "event123",
            title = "Concert",
            date = testDateAsObject,
            testStartAsObject,
            endTime = testEndAsObject,
            ticketPrice = 49.99,
            location = "Montreal",
            status = "active",
            details = testDetails
        )

        val dto = event.toDTO()

        assertEquals(dto.id, "event123")
        assertEquals(dto.title, "Concert")
        assertEquals(dto.date, testDateAsLong)
        assertEquals(dto.startTime, testStartAsLong)
        assertEquals(dto.endTime, testEndAsLong)
        assertEquals(dto.ticketPrice, 49.99)
        assertEquals(dto.location, "Montreal")
        assertEquals(dto.status, "active")
        assertEquals(dto.detailsId, testDetails.id)
    }

    @Test
    @DisplayName("correctly maps an EventDTO to a domain Event")
    fun eventDTOToDomainMappingIsCorrect() {
        val dto = EventDTO(
            id = "event123",
            title = "Concert",
            date = testDateAsLong,
            startTime = testStartAsLong,
            endTime = testEndAsLong,
            ticketPrice = 49.99,
            location = "Montreal",
            status = "active",
            detailsId = testDetails.id
        )

        val event = dto.toDomain(testDetails)

        assertEquals(event.id, "event123")
        assertEquals(event.title, "Concert")
        assertEquals(event.date, testDateAsObject)
        assertEquals(event.startTime, testStartAsObject)
        assertEquals(event.endTime, testEndAsObject)
        assertEquals(event.ticketPrice, 49.99)
        assertEquals(event.location, "Montreal")
        assertEquals(event.status, "active")
        assertEquals(event.details, testDetails)
    }

    @Test
    @DisplayName("values are preserved when converting from EventDTO to Event and back")
    fun eventRoundTripConversionPreservesValues() {
        val dto = EventDTO(
            id = "event123",
            title = "Concert",
            date = testDateAsLong,
            startTime = testStartAsLong,
            endTime = testEndAsLong,
            ticketPrice = 49.99,
            location = "Montreal",
            status = "active",
            detailsId = testDetails.id
        )

        val event = dto.toDomain(testDetails)
        val roundtripDTO = event.toDTO()

        assertEquals(roundtripDTO.id, "event123")
        assertEquals(roundtripDTO.title, "Concert")
        assertEquals(roundtripDTO.date, testDateAsLong)
        assertEquals(roundtripDTO.startTime, testStartAsLong)
        assertEquals(roundtripDTO.endTime, testEndAsLong)
        assertEquals(roundtripDTO.ticketPrice, 49.99)
        assertEquals(roundtripDTO.location, "Montreal")
        assertEquals(roundtripDTO.status, "active")
        assertEquals(roundtripDTO.detailsId, testDetails.id)
    }

    @Test
    @DisplayName("event details are correctly converted into generic DTOs")
    fun concreteEventDetailsConvertToDTO() {
        val dto = testDetails.toGenericDTO()

        assertEquals(testDetails.id, dto.id)
        assertEquals(testDetails.detailType, dto.detailType)
    }

}
