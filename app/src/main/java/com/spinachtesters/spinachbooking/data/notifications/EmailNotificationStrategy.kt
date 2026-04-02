package com.spinachtesters.spinachbooking.data.notifications

import com.spinachtesters.spinachbooking.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class EmailNotificationStrategy(
    private val httpClient: OkHttpClient
) : NotificationStrategy {
    override fun canSend(request: NotificationRequest): Boolean {
        return request.email.isNotBlank()
    }

    override suspend fun send(request: NotificationRequest) {
        val apiKey = BuildConfig.MAILGUN_API_KEY
        val domain = BuildConfig.MAILGUN_DOMAIN
        val from = BuildConfig.MAILGUN_FROM_EMAIL

        if (apiKey.isBlank() || domain.isBlank() || from.isBlank()) {
            return
        }

        withContext(Dispatchers.IO) {
            val formBody = FormBody.Builder()
                .add("from", from)
                .add("to", request.email)
                .add("subject", buildSubject(request.action))
                .add("text", buildMessage(request))
                .build()

            val apiRequest = Request.Builder()
                .url("$MAILGUN_BASE_URL/$domain/messages")
                .header("Authorization", Credentials.basic("api", apiKey))
                .post(formBody)
                .build()

            httpClient.newCall(apiRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IOException("Mailgun email request failed: code=${response.code}, body=$body")
                }
            }
        }
    }

    private fun buildSubject(action: String): String {
        return when (action) {
            NotificationActions.BOOKING_CANCELLED -> "Event booking canceled"
            NotificationActions.BOOKING_REGISTERED -> "Event booking confirmed"
            else -> "Event booking update"
        }
    }

    private companion object {
        private const val MAILGUN_BASE_URL = "https://api.mailgun.net/v3"
    }
}
