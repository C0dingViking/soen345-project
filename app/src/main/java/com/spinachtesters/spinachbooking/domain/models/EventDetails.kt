package com.spinachtesters.spinachbooking.domain.models

import com.spinachtesters.spinachbooking.data.models.EventDetailsDTO

abstract class EventDetails(
    open val id: String = "",
    val detailType: String = ""
)

fun EventDetails.toGenericDTO() = EventDetailsDTO(
    id = this.id,
    detailType = this.detailType
)
