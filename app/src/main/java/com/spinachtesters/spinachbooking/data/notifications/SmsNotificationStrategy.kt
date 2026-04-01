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

class SmsNotificationStrategy(
    private val httpClient: OkHttpClient
) : NotificationStrategy {
    override fun canSend(request: NotificationRequest): Boolean {
        return request.phoneNumber.isNotBlank()
    }

    override suspend fun send(request: NotificationRequest) {
        val apiKey = BuildConfig.VONAGE_API_KEY
        val apiSecret = BuildConfig.VONAGE_API_SECRET
        val from = BuildConfig.VONAGE_FROM

        if (apiKey.isBlank() || apiSecret.isBlank() || from.isBlank()) {
            return
        }

        withContext(Dispatchers.IO) {
            val phone = "1" + request.phoneNumber;
            val payload = JSONObject()
                .put("to", phone)
                .put("from", from)
                .put("channel", "sms")
                .put("message_type", "text")
                .put("text", buildMessage(request))
                .toString()

            val apiRequest = Request.Builder()
                .url(VONAGE_MESSAGES_URL)
                .header("Authorization", Credentials.basic(apiKey, apiSecret))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(apiRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IOException("Vonage SMS request failed: code=${response.code}, body=$body")
                }
            }
        }
    }

    private companion object {
        private const val VONAGE_MESSAGES_URL = "https://api.nexmo.com/v1/messages"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
