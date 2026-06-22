package com.conzchat.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.conzchat.app.ConzChatApp
import com.conzchat.app.MainActivity
import com.conzchat.app.R
import com.conzchat.app.util.ConzMods
import com.conzchat.app.util.FirebaseManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ConzFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirebaseManager.updateFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: "ConzChat"
        val body = notification?.body ?: data["body"] ?: "New message"
        val type = data["type"] ?: "message"
        val fromUid = data["fromUid"] ?: ""
        val chatId = data["chatId"] ?: ""

        when (type) {
            "call" -> handleIncomingCallNotification(data)
            else -> {
                showMessageNotification(title, body, fromUid, chatId, type)
                // Auto-Reply: if enabled, send an automatic reply to DMs
                if (type == "message" && fromUid.isNotEmpty() && ConzMods.isAutoReply(this)) {
                    sendAutoReply(fromUid)
                }
            }
        }
    }

    private fun sendAutoReply(toUid: String) {
        val myUid = FirebaseManager.currentUid
        if (myUid.isEmpty() || toUid == myUid) return

        val replyMsg = ConzMods.getAutoReplyMsg(this)
        if (replyMsg.isEmpty()) return

        val msgData = hashMapOf<String, Any>(
            "from" to myUid,
            "to" to toUid,
            "time" to System.currentTimeMillis(),
            "type" to "text",
            "text" to replyMsg,
            "receipt" to "S",
            "isAutoReply" to true
        )
        FirebaseManager.messagesRef.add(msgData)
    }

    private fun showMessageNotification(
        title: String,
        body: String,
        fromUid: String,
        chatId: String,
        type: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("fromUid", fromUid)
            putExtra("chatId", chatId)
            putExtra("notifType", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channel = when (type) {
            "group" -> ConzChatApp.CHANNEL_GROUPS
            else -> ConzChatApp.CHANNEL_MESSAGES
        }

        // Use the notification sound mod setting
        val soundUri = getNotificationSoundUri()

        // Vibration pattern based on notification sound setting
        val vibrate = when (ConzMods.getNotifSound(this)) {
            "buzz" -> longArrayOf(0, 500, 200, 500)
            "pop" -> longArrayOf(0, 100)
            "chime" -> longArrayOf(0, 200, 100, 200)
            else -> longArrayOf(0, 250, 250, 250)
        }

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(vibrate)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getNotificationSoundUri(): Uri {
        return when (ConzMods.getNotifSound(this)) {
            "pop" -> {
                // Use a short notification sound
                try {
                    Uri.parse("android.resource://${packageName}/${R.raw.notif_pop}")
                } catch (_: Exception) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
            }
            "chime" -> {
                try {
                    Uri.parse("android.resource://${packageName}/${R.raw.notif_chime}")
                } catch (_: Exception) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
            }
            "buzz" -> {
                // Buzz uses vibration primarily, minimal sound
                try {
                    Uri.parse("android.resource://${packageName}/${R.raw.notif_buzz}")
                } catch (_: Exception) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
            }
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    private fun handleIncomingCallNotification(data: Map<String, String>) {
        val callId = data["callId"] ?: return
        val callerName = data["callerName"] ?: "Unknown"
        val callerPhoto = data["callerPhoto"] ?: ""
        val callType = data["callType"] ?: "voice"

        // Launch MainActivity with call extras - it will navigate to CallFragment
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("callId", callId)
            putExtra("callerName", callerName)
            putExtra("callerPhoto", callerPhoto)
            putExtra("callType", callType)
            putExtra("isIncomingCall", true)
        }
        startActivity(intent)
    }
}
