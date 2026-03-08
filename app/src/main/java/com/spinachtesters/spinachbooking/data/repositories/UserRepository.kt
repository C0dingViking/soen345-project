package com.spinachtesters.spinachbooking.data.repositories

import com.spinachtesters.spinachbooking.data.RootDatabaseProvider
import com.spinachtesters.spinachbooking.domain.models.User
import java.util.UUID

class UserRepository(
    private val source: FirebaseRepository<User> =
        FirebaseRepository(User::class.java, RootDatabaseProvider.users)
) {
    suspend fun save(id: String, item: User) {
        source.save(id, item)
    }

    suspend fun getById(id: String): User? {
        return source.getById(id)
    }

    suspend fun getAll(): List<User> {
        return source.getAll()
    }

    suspend fun usernameExists(username: String): Boolean {
        return getAll().any { it.username.equals(username, ignoreCase = true) }
    }

    suspend fun emailExists(email: String): Boolean {
        return getAll().any { it.email.equals(email, ignoreCase = true) }
    }

    suspend fun phoneExists(phone: String): Boolean {
        return getAll().any { it.phoneNb == phone }
    }

    suspend fun create(user: User): User {
        val id = user.id.ifBlank { UUID.randomUUID().toString() }
        val saved = user.copy(id = id)
        save(id, saved)
        return saved
    }

    suspend fun findByLoginIdentifier(identifier: String): User? {
        val normalized = identifier.trim()
        return getAll().firstOrNull {
            it.username.equals(normalized, ignoreCase = true) ||
                it.email.equals(normalized, ignoreCase = true) ||
                it.phoneNb == normalized
        }
    }
}
