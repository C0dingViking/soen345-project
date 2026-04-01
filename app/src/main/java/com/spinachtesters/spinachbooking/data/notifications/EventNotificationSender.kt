package com.spinachtesters.spinachbooking.data.notifications

import com.spinachtesters.spinachbooking.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object EventNotificationSender {
    suspend fun sendBookingUpdate(
        phoneNumber: String,
        email: String,
        eventTitle: String,
        eventDate: String,
        eventTime: String,
        action: String
    ) {
        when {
            phoneNumber.isNotBlank() -> sendSms(
                phoneNumber,
                eventTitle,
                eventDate,
                eventTime,
                action
            )

            email.isNotBlank() -> sendEmail(email, eventTitle, eventDate, eventTime, action)
        }
    }

    private suspend fun sendSms(
        phoneNumber: String,
        eventTitle: String,
        eventDate: String,
        eventTime: String,
        action: String
    ) {
        val apiKey = BuildConfig.VONAGE_API_KEY
        val apiSecret = BuildConfig.VONAGE_API_SECRET
        val from = BuildConfig.VONAGE_FROM

        if (apiKey.isBlank() || apiSecret.isBlank() || from.isBlank()) {
            return
        }

        withContext(Dispatchers.IO) {
            val message = buildMessage(eventTitle, eventDate, eventTime, action)
            val payload = JSONObject()
                .put("to", phoneNumber)
                .put("from", from)
                .put("channel", "sms")
                .put("message_type", "text")
                .put("text", message)
                .toString()

            val request = Request.Builder()
                .url(VONAGE_MESSAGES_URL)
                .header("Authorization", Credentials.basic(apiKey, apiSecret))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    throw IOException("Vonage SMS request failed: code=${response.code}, body=$body")
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun sendEmail(
        email: String,
        eventTitle: String,
        eventDate: String,
        eventTime: String,
        action: String
    ) {
    }

    private fun buildMessage(
        eventTitle: String,
        eventDate: String,
        eventTime: String,
        action: String
    ): String {
        return when (action) {
            ACTION_BOOKING_CANCELLED -> {
                "Your booking has been canceled. Title: \"$eventTitle\", Date: $eventDate, Time: $eventTime."
            }

            ACTION_BOOKING_REGISTERED -> {
                "You are registered for this event. Title: \"$eventTitle\", Date: $eventDate, Time: $eventTime."
            }

            else -> {
                "Booking update. Title: \"$eventTitle\", Date: $eventDate, Time: $eventTime."
            }
        }
    }

    const val ACTION_BOOKING_REGISTERED = "booking_registered"
    const val ACTION_BOOKING_CANCELLED = "booking_cancelled"

    private const val VONAGE_MESSAGES_URL = "https://api.nexmo.com/v1/messages"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val httpClient = OkHttpClient()
}
