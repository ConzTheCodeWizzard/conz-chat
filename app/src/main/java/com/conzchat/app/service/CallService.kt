package com.conzchat.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.conzchat.app.MainActivity
import com.conzchat.app.R

/**
 * CallService — foreground service for Agora RTC calls.
 * Keeps the call alive when the app is backgrounded.
 * Also handles incoming call notifications (accept/decline actions).
 */
class CallService : Service() {

    companion object {
        private const val TAG = "CallService"
        private const val CHANNEL_ID_CALL = "conzchat_call_active"
        private const val CHANNEL_ID_INCOMING = "conzchat_calls"
        private const val NOTIF_ID_ACTIVE = 1001
        private const val NOTIF_ID_INCOMING = 1002

        const val ACTION_START_CALL = "com.conzchat.app.START_CALL"
        const val ACTION_INCOMING_CALL = "com.conzchat.app.INCOMING_CALL"
        const val ACTION_ACCEPT_CALL = "com.conzchat.app.ACCEPT_CALL"
        const val ACTION_DECLINE_CALL = "com.conzchat.app.DECLINE_CALL"
        const val ACTION_END_CALL = "com.conzchat.app.END_CALL"

        const val EXTRA_TO_UID = "toUid"
        const val EXTRA_TO_NAME = "toName"
        const val EXTRA_TO_PHOTO = "toPhoto"
        const val EXTRA_CALL_TYPE = "callType"
        const val EXTRA_CALL_ID = "callId"
        const val EXTRA_CHANNEL = "channel"
        const val EXTRA_TOKEN = "agoraToken"
        const val EXTRA_IS_INCOMING = "isIncoming"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_CALL
        Log.d(TAG, "onStartCommand action=$action")

        when (action) {
            ACTION_START_CALL -> {
                val name = intent?.getStringExtra(EXTRA_TO_NAME) ?: "Unknown"
                val callType = intent?.getStringExtra(EXTRA_CALL_TYPE) ?: "voice"
                startForeground(NOTIF_ID_ACTIVE, buildActiveCallNotification(name, callType))
            }
            ACTION_INCOMING_CALL -> {
                val fromName = intent?.getStringExtra(EXTRA_TO_NAME) ?: "Unknown"
                val callType = intent?.getStringExtra(EXTRA_CALL_TYPE) ?: "voice"
                val fromUid = intent?.getStringExtra(EXTRA_TO_UID) ?: ""
                val fromPhoto = intent?.getStringExtra(EXTRA_TO_PHOTO) ?: ""
                val callId = intent?.getStringExtra(EXTRA_CALL_ID) ?: ""
                val channel = intent?.getStringExtra(EXTRA_CHANNEL) ?: ""
                val token = intent?.getStringExtra(EXTRA_TOKEN) ?: ""
                startForeground(NOTIF_ID_INCOMING, buildIncomingCallNotification(
                    fromName, callType, fromUid, fromPhoto, callId, channel, token
                ))
            }
            ACTION_ACCEPT_CALL -> {
                // Launch MainActivity with accept intent
                val fromUid = intent?.getStringExtra(EXTRA_TO_UID) ?: ""
                val fromName = intent?.getStringExtra(EXTRA_TO_NAME) ?: ""
                val fromPhoto = intent?.getStringExtra(EXTRA_TO_PHOTO) ?: ""
                val callType = intent?.getStringExtra(EXTRA_CALL_TYPE) ?: "voice"
                val callId = intent?.getStringExtra(EXTRA_CALL_ID) ?: ""
                val channel = intent?.getStringExtra(EXTRA_CHANNEL) ?: ""
                val token = intent?.getStringExtra(EXTRA_TOKEN) ?: ""

                val launchIntent = Intent(this, MainActivity::class.java).apply {
                    this.action = ACTION_ACCEPT_CALL
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(EXTRA_TO_UID, fromUid)
                    putExtra(EXTRA_TO_NAME, fromName)
                    putExtra(EXTRA_TO_PHOTO, fromPhoto)
                    putExtra(EXTRA_CALL_TYPE, callType)
                    putExtra(EXTRA_CALL_ID, callId)
                    putExtra(EXTRA_CHANNEL, channel)
                    putExtra(EXTRA_TOKEN, token)
                    putExtra(EXTRA_IS_INCOMING, true)
                }
                startActivity(launchIntent)
                stopSelf()
            }
            ACTION_DECLINE_CALL, ACTION_END_CALL -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Active call channel
            val activeChannel = NotificationChannel(
                CHANNEL_ID_CALL,
                "Active Calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing call notification"
                setShowBadge(false)
            }
            nm.createNotificationChannel(activeChannel)

            // Incoming call channel (high importance for heads-up)
            val incomingChannel = NotificationChannel(
                CHANNEL_ID_INCOMING,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming call alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500, 500, 500)
            }
            nm.createNotificationChannel(incomingChannel)
        }
    }

    private fun buildActiveCallNotification(callerName: String, callType: String): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val tapPi = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endIntent = Intent(this, CallService::class.java).apply { action = ACTION_END_CALL }
        val endPi = PendingIntent.getService(
            this, 1, endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_CALL)
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle("ConzChat — ${if (callType == "video") "Video" else "Voice"} Call")
            .setContentText("In call with $callerName")
            .setContentIntent(tapPi)
            .setOngoing(true)
            .addAction(R.drawable.ic_call, "End", endPi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun buildIncomingCallNotification(
        fromName: String,
        callType: String,
        fromUid: String,
        fromPhoto: String,
        callId: String,
        channel: String,
        token: String
    ): Notification {
        // Accept action
        val acceptIntent = Intent(this, CallService::class.java).apply {
            action = ACTION_ACCEPT_CALL
            putExtra(EXTRA_TO_UID, fromUid)
            putExtra(EXTRA_TO_NAME, fromName)
            putExtra(EXTRA_TO_PHOTO, fromPhoto)
            putExtra(EXTRA_CALL_TYPE, callType)
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_CHANNEL, channel)
            putExtra(EXTRA_TOKEN, token)
        }
        val acceptPi = PendingIntent.getService(
            this, 10, acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decline action
        val declineIntent = Intent(this, CallService::class.java).apply {
            action = ACTION_DECLINE_CALL
        }
        val declinePi = PendingIntent.getService(
            this, 11, declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tap notification → open app
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_ACCEPT_CALL
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_TO_UID, fromUid)
            putExtra(EXTRA_TO_NAME, fromName)
            putExtra(EXTRA_TO_PHOTO, fromPhoto)
            putExtra(EXTRA_CALL_TYPE, callType)
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_CHANNEL, channel)
            putExtra(EXTRA_TOKEN, token)
            putExtra(EXTRA_IS_INCOMING, true)
        }
        val tapPi = PendingIntent.getActivity(
            this, 12, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val typeLabel = if (callType == "video") "Video" else "Voice"
        return NotificationCompat.Builder(this, CHANNEL_ID_INCOMING)
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle("Incoming $typeLabel Call")
            .setContentText("$fromName is calling you")
            .setContentIntent(tapPi)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(R.drawable.ic_call, "✅ Accept", acceptPi)
            .addAction(R.drawable.ic_call, "❌ Decline", declinePi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(tapPi, true)
            .setVibrate(longArrayOf(0, 500, 500, 500, 500, 500))
            .build()
    }
}
