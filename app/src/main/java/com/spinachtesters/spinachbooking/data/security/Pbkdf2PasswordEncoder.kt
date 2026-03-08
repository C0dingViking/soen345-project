package com.spinachtesters.spinachbooking.data.security

import com.spinachtesters.spinachbooking.domain.security.PasswordEncoder
import com.spinachtesters.spinachbooking.domain.security.PasswordHashResult
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class Pbkdf2PasswordEncoder(
    private val iterations: Int = DEFAULT_ITERATIONS,
    private val saltLengthBytes: Int = DEFAULT_SALT_LENGTH_BYTES,
    private val keyLengthBits: Int = DEFAULT_KEY_LENGTH_BITS
) : PasswordEncoder {

    override fun hash(plainTextPassword: String): PasswordHashResult {
        val salt = ByteArray(saltLengthBytes)
        SecureRandom().nextBytes(salt)
        val hash = deriveKey(plainTextPassword, salt, iterations)

        return PasswordHashResult(
            hash = hash.toHexString(),
            salt = salt.toHexString(),
            iterations = iterations
        )
    }

    override fun verify(plainTextPassword: String, hash: String, salt: String, iterations: Int): Boolean {
        val expectedHash = hash.hexToByteArray()
        val saltBytes = salt.hexToByteArray()
        val actualHash = deriveKey(plainTextPassword, saltBytes, iterations)

        return MessageDigest.isEqual(expectedHash, actualHash)
    }

    private fun deriveKey(password: String, salt: ByteArray, rounds: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, rounds, keyLengthBits)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun ByteArray.toHexString(): String {
        return joinToString(separator = "") { "%02x".format(it) }
    }

    private fun String.hexToByteArray(): ByteArray {
        check(length % 2 == 0) { "Invalid hex length" }
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val DEFAULT_ITERATIONS = 210_000
        const val DEFAULT_SALT_LENGTH_BYTES = 16
        const val DEFAULT_KEY_LENGTH_BITS = 256
    }
}
