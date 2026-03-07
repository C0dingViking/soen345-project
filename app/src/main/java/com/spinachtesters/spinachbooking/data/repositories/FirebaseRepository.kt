package com.spinachtesters.spinachbooking.data.repositories

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class FirebaseRepository<T : Any>(
    private val classType: Class<T>,
    private val ref: DatabaseReference
) {
    suspend fun save(id: String, item: T) {
        ref.child(id).setValue(item).await()
    }

    suspend fun getById(id: String): T? {
        val snapshot = ref.child(id).get().await()
        return snapshot.getValue(classType)
    }

    suspend fun getAll(): List<T> {
        val snapshot = ref.get().await()
        return snapshot.children.mapNotNull { it.getValue(classType) }
    }
}
