package com.spinachtesters.spinachbooking.domain.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.spinachtesters.spinachbooking.data.models.BookingDTO
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class Booking (
    val bookedBy: String = "",   // FKEY to a User instance
    val bookedFor: String = "",   // FKEY to an Event instance
    val dateOfBooking: LocalDate = LocalDate.now(),
    val status: String = ""
)

@RequiresApi(Build.VERSION_CODES.O)
fun Booking.toDTO() = BookingDTO(
    bookedBy = this.bookedBy,
    bookedFor = this.bookedFor,
    dateOfBooking = this.dateOfBooking.toEpochDay(),
    status = this.status
)
