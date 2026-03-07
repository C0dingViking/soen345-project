package com.spinachtesters.spinachbooking.data

import com.google.firebase.database.FirebaseDatabase

object RootDatabaseProvider {
    val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
}
