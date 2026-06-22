package com.conzchat.app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class User(
    @DocumentId val uid: String = "",
    val username: String = "",
    val usernameLower: String = "",
    val displayName: String = "",
    val email: String = "",
    val photo: String = "",
    val coverPhoto: String = "",
    val status: String = "",
    val premium: Boolean = false,
    val banned: Boolean = false,
    val fcmToken: String = "",
    val created: Long = 0L,
    val friends: List<String> = emptyList(),
    val blockedUsers: List<String> = emptyList(),
    val forceLogout: Boolean = false,
    val logoutMessage: String = "",
    val premiumPopup: String = ""
)

data class Message(
    @DocumentId val id: String = "",
    val from: String = "",
    val to: String = "",
    val time: Long = 0L,
    val text: String = "",
    val type: String = "text", // text, image, video, voice, gif
    val url: String = "",
    val receipt: String = "S", // S=Sent, D=Delivered, R=Read
    val deleted: Boolean = false,
    val viewOnce: Boolean = false,
    val viewed: Boolean = false,
    val isCamera: Boolean = false,
    val transcript: String = "",
    val replyTo: ReplyTo? = null,
    val reactions: Map<String, String> = emptyMap()
)

data class ReplyTo(
    val id: String = "",
    val text: String = "",
    val sender: String = ""
)

data class GroupMessage(
    @DocumentId val id: String = "",
    val groupId: String = "",
    val from: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val time: Long = 0L,
    val text: String = "",
    val type: String = "text",
    val url: String = "",
    val deleted: Boolean = false,
    val viewOnce: Boolean = false,
    val viewed: Boolean = false,
    val isCamera: Boolean = false,
    val transcript: String = "",
    val replyTo: ReplyTo? = null,
    val reactions: Map<String, String> = emptyMap()
)

data class Group(
    @DocumentId val id: String = "",
    val name: String = "",
    val photo: String = "",
    val owner: String = "",
    val members: List<String> = emptyList(),
    val admins: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTime: Long = 0L
)

data class PublicGroup(
    @DocumentId val id: String = "",
    val tag: String = "",
    val name: String = "",
    val photo: String = "",
    val owner: String = "",
    val members: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTime: Long = 0L
)

data class Story(
    @DocumentId val id: String = "",
    val uid: String = "",
    val name: String = "",
    val photo: String = "",
    val type: String = "text", // text or image
    val text: String = "",
    val imageUrl: String = "",
    val time: Long = 0L,
    val expires: Long = 0L,
    val seenBy: List<String> = emptyList()
)

data class Call(
    @DocumentId val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerPhoto: String = "",
    val callType: String = "voice", // voice or video
    val status: String = "ringing", // ringing, answered, ended, declined
    val callTo: String = "",
    val callGroup: String = "",
    val callToGroup: List<String> = emptyList(),
    val time: Long = 0L
)

data class FriendRequest(
    @DocumentId val id: String = "",
    val from: String = "",
    val to: String = "",
    val status: String = "pending" // pending, accepted, declined
)

data class Suggestion(
    @DocumentId val id: String = "",
    val uid: String = "",
    val name: String = "",
    val text: String = "",
    val ts: Long = 0L,
    val replied: Boolean = false,
    val reply: String = ""
)

data class ConzAIMessage(
    @DocumentId val id: String = "",
    val role: String = "user", // user or assistant
    val content: String = "",
    val time: Long = 0L
)

data class ChatListItem(
    val uid: String,
    val name: String,
    val photo: String,
    val lastMessage: String,
    val lastTime: Long,
    val unreadCount: Int = 0,
    val type: String = "dm", // dm, group, publicGroup, conzAI
    val groupId: String = "",
    val publicGroupTag: String = ""
)

data class DmTyping(
    val from: String = "",
    val to: String = "",
    val typing: Boolean = false,
    val ts: Long = 0L
)

data class Session(
    val sessionId: String = "",
    val ts: Long = 0L
)

data class AppConfig(
    val version: String = "",
    val updateMessage: String = "",
    val updateTitle: String = ""
)

data class FeedPost(
    val id: String = "",
    val uid: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "", // image, video, or empty
    val timestamp: Long = 0L,
    val likes: List<String> = emptyList(),
    val commentCount: Int = 0
)

data class FeedComment(
    val id: String = "",
    val uid: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

data class CollectedAccountEntry(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val uid: String = "",
    val type: String = "login",
    val timestamp: Long = 0L
)
