package com.conzchat.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp

class ConzChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            // Messages channel
            NotificationChannel(
                CHANNEL_MESSAGES,
                getString(R.string.channel_messages_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_messages_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                nm.createNotificationChannel(this)
            }

            // Calls channel
            NotificationChannel(
                CHANNEL_CALLS,
                getString(R.string.channel_calls_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_calls_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                nm.createNotificationChannel(this)
            }

            // Groups channel
            NotificationChannel(
                CHANNEL_GROUPS,
                getString(R.string.channel_groups_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_groups_desc)
                nm.createNotificationChannel(this)
            }
        }
    }

    companion object {
        const val CHANNEL_MESSAGES = "conzchat_messages"
        const val CHANNEL_CALLS = "conzchat_calls"
        const val CHANNEL_GROUPS = "conzchat_groups"
        const val DEV_UID = BuildConfig.DEV_UID
    }
}
