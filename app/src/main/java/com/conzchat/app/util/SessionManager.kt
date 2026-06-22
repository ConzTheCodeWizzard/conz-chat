package com.conzchat.app.util

import com.conzchat.app.util.FirebaseManager.db
import com.google.firebase.firestore.ListenerRegistration

object SessionManager {
    private var mySessionId: String = ""
    private var sessionListener: ListenerRegistration? = null
    private var onKicked: (() -> Unit)? = null

    fun startSessionGuard(uid: String, onKickedCallback: () -> Unit) {
        onKicked = onKickedCallback
        mySessionId = System.currentTimeMillis().toString(36) + Math.random().toString().substring(2)
        sessionListener?.remove()

        db.collection("sessions").document(uid)
            .set(mapOf("sessionId" to mySessionId, "ts" to System.currentTimeMillis()))
            .addOnSuccessListener {
                // Wait 2 seconds before subscribing to avoid false positives
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    val currentUser = FirebaseManager.auth.currentUser
                    if (currentUser == null || currentUser.uid != uid) return@postDelayed

                    sessionListener = db.collection("sessions").document(uid)
                        .addSnapshotListener { snap, _ ->
                            if (snap == null || !snap.exists()) return@addSnapshotListener
                            val remoteSessionId = snap.getString("sessionId") ?: return@addSnapshotListener
                            if (remoteSessionId != mySessionId) {
                                sessionListener?.remove()
                                sessionListener = null
                                onKicked?.invoke()
                            }
                        }
                }, 2000)
            }
    }

    fun stopSessionGuard() {
        sessionListener?.remove()
        sessionListener = null
        onKicked = null
    }
}
