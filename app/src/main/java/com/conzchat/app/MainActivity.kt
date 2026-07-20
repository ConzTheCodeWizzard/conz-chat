package com.conzchat.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.content.Intent
import com.conzchat.app.service.CallService
import com.conzchat.app.ui.auth.WelcomeFragment
import com.conzchat.app.ui.call.CallFragment
import com.conzchat.app.ui.home.HomeFragment
import com.conzchat.app.util.ApiManager
import com.conzchat.app.util.AppPreferences
import com.conzchat.app.util.OneSignalManager
import com.onesignal.OneSignal
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationClickEvent

class MainActivity : AppCompatActivity() {

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) OneSignalManager.optIn()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        when {
            com.conzchat.app.util.ConzMods.isHarleyQuinnTheme(this) -> setTheme(R.style.Theme_ConzChat_HarleyQuinn)
            com.conzchat.app.util.ConzMods.isLightMode(this) -> setTheme(R.style.Theme_ConzChat_Light)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (com.conzchat.app.util.ConzMods.isHarleyQuinnTheme(this)) {
            val container = findViewById<android.widget.FrameLayout>(R.id.fragmentContainer)
            container.background = ContextCompat.getDrawable(this, R.drawable.bg_harley_quinn)
            container.foreground = android.graphics.drawable.ColorDrawable(0xAA0A0A12.toInt())
        }

        val brightness = AppPreferences.getBrightness(this)
        applyBrightness(brightness)

        if (com.conzchat.app.util.ConzMods.isScreenshotProtection(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Initialize ApiManager with application context
        ApiManager.init(applicationContext)

        if (ApiManager.isLoggedIn()) {
            // Restore session — fetch current user profile
            ApiManager.getMe { user, _ ->
                runOnUiThread {
                    if (user != null) {
                        ApiManager.currentUser = user
                        ApiManager.connectWebSocket()
                        // Re-link OneSignal to this user on session restore
                        OneSignalManager.login(user.uid)
                        OneSignalManager.optIn()
                        showHome()
                        setupOneSignalCallHandler()
                        // Handle deep link or incoming call after home loads
                        if (intent?.action == CallService.ACTION_ACCEPT_CALL) {
                            handleIncomingCallIntent(intent)
                        } else {
                            com.conzchat.app.util.DeepLinkHandler.handle(this, intent)
                        }
                    } else {
                        ApiManager.clearSession()
                        showWelcome()
                    }
                }
            }
        } else {
            showWelcome()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle incoming call accept from notification
        if (intent.action == CallService.ACTION_ACCEPT_CALL) {
            handleIncomingCallIntent(intent)
        } else {
            // Handle deep link when app is already running
            com.conzchat.app.util.DeepLinkHandler.handle(this, intent)
        }
    }

    private fun handleIncomingCallIntent(intent: Intent) {
        val fromUid = intent.getStringExtra(CallService.EXTRA_TO_UID) ?: return
        val fromName = intent.getStringExtra(CallService.EXTRA_TO_NAME) ?: "Unknown"
        val fromPhoto = intent.getStringExtra(CallService.EXTRA_TO_PHOTO) ?: ""
        val callType = intent.getStringExtra(CallService.EXTRA_CALL_TYPE) ?: "voice"
        val callId = intent.getStringExtra(CallService.EXTRA_CALL_ID) ?: ""
        val channel = intent.getStringExtra(CallService.EXTRA_CHANNEL) ?: ""
        val token = intent.getStringExtra(CallService.EXTRA_TOKEN) ?: ""
        val isIncoming = intent.getBooleanExtra(CallService.EXTRA_IS_INCOMING, true)

        if (fromUid.isEmpty()) return

        // Wait briefly for the fragment container to be ready
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val callFrag = CallFragment.newInstance(
                toUid = fromUid,
                toName = fromName,
                toPhoto = fromPhoto,
                callType = callType,
                isIncoming = isIncoming,
                callId = callId,
                channelName = channel,
                agoraToken = token
            )
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, callFrag)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }, 500)
    }

    fun setupOneSignalCallHandler() {
        // Handle notification click for incoming calls
        OneSignal.Notifications.addClickListener(object : INotificationClickListener {
            override fun onClick(event: INotificationClickEvent) {
                val data = event.notification.additionalData ?: return
                val notifType = data.optString("notifType", "")
                if (notifType == "incoming_call") {
                    val fromUid = data.optString("fromUid", "")
                    val fromName = data.optString("fromName", "Unknown")
                    val fromPhoto = data.optString("fromPhoto", "")
                    val callType = data.optString("callType", "voice")
                    val callId = data.optString("callId", "")
                    val channel = data.optString("channel", "")
                    val agoraToken = data.optString("agoraToken", "")
                    if (fromUid.isNotEmpty()) {
                        runOnUiThread {
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                val callFrag = CallFragment.newInstance(
                                    toUid = fromUid,
                                    toName = fromName,
                                    toPhoto = fromPhoto,
                                    callType = callType,
                                    isIncoming = true,
                                    callId = callId,
                                    channelName = channel,
                                    agoraToken = agoraToken
                                )
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainer, callFrag)
                                    .addToBackStack(null)
                                    .commitAllowingStateLoss()
                            }, 300)
                        }
                    }
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        // Update online status (fire-and-forget)
        val uid = ApiManager.currentUserId
        if (uid.isNotEmpty()) {
            ApiManager.updateFcmToken("offline_signal")
        }
    }

    fun showWelcome() {
        replaceFragment(WelcomeFragment())
    }

    fun showHome() {
        replaceFragment(HomeFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss()
    }

    fun applyBrightness(value: Int) {
        val lp = window.attributes
        lp.screenBrightness = 0.25f + (value / 100f) * 0.75f
        window.attributes = lp
    }

    fun showSessionKickedDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Account Logged In Elsewhere")
            .setMessage("Your account has been logged into on another device. You have been signed out for security.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                OneSignalManager.logout()
                ApiManager.clearSession()
                showWelcome()
            }
            .show()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            OneSignalManager.logout()
            ApiManager.clearSession()
            showWelcome()
        }, 4000)
    }

    override fun onDestroy() {
        super.onDestroy()
        com.conzchat.app.util.ConzMods.setAppUnlocked(this, false)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
