package com.spinachtesters.spinachbooking.data

import com.spinachtesters.spinachbooking.data.models.EventDTO
import com.spinachtesters.spinachbooking.data.models.toDomain
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.EventDetails
import com.spinachtesters.spinachbooking.domain.models.toDTO
import org.junit.jupiter.api.Assertions
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

        Assertions.assertEquals(dto.id, "event123")
        Assertions.assertEquals(dto.title, "Concert")
        Assertions.assertEquals(dto.date, testDateAsLong)
        Assertions.assertEquals(dto.startTime, testStartAsLong)
        Assertions.assertEquals(dto.endTime, testEndAsLong)
        Assertions.assertEquals(dto.ticketPrice, 49.99)
        Assertions.assertEquals(dto.location, "Montreal")
        Assertions.assertEquals(dto.status, "active")
        Assertions.assertEquals(dto.detailsId, testDetails.id)
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

        Assertions.assertEquals(event.id, "event123")
        Assertions.assertEquals(event.title, "Concert")
        Assertions.assertEquals(event.date, testDateAsObject)
        Assertions.assertEquals(event.startTime, testStartAsObject)
        Assertions.assertEquals(event.endTime, testEndAsObject)
        Assertions.assertEquals(event.ticketPrice, 49.99)
        Assertions.assertEquals(event.location, "Montreal")
        Assertions.assertEquals(event.status, "active")
        Assertions.assertEquals(event.details, testDetails)
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

        Assertions.assertEquals(roundtripDTO.id, "event123")
        Assertions.assertEquals(roundtripDTO.title, "Concert")
        Assertions.assertEquals(roundtripDTO.date, testDateAsLong)
        Assertions.assertEquals(roundtripDTO.startTime, testStartAsLong)
        Assertions.assertEquals(roundtripDTO.endTime, testEndAsLong)
        Assertions.assertEquals(roundtripDTO.ticketPrice, 49.99)
        Assertions.assertEquals(roundtripDTO.location, "Montreal")
        Assertions.assertEquals(roundtripDTO.status, "active")
        Assertions.assertEquals(roundtripDTO.detailsId, testDetails.id)
    }

}
