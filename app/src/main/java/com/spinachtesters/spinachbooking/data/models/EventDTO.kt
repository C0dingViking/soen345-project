package com.spinachtesters.spinachbooking.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.EventDetails
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class EventDTO(
    var id: String = "",
    var title: String = "",
    var date: Long = 0L,        // stored as number of epoch days
    var startTime: Long = 0L,   // stored as number of epoch millis
    var endTime: Long = 0L,     // stored as number of epoch millis
    var ticketPrice: Double = 0.0,
    var location: String = "",
    var status: String = ""
)

@RequiresApi(Build.VERSION_CODES.O)
fun EventDTO.toDomain(eventDetails: EventDetails) = Event(
    id = this.id,
    title = this.title,
    date = LocalDate.ofEpochDay(this.date),
    startTime = Instant.ofEpochMilli(this.startTime).atZone(ZoneId.of("UST")).toLocalDateTime(),
    endTime = Instant.ofEpochMilli(this.endTime).atZone(ZoneId.of("UST")).toLocalDateTime(),
    ticketPrice = this.ticketPrice,
    location = this.location,
    status = this.status,
    details = eventDetails
)
