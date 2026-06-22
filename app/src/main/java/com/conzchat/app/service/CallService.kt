package com.conzchat.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.conzchat.app.ConzChatApp
import com.conzchat.app.MainActivity
import com.conzchat.app.R

class CallService : Service() {

    companion object {
        const val ACTION_START = "START_CALL"
        const val ACTION_END = "END_CALL"
        const val NOTIF_ID = 9001
        const val EXTRA_CALL_ID = "callId"
        const val EXTRA_CALLER_NAME = "callerName"
        const val EXTRA_CALL_TYPE = "callType"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
                val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Call"
                val callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "voice"
                startForeground(NOTIF_ID, buildNotification(callerName, callType, callId))
            }
            ACTION_END -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(callerName: String, callType: String, callId: String): Notification {
        val callIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("callId", callId)
            putExtra("callerName", callerName)
            putExtra("callType", callType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pi = PendingIntent.getActivity(
            this, 0, callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ConzChatApp.CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ConzChat ${if (callType == "video") "Video" else "Voice"} Call")
            .setContentText("In call with $callerName")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .build()
    }
}
