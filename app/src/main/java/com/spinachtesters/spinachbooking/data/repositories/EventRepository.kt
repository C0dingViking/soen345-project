package com.spinachtesters.spinachbooking.data.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.spinachtesters.spinachbooking.data.RootDatabaseProvider
import com.spinachtesters.spinachbooking.data.models.EventDTO
import com.spinachtesters.spinachbooking.data.models.EventDetailsDTO
import com.spinachtesters.spinachbooking.data.models.toDomain
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.EventDetails
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.domain.models.TheaterDetails
import com.spinachtesters.spinachbooking.domain.models.toDTO
import com.spinachtesters.spinachbooking.domain.models.toGenericDTO

@RequiresApi(Build.VERSION_CODES.O)
class EventRepository(
    private val eventSrc : FirebaseRepository<EventDTO> = FirebaseRepository(EventDTO::class.java, RootDatabaseProvider.events),
    private val detailsSrc : FirebaseRepository<EventDetailsDTO> = FirebaseRepository(EventDetailsDTO::class.java, RootDatabaseProvider.eventDetails),
    private val sportSrc : FirebaseRepository<SportDetails> = FirebaseRepository(SportDetails::class.java, RootDatabaseProvider.sportDetails),
    private val filmSrc : FirebaseRepository<FilmDetails> = FirebaseRepository(FilmDetails::class.java, RootDatabaseProvider.filmDetails),
    private val theaterSrc : FirebaseRepository<TheaterDetails> = FirebaseRepository(TheaterDetails::class.java, RootDatabaseProvider.theaterDetails),
    private val concertSrc : FirebaseRepository<ConcertDetails> = FirebaseRepository(ConcertDetails::class.java, RootDatabaseProvider.concertDetails)
) {
    suspend fun save(id: String, item: Event) {
        eventSrc.save(id, item.toDTO())
        detailsSrc.save(item.details.id, item.details.toGenericDTO())

        when (val details = item.details) {
            is SportDetails -> sportSrc.save(details.id, details)
            is FilmDetails -> filmSrc.save(details.id, details)
            is TheaterDetails -> theaterSrc.save(details.id, details)
            is ConcertDetails -> concertSrc.save(details.id, details)
        }
    }

    private suspend fun generateConcreteDetails(detailsId: String): EventDetails? {
        val eventDetailsDTO = detailsSrc.getById(detailsId) ?: return null

        val concreteDetails = when (eventDetailsDTO.detailType) {
            "sport" -> sportSrc.getById(eventDetailsDTO.id)
            "film" -> filmSrc.getById(eventDetailsDTO.id)
            "theater" -> theaterSrc.getById(eventDetailsDTO.id)
            "concert" -> concertSrc.getById(eventDetailsDTO.id)
            else -> null
        }

        return concreteDetails
    }

    suspend fun getById(id: String): Event? {
        val eventDTO = eventSrc.getById(id) ?: return null
        val concreteDetails = generateConcreteDetails(eventDTO.detailsId) ?: return null

        return eventDTO.toDomain(concreteDetails)
    }

    suspend fun getAll(): List<Event> {
        val eventsDTOs = eventSrc.getAll()

        return eventsDTOs.mapNotNull { dto ->
            val concreteDetails = generateConcreteDetails(dto.detailsId) ?: return@mapNotNull null
            dto.toDomain(concreteDetails)
        }
    }

    suspend fun deleteById(id: String) {
        val eventDto = eventSrc.getById(id) ?: return
        deleteConcreteDetails(eventDto.detailsId)

        eventSrc.deleteById(id)
    }

    private suspend fun deleteConcreteDetails(detailsId: String) {
        val eventDetailsDTO = detailsSrc.getById(detailsId) ?: return

        when (eventDetailsDTO.detailType) {
            "sport" -> sportSrc.deleteById(eventDetailsDTO.id)
            "film" -> filmSrc.deleteById(eventDetailsDTO.id)
            "theater" -> theaterSrc.deleteById(eventDetailsDTO.id)
            "concert" -> concertSrc.deleteById(eventDetailsDTO.id)
            else -> null
        }

        detailsSrc.deleteById(eventDetailsDTO.id)
    }
}
