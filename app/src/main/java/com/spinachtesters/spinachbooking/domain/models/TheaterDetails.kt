package com.spinachtesters.spinachbooking.domain.models

data class TheaterDetails(
    override val id: String = "",
    val writer: String = "",
    val genre: String = "",
    val durationMin: Int = 0
) : EventDetails(id, "theater") {}
