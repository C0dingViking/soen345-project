package com.spinachtesters.spinachbooking.domain.models

data class SportDetails(
    override val id: String = "",   // FKEY to an EventDetails instance
    var sportType: String = "",
    var homeTeam: String = "",
    var visitingTeam: String = "",
    var league: String = ""
) : EventDetails(id, "sport") {}
