package com.spinachtesters.spinachbooking.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.spinachtesters.spinachbooking.domain.models.Booking
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class BookingDTO(
    var bookedBy: String = "",
    var bookedFor: String = "",
    var dateOfBooking: Long = 0L, // date is stored as the number of epoch days
    var status: String = ""
)

@RequiresApi(Build.VERSION_CODES.O)
fun BookingDTO.toDomain() = Booking(
    bookedBy = bookedBy,
    bookedFor = bookedFor,
    dateOfBooking = LocalDate.ofEpochDay(this.dateOfBooking),
    status = status
)
