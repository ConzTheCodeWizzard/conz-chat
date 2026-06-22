package com.conzchat.app.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sends push notifications via OneSignal REST API.
 * Called from the sender's device whenever they send a message.
 * OneSignal delivers the notification to the recipient's device
 * even if their app is completely closed.
 */
object OneSignalNotifier {

    private const val TAG = "OneSignalNotifier"
    private const val APP_ID = "72d0a73d-b1ed-4ffa-9356-f84d79a0e0cc"
    private const val REST_API_KEY = "os_v2_app_olikopnr5vh7ve2w7bgxtihazqe4wqly24mumbvuy5rung43ujy3utgfgzypshsumarwxj47t7hbe3t7vw5xrl5mpnh7hyywzbvlx6y"
    private const val API_URL = "https://api.onesignal.com/notifications"

    /**
     * Send a DM notification to a specific user by their UID.
     * @param toUid Firebase UID of the recipient
     * @param senderName Display name of the sender
     * @param messageText The message text (or "📷 Photo", "🎤 Voice", etc.)
     * @param senderUid Firebase UID of the sender (for tap-to-open)
     */
    fun sendDmNotification(
        toUid: String,
        senderName: String,
        messageText: String,
        senderUid: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = JSONObject().apply {
                    put("app_id", APP_ID)
                    // Target by external_id (which we set to the user's Firebase UID)
                    put("include_aliases", JSONObject().apply {
                        put("external_id", JSONArray().apply { put(toUid) })
                    })
                    put("target_channel", "push")
                    put("headings", JSONObject().apply {
                        put("en", senderName)
                    })
                    put("contents", JSONObject().apply {
                        put("en", messageText)
                    })
                    put("data", JSONObject().apply {
                        put("fromUid", senderUid)
                        put("notifType", "message")
                    })
                    put("android_channel_id", "conzchat_messages")
                    put("priority", 10)
                    put("android_visibility", 1)
                }
                postToOneSignal(body)
            } catch (e: Exception) {
                Log.e(TAG, "sendDmNotification failed: ${e.message}")
            }
        }
    }

    /**
     * Send a group message notification to multiple users.
     * @param toUids List of Firebase UIDs to notify (excluding the sender)
     * @param groupName Name of the group
     * @param senderName Display name of the sender
     * @param messageText The message text
     * @param chatId The group chat ID (for tap-to-open)
     */
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
                val body = JSONObject().apply {
                    put("app_id", APP_ID)
                    put("include_aliases", JSONObject().apply {
                        put("external_id", JSONArray().apply {
                            toUids.forEach { put(it) }
                        })
                    })
                    put("target_channel", "push")
                    put("headings", JSONObject().apply {
                        put("en", groupName)
                    })
                    put("contents", JSONObject().apply {
                        put("en", "$senderName: $messageText")
                    })
                    put("data", JSONObject().apply {
                        put("chatId", chatId)
                        put("notifType", "group")
                    })
                    put("android_channel_id", "conzchat_groups")
                    put("priority", 8)
                }
                postToOneSignal(body)
            } catch (e: Exception) {
                Log.e(TAG, "sendGroupNotification failed: ${e.message}")
            }
        }
    }

    /**
     * Send a friend request notification.
     */
    fun sendFriendRequestNotification(toUid: String, fromName: String, fromUid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = JSONObject().apply {
                    put("app_id", APP_ID)
                    put("include_aliases", JSONObject().apply {
                        put("external_id", JSONArray().apply { put(toUid) })
                    })
                    put("target_channel", "push")
                    put("headings", JSONObject().apply {
                        put("en", "New Friend Request")
                    })
                    put("contents", JSONObject().apply {
                        put("en", "$fromName sent you a friend request")
                    })
                    put("data", JSONObject().apply {
                        put("fromUid", fromUid)
                        put("notifType", "friend_request")
                    })
                    put("android_channel_id", "conzchat_messages")
                    put("priority", 7)
                }
                postToOneSignal(body)
            } catch (e: Exception) {
                Log.e(TAG, "sendFriendRequestNotification failed: ${e.message}")
            }
        }
    }

    private fun postToOneSignal(body: JSONObject) {
        val url = URL(API_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Key $REST_API_KEY")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val writer = OutputStreamWriter(conn.outputStream)
        writer.write(body.toString())
        writer.flush()
        writer.close()

        val responseCode = conn.responseCode
        if (responseCode == 200 || responseCode == 201) {
            Log.d(TAG, "Notification sent successfully")
        } else {
            val error = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
            Log.e(TAG, "Notification failed [$responseCode]: $error")
        }
        conn.disconnect()
    }
}
