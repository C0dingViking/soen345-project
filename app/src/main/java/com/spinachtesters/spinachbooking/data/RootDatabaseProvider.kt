package com.spinachtesters.spinachbooking.data

import com.google.firebase.database.FirebaseDatabase

object RootDatabaseProvider {
    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    val users = db.getReference("users")
    val bookings = db.getReference("bookings")
    val events = db.getReference("events")
    val eventDetails = db.getReference("event_details")
    val sportDetails = db.getReference("sport_details")
    val theaterDetails = db.getReference("theater_details")
    val concertDetails = db.getReference("concert_details")
    val  filmDetails = db.getReference("film_details")
}
