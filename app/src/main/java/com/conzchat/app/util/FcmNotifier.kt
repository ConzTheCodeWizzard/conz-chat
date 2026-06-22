package com.conzchat.app.util

import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Sends push notifications via FCM v1 API using a service account JWT.
 * This approach works regardless of the sender's app version — only the
 * receiver needs to be on v4.0.7+ to have their FCM token stored in Firestore.
 *
 * Flow:
 * 1. Sender calls sendDmNotification / sendGroupNotification / sendFriendRequestNotification
 * 2. This class reads the receiver's fcmToken from Firestore (users/{uid}/fcmToken)
 * 3. Generates a short-lived OAuth2 JWT from the embedded service account private key
 * 4. Exchanges the JWT for an access token from Google's OAuth2 endpoint
 * 5. Sends the FCM v1 notification to the receiver's device token
 * 6. Receiver's device shows the notification even if the app is completely killed
 */
object FcmNotifier {

    private const val TAG = "FcmNotifier"
    private const val PROJECT_ID = "conzchat"
    private const val FCM_URL = "https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send"
    private const val TOKEN_URL = "https://oauth2.googleapis.com/token"
    private const val SERVICE_ACCOUNT_EMAIL = "firebase-adminsdk-fbsvc@conzchat.iam.gserviceaccount.com"
    private const val FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"

    // Service account private key (PKCS8, stripped of headers/newlines)
    // This is the private key from the Firebase service account JSON
    private const val PRIVATE_KEY_BASE64 =
        "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC+gUPXRM0QadvT" +
        "36+QzieOxKtkCLEwVqiUe6ovBbJc64nMNFFe+L4tjL7z06a3xYVtyvj+eBJsAfr7" +
        "1kV9eVmUu0uZmtsx6W6j+LN9FmRb9xZz5MKg1wB9r74QQq1O5fv1J9I5KNgas3zt" +
        "g3V9xZ7TqVOK7M6yhSYHoXW4wQqSm6wxOfTSJsgZ936g0FNlszj5tMtcbRM14pMr" +
        "5hM6T93XTf3GCrClw26wOXhVHfG3SfaF8MPwht6tpfqXeYi7zj5CJ7ggVtO/Wf8n" +
        "JAHjPEiYbrhdDWEwhO1Px39YtD4X6u2uH/wnvUbn12oOH4UTJovip5Mn7HnH/3s+" +
        "yfFo4+ktAgMBAAECggEAMgK0QQxmOvNXSrEzoTOFbfTSO4IOpe5x9fza6do4fy00" +
        "MP/hBqoNHt8Od+rtntvDEdYRdJgn2ePRNBCFkJFtQ17B03K0LU+nBHOzBtj8gqoE" +
        "YmtA7sFSdyxOHG2smoRElxCbnvbKeIP6/U0f6GMbNVyDuno9Lt9GYwVvZo+Cy4aP" +
        "WznprIMSJ1ZvivgvDUjUFHZNMtfyZbakkLhB3tdjNj6qOiZR++Q4z9eboyyL43N9" +
        "B01JlI5Npokp1czRrcIlzLd3mLzrqNvFiTQ9AhBkGXihxHTdFS4MHkIBVDzpnb4x" +
        "4dzRv9dxxmaFFlAmTWQdTiaDfuwA/a9RdoN3LzMX/wKBgQDoxFltyIQNb+IKEyAc" +
        "+A7hCLcSgDd9+7yZARTgzuoUa8FlqZVGJjt7w8HKKdW/MsB33J+d9A/af4ebuJ6D" +
        "m3Ry34St6C5DT7XMqJ+BX4ZpBqBz5RBuIMr2qXvxxyOJPIslJMMrOmFhIgAEeqlJ" +
        "9qP65mmp9IwuQGbVPTURQIPo7wKBgQDRhQmzkvcj1DUP0208+dOcfvBhRJbRqqd1" +
        "E6Etq+AgwVsFv52B+bLlgjJsKG/u2CQn7/vpOgv7j0d8R/MQ7q855HHMQ5+D28Yt" +
        "SWCs+I5IeXGqrzKe03nfIoo0Bo/O9C5/3r1xrdOgWImH5DI+RdFShfb766P4REwz" +
        "flev01j3owKBgQDGnv28q1TbbnRQqn9FmPqBVgF5/QTdMRl+6ihZqNaftE9kI7Ao" +
        "Gum5M5LCoq4tJ+6KmS1vCFsa1KXv4DsLDHTyrP63sx7++x6j0+O/7rZwGmKCYp2B" +
        "i+QFVRxZdNdC/PGzMUqFMZz02KFxMQYSAi9tIn5Zsz6HHRdisIb/ALFO6wKBgDOO" +
        "uE9vJ8+yRYhqb6QkmfUVq5NT7IUVqHV/6hayB8onqn1kpJ66UlJ10nCZFspAd804" +
        "GdZPiWlS3bVwgxi0k3v8giBQt60b5M16FAccdu7Qq67jw+Ifigrllfqtbq/vmI2w" +
        "ww95Re1cPCOrfM2kIplbC+b8GnJaZCH7whorOOZfAoGBAMwQvC0giJxHun1tnXf5" +
        "TRdWjO87jxRbHjLUM4MwLZ2JtbYcVYCJWXz5onCze8mas3/fxWjAeu+EWP9CIGLQ" +
        "EPkICyx3Xwg+IkikQa4lhJzmBbXEU1eZJXyelFFtR1iCoL1ghqUJq035rBX/YMta" +
        "cEmY5EUVD6hP62F2yLHvFOXf"

    // Cache the access token to avoid generating a new JWT on every message
    @Volatile private var cachedAccessToken: String? = null
    @Volatile private var tokenExpiryMs: Long = 0L

    fun sendDmNotification(
        toUid: String,
        senderName: String,
        messageText: String,
        senderUid: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getFcmToken(toUid) ?: return@launch
                val accessToken = getAccessToken() ?: return@launch
                val body = buildFcmMessage(
                    deviceToken = token,
                    title = senderName,
                    body = messageText,
                    data = mapOf(
                        "fromUid" to senderUid,
                        "notifType" to "message"
                    ),
                    channelId = "conzchat_messages"
                )
                sendFcmMessage(body, accessToken)
            } catch (e: Exception) {
                Log.e(TAG, "sendDmNotification failed: ${e.message}")
            }
        }
    }

    fun sendGroupNotification(
        toUids: List<String>,
        groupName: String,
        senderName: String,
        messageText: String,
        chatId: String
    ) {
        if (toUids.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getAccessToken() ?: return@launch
                for (uid in toUids) {
                    val token = getFcmToken(uid) ?: continue
                    val body = buildFcmMessage(
                        deviceToken = token,
                        title = groupName,
                        body = "$senderName: $messageText",
                        data = mapOf(
                            "chatId" to chatId,
                            "notifType" to "group"
                        ),
                        channelId = "conzchat_groups"
                    )
                    sendFcmMessage(body, accessToken)
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendGroupNotification failed: ${e.message}")
            }
        }
    }

    fun sendFriendRequestNotification(toUid: String, fromName: String, fromUid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getFcmToken(toUid) ?: return@launch
                val accessToken = getAccessToken() ?: return@launch
                val body = buildFcmMessage(
                    deviceToken = token,
                    title = "New Friend Request",
                    body = "$fromName sent you a friend request",
                    data = mapOf(
                        "fromUid" to fromUid,
                        "notifType" to "friend_request"
                    ),
                    channelId = "conzchat_messages"
                )
                sendFcmMessage(body, accessToken)
            } catch (e: Exception) {
                Log.e(TAG, "sendFriendRequestNotification failed: ${e.message}")
            }
        }
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private suspend fun getFcmToken(uid: String): String? {
        return try {
            val snap = FirebaseFirestore.getInstance()
                .collection("users").document(uid).get().await()
            val token = snap.getString("fcmToken")
            if (token.isNullOrEmpty()) {
                Log.w(TAG, "No FCM token for uid=$uid")
                null
            } else token
        } catch (e: Exception) {
            Log.e(TAG, "getFcmToken failed for uid=$uid: ${e.message}")
            null
        }
    }

    private fun buildFcmMessage(
        deviceToken: String,
        title: String,
        body: String,
        data: Map<String, String>,
        channelId: String
    ): JSONObject {
        return JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", deviceToken)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                })
                put("android", JSONObject().apply {
                    put("priority", "high")
                    put("notification", JSONObject().apply {
                        put("channel_id", channelId)
                        put("sound", "default")
                        put("notification_priority", "PRIORITY_HIGH")
                        put("visibility", "PUBLIC")
                    })
                })
                put("data", JSONObject().apply {
                    data.forEach { (k, v) -> put(k, v) }
                })
            })
        }
    }

    private fun sendFcmMessage(body: JSONObject, accessToken: String) {
        val url = URL(FCM_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val writer = OutputStreamWriter(conn.outputStream)
        writer.write(body.toString())
        writer.flush()
        writer.close()

        val responseCode = conn.responseCode
        if (responseCode == 200) {
            Log.d(TAG, "FCM notification sent successfully")
        } else {
            val error = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
            Log.e(TAG, "FCM notification failed [$responseCode]: $error")
        }
        conn.disconnect()
    }

    /**
     * Gets a valid OAuth2 access token for FCM v1 API.
     * Uses a cached token if still valid (expires in 1 hour).
     * Otherwise generates a new JWT and exchanges it for an access token.
     */
    @Synchronized
    private fun getAccessToken(): String? {
        val now = System.currentTimeMillis()
        // Return cached token if still valid (with 5 min buffer)
        if (cachedAccessToken != null && now < tokenExpiryMs - 300_000) {
            return cachedAccessToken
        }
        return try {
            val jwt = buildJwt()
            val token = exchangeJwtForAccessToken(jwt)
            cachedAccessToken = token
            tokenExpiryMs = now + 3600_000 // 1 hour
            token
        } catch (e: Exception) {
            Log.e(TAG, "getAccessToken failed: ${e.message}")
            null
        }
    }

    /**
     * Builds a signed JWT for service account authentication.
     * Format: base64(header).base64(claims).base64(signature)
     */
    private fun buildJwt(): String {
        val nowSecs = System.currentTimeMillis() / 1000
        val expSecs = nowSecs + 3600

        val header = JSONObject().apply {
            put("alg", "RS256")
            put("typ", "JWT")
        }
        val claims = JSONObject().apply {
            put("iss", SERVICE_ACCOUNT_EMAIL)
            put("scope", FCM_SCOPE)
            put("aud", TOKEN_URL)
            put("iat", nowSecs)
            put("exp", expSecs)
        }

        val headerEncoded = base64UrlEncode(header.toString().toByteArray())
        val claimsEncoded = base64UrlEncode(claims.toString().toByteArray())
        val signingInput = "$headerEncoded.$claimsEncoded"

        val keyBytes = Base64.decode(PRIVATE_KEY_BASE64, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec)

        val signature = Signature.getInstance("SHA256withRSA").apply {
            initSign(privateKey)
            update(signingInput.toByteArray())
        }.sign()

        val signatureEncoded = base64UrlEncode(signature)
        return "$signingInput.$signatureEncoded"
    }

    private fun exchangeJwtForAccessToken(jwt: String): String {
        val url = URL(TOKEN_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val postData = "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=$jwt"
        val writer = OutputStreamWriter(conn.outputStream)
        writer.write(postData)
        writer.flush()
        writer.close()

        val responseCode = conn.responseCode
        val response = if (responseCode == 200) {
            conn.inputStream.bufferedReader().readText()
        } else {
            val error = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown"
            throw Exception("Token exchange failed [$responseCode]: $error")
        }
        conn.disconnect()

        val json = JSONObject(response)
        return json.getString("access_token")
    }

    private fun base64UrlEncode(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}
