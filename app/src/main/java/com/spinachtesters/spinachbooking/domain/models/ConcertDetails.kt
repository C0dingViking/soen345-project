package com.spinachtesters.spinachbooking.domain.models

data class ConcertDetails(
    override val id: String = "",
    val mainArtist: String = "",
    val genre: String = ""
) : EventDetails(id, "concert") {}
