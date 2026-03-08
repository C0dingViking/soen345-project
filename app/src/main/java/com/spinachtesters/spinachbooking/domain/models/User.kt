package com.spinachtesters.spinachbooking.domain.models

data class User(
    val id: String = "",
    val fullName: String = "",
    val username: String = "",
    val passwordHash: String = "",
    val passwordSalt: String = "",
    val passwordIterations: Int = 0,
    val email: String = "",
    val phoneNb: String = "",
    val isOrganizer: Boolean = false
) {}
