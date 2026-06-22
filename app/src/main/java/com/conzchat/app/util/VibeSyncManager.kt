package com.conzchat.app.util

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object VibeSyncManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listener: ListenerRegistration? = null
    private var pulseAnimator: ValueAnimator? = null

    fun getVibeColor(myUid: String, theirUid: String): Int {
        val hue = ((myUid.hashCode() xor theirUid.hashCode()) and Int.MAX_VALUE) % 360
        return Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.7f, 0.9f))
    }

    fun startVibeSync(context: Context, chatId: String, theirUid: String, targetView: View) {
        if (!ConzMods.isVibeSync(context)) return
        val uid = auth.currentUser?.uid ?: return
        val vibeColor = getVibeColor(uid, theirUid)
        listener = db.collection("users").document(theirUid)
            .addSnapshotListener { snap, _ ->
                val online = snap?.getBoolean("online") ?: false
                if (online) {
                    startPulse(targetView, vibeColor)
                    SoundManager.play(context, "vibe_sync")
                } else {
                    stopPulse(targetView)
                }
            }
    }

    private fun startPulse(view: View, color: Int) {
        pulseAnimator?.cancel()
        pulseAnimator = ValueAnimator.ofFloat(0.3f, 1f).apply {
            duration = 1200L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                val alpha = (anim.animatedValue as Float * 255).toInt()
                view.setBackgroundColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)))
            }
            start()
        }
    }

    private fun stopPulse(view: View) {
        pulseAnimator?.cancel()
        pulseAnimator = null
        view.setBackgroundColor(0)
    }

    fun stop(view: View) {
        listener?.remove()
        listener = null
        stopPulse(view)
    }
}
