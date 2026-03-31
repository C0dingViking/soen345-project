package com.spinachtesters.spinachbooking.data.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.spinachtesters.spinachbooking.data.RootDatabaseProvider
import com.spinachtesters.spinachbooking.data.models.BookingDTO
import com.spinachtesters.spinachbooking.data.models.toDomain
import com.spinachtesters.spinachbooking.domain.models.Booking
import com.spinachtesters.spinachbooking.domain.models.toDTO

@RequiresApi(Build.VERSION_CODES.O)
class BookingRepository(
    private val source: FirebaseRepository<BookingDTO> =
        FirebaseRepository(BookingDTO::class.java, RootDatabaseProvider.bookings)
) {
    suspend fun save(id: String, item: Booking) {
        source.save(id, item.toDTO())
    }

    suspend fun getById(id: String): Booking? {
        val dto = source.getById(id)
        return dto?.toDomain()
    }

    suspend fun getAll(): List<Booking> {
        val dtoList = source.getAll()
        return dtoList.map { dto -> dto.toDomain() }
    }

    suspend fun deleteById(id: String) {
        source.deleteById(id)
    }
}
