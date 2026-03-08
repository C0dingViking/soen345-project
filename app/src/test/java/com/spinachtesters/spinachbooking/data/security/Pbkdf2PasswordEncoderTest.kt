package com.spinachtesters.spinachbooking.data.security

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Pbkdf2PasswordEncoderTest {

    private val encoder = Pbkdf2PasswordEncoder()

    @Test
    fun hashAndVerify_roundTripSucceeds() {
        val result = encoder.hash("StrongPass123!@#")

        val isValid = encoder.verify(
            plainTextPassword = "StrongPass123!@#",
            hash = result.hash,
            salt = result.salt,
            iterations = result.iterations
        )

        assertTrue(isValid)
    }

    @Test
    fun hashAndVerify_wrongPasswordFails() {
        val result = encoder.hash("StrongPass123!@#")

        val isValid = encoder.verify(
            plainTextPassword = "WrongPassword1!",
            hash = result.hash,
            salt = result.salt,
            iterations = result.iterations
        )

        assertFalse(isValid)
    }

    @Test
    fun hash_twoCallsProduceDifferentSaltAndHash() {
        val first = encoder.hash("StrongPass123!@#")
        val second = encoder.hash("StrongPass123!@#")

        assertNotEquals(first.salt, second.salt)
        assertNotEquals(first.hash, second.hash)
    }
}

