package com.conzchat.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.conzchat.app.ui.auth.WelcomeFragment
import com.conzchat.app.ui.home.HomeFragment
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.SessionManager
import com.conzchat.app.util.AppPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var authListener: FirebaseAuth.AuthStateListener? = null

    // Android 13+ notification permission launcher
    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — FCM still works, just no heads-up on 13+ if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Apply brightness from preferences
        val brightness = AppPreferences.getBrightness(this)
        applyBrightness(brightness)

        // Apply screenshot protection mod on startup
        if (com.conzchat.app.util.ConzMods.isScreenshotProtection(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        auth = FirebaseManager.auth

        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Register FCM token every time user is authenticated
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    FirebaseManager.updateFcmToken(token)
                }
                // Start session guard
                SessionManager.startSessionGuard(user.uid) {
                    runOnUiThread {
                        showSessionKickedDialog()
                    }
                }
                showHome()
            } else {
                SessionManager.stopSessionGuard()
                showWelcome()
            }
        }

        // Handle notification tap
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent ?: return
        val fromUid = intent.getStringExtra("fromUid") ?: return
        val notifType = intent.getStringExtra("notifType") ?: "message"
        // Delay to let auth + HomeFragment load first
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (FirebaseManager.currentUid.isEmpty()) return@postDelayed
            when (notifType) {
                "group" -> {
                    val chatId = intent.getStringExtra("chatId") ?: return@postDelayed
                    FirebaseManager.groupsRef.document(chatId).get().addOnSuccessListener { snap ->
                        val name = snap.getString("name") ?: "Group"
                        val photo = snap.getString("photo") ?: ""
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer,
                                com.conzchat.app.ui.groups.GroupChatFragment.newInstance(chatId, name, photo))
                            .addToBackStack(null).commitAllowingStateLoss()
                    }
                }
                else -> {
                    FirebaseManager.usersRef.document(fromUid).get().addOnSuccessListener { snap ->
                        val name = snap.getString("displayName") ?: snap.getString("username") ?: ""
                        val photo = snap.getString("photo") ?: ""
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer,
                                com.conzchat.app.ui.chat.ChatFragment.newInstance(fromUid, name, photo))
                            .addToBackStack(null).commitAllowingStateLoss()
                    }
                }
            }
        }, 1200)
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener!!)
        // Set online presence
        val uid = FirebaseManager.currentUid
        if (uid.isNotEmpty()) {
            FirebaseManager.usersRef.document(uid)
                .update("online", true, "lastSeen", System.currentTimeMillis())
        }
    }

    override fun onStop() {
        super.onStop()
        authListener?.let { auth.removeAuthStateListener(it) }
        // Set offline presence
        val uid = FirebaseManager.currentUid
        if (uid.isNotEmpty()) {
            FirebaseManager.usersRef.document(uid)
                .update("online", false, "lastSeen", System.currentTimeMillis())
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

    private fun showSessionKickedDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Account Logged In Elsewhere")
            .setMessage("Your account has been logged into on another device. You have been signed out for security.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                FirebaseManager.auth.signOut()
            }
            .show()
        // Auto sign out after 4 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            FirebaseManager.auth.signOut()
        }, 4000)
    }

    override fun onBackPressed() {
        // Just close normally — no exit dialog
        super.onBackPressed()
    }
}
