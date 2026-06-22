package com.conzchat.app.util

import android.content.Context
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object OneSignalManager {

    private const val ONESIGNAL_APP_ID = "72d0a73d-b1ed-4ffa-9356-f84d79a0e0cc"
    private const val TAG = "OneSignalManager"
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Only verbose in debug builds
                OneSignal.Debug.logLevel = LogLevel.NONE
                OneSignal.initWithContext(context.applicationContext, ONESIGNAL_APP_ID)
                initialized = true
                Log.d(TAG, "OneSignal initialized")
            } catch (e: Exception) {
                Log.e(TAG, "OneSignal init failed: ${e.message}")
            }
        }
    }

    /**
     * Call after user logs in. Links this device to the user's UID so
     * notifications are delivered to the right person.
     */
    fun login(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                OneSignal.login(uid)
                Log.d(TAG, "OneSignal login: $uid")
            } catch (e: Exception) {
                Log.e(TAG, "OneSignal login failed: ${e.message}")
            }
        }
    }

    /**
     * Call when user logs out so this device stops receiving their notifications.
     */
    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                OneSignal.logout()
                Log.d(TAG, "OneSignal logout")
            } catch (e: Exception) {
                Log.e(TAG, "OneSignal logout failed: ${e.message}")
            }
        }
    }

    /**
     * Opt in to push notifications (call after user grants permission).
     */
    fun optIn() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                OneSignal.User.pushSubscription.optIn()
            } catch (e: Exception) {
                Log.e(TAG, "OneSignal optIn failed: ${e.message}")
            }
        }
    }
}
