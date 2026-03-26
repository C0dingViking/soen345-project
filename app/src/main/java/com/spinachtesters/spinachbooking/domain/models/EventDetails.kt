package com.spinachtesters.spinachbooking.domain.models

import com.spinachtesters.spinachbooking.data.models.EventDetailsDTO

abstract class EventDetails(
    open val id: String = "",
    val detailType: String = ""
)  {
    abstract fun copyWithId(newId: String): EventDetails
}

fun EventDetails.toGenericDTO() = EventDetailsDTO(
    id = this.id,
    detailType = this.detailType
)
