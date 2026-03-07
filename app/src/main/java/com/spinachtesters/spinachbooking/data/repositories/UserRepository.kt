package com.spinachtesters.spinachbooking.data.repositories

import com.spinachtesters.spinachbooking.data.RootDatabaseProvider
import com.spinachtesters.spinachbooking.domain.models.User

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
}
