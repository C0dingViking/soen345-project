package com.spinachtesters.spinachbooking.domain.models

data class ConcertDetails(
    override val id: String = "",   // FKEY to an EventDetails instance
    val mainArtist: String = "",
    val genre: String = ""
) : EventDetails(id, "concert") {
    override fun copyWithId(newId: String) = copy(id = newId)
}
