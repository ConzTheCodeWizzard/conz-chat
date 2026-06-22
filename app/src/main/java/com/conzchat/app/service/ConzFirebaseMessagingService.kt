package com.conzchat.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.conzchat.app.ConzChatApp
import com.conzchat.app.MainActivity
import com.conzchat.app.R
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
            else -> showMessageNotification(title, body, fromUid, chatId, type)
        }
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

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(System.currentTimeMillis().toInt(), notification)
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
