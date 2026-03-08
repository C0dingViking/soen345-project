package com.spinachtesters.spinachbooking.domain.security

data class PasswordHashResult(
    val hash: String,
    val salt: String,
    val iterations: Int
)

interface PasswordEncoder {
    fun hash(plainTextPassword: String): PasswordHashResult
    fun verify(plainTextPassword: String, hash: String, salt: String, iterations: Int): Boolean
}

