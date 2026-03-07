package com.spinachtesters.spinachbooking.domain.models

data class SportDetails(
    override val id: String = "",
    var sportType: String = "",
    var homeTeam: String = "",
    var visitingTeam: String = "",
    var league: String = ""
) : EventDetails(id, "sport") {}
