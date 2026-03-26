package com.spinachtesters.spinachbooking.domain.models

data class FilmDetails(
    override val id: String = "",   // FKEY to an EventDetails instance
    var director: String = "",
    var runtimeMin: Int = 0,
    var rating: Int = 0,
    var genre: String = ""
) : EventDetails(id, "film") {
    override fun copyWithId(newId: String) = copy(id = newId)
}
