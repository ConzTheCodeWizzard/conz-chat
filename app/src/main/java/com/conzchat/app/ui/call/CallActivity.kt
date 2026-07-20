package com.conzchat.app.ui.call

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.conzchat.app.R
import com.conzchat.app.service.CallService

/**
 * Full-screen activity for incoming calls — shown on the lock screen.
 * Hosts CallFragment in a bare container.
 */
class CallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_call)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val fromUid = intent.getStringExtra(CallService.EXTRA_TO_UID) ?: return
        val fromName = intent.getStringExtra(CallService.EXTRA_TO_NAME) ?: "Unknown"
        val fromPhoto = intent.getStringExtra(CallService.EXTRA_TO_PHOTO) ?: ""
        val callType = intent.getStringExtra(CallService.EXTRA_CALL_TYPE) ?: "voice"
        val callId = intent.getStringExtra(CallService.EXTRA_CALL_ID) ?: ""
        val channel = intent.getStringExtra(CallService.EXTRA_CHANNEL) ?: ""
        val token = intent.getStringExtra(CallService.EXTRA_TOKEN) ?: ""
        val isIncoming = intent.getBooleanExtra(CallService.EXTRA_IS_INCOMING, true)

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
            .replace(R.id.callContainer, callFrag)
            .commitAllowingStateLoss()
    }
}
