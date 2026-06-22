package com.conzchat.app.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {
    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val db: FirebaseFirestore get() = Firebase.firestore
    val storage: FirebaseStorage get() = FirebaseStorage.getInstance()
    val messaging: FirebaseMessaging get() = FirebaseMessaging.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val currentUid: String get() = auth.currentUser?.uid ?: ""

    // Collection references
    val usersRef get() = db.collection("users")
    val messagesRef get() = db.collection("messages")
    fun groupMessagesRef(groupId: String) = db.collection("groups").document(groupId).collection("messages")
    fun publicGroupMessagesRef(groupId: String) = db.collection("publicGroups").document(groupId).collection("messages")
    val groupsRef get() = db.collection("groups")
    val publicGroupsRef get() = db.collection("publicGroups")
    val storiesRef get() = db.collection("stories")
    val callsRef get() = db.collection("calls")
    val suggestionsRef get() = db.collection("suggestions")
    val friendRequestsRef get() = db.collection("friendRequests")
    val dmTypingRef get() = db.collection("dmTyping")
    val sessionsRef get() = db.collection("sessions")
    val conzAIChatsRef get() = db.collection("conzAIChats")
    val chatSettingsRef get() = db.collection("chatSettings")
    val appConfigRef get() = db.collection("appConfig")

    fun updateFcmToken(token: String) {
        val uid = currentUid
        if (uid.isNotEmpty()) {
            usersRef.document(uid).update("fcmToken", token)
                .addOnFailureListener { }
        }
    }
}
