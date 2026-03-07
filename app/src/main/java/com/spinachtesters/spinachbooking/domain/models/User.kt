package com.spinachtesters.spinachbooking.domain.models

data class User(
    val id: String = "",
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val phoneNb: String = "",
    val isOrganizer: Boolean = false
) {}
