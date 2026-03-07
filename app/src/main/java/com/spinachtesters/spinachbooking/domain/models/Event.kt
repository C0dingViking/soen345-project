package com.spinachtesters.spinachbooking.domain.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.spinachtesters.spinachbooking.data.models.EventDTO
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
data class Event (
    val id: String = "",
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime = LocalDateTime.now(),
    val ticketPrice: Double = 0.0,
    val location: String = "",
    val status: String = "",
    val details: EventDetails
)

@RequiresApi(Build.VERSION_CODES.O)
fun Event.toDTO() = EventDTO(
    id = this.id,
    title = this.title,
    date = this.date.toEpochDay(),
    startTime = this.startTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli(),
    endTime = this.endTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli(),
    ticketPrice = this.ticketPrice,
    location = this.location,
    status = this.status
)
