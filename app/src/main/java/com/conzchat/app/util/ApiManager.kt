package com.conzchat.app.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.conzchat.app.model.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * ApiManager — replaces FirebaseManager entirely.
 * All network calls go through this singleton.
 * Uses OkHttp for REST and WebSocket.
 */
object ApiManager {

    // ─── Configuration ────────────────────────────────────────────────────────
    // Change this to your deployed server URL before building release APK
    private val BASE_URL_DEFAULT get() = SecureConfig.serverBaseUrl()
    private const val PREFS_NAME = "conzchat_api_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_BASE_URL = "base_url"
    // Conversation partners are keyed per user: "conv_partners_<userId>" so they survive logout

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    val gson: Gson = GsonBuilder().serializeNulls().create()

    private lateinit var prefs: SharedPreferences
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Current user state (in-memory cache)
    var currentUser: User? = null
    var currentUserId: String = ""
    var currentUsername: String = ""

    // WebSocket
    private var webSocket: WebSocket? = null
    private var wsListeners = mutableListOf<WebSocketListener2>()
    private var wsReconnectAttempts = 0

    // ─── Init ─────────────────────────────────────────────────────────────────

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_TOKEN, null)
        if (token != null) {
            currentUserId = prefs.getString(KEY_USER_ID, "") ?: ""
            currentUsername = prefs.getString(KEY_USERNAME, "") ?: ""
        }
    }

    fun getBaseUrl(): String = prefs.getString(KEY_BASE_URL, BASE_URL_DEFAULT) ?: BASE_URL_DEFAULT

    fun setBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isLoggedIn(): Boolean = getToken() != null && currentUserId.isNotEmpty()

    /** Replace localhost URLs with the configured base URL so photos load on device */
    fun normalizeUrl(url: String?): String? {
        if (url.isNullOrEmpty()) return url
        val base = getBaseUrl()
        return url
            .replace("http://localhost:8080", base)
            .replace("http://127.0.0.1:8080", base)
    }

    fun saveSession(token: String, user: User) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, user.uid)
            .putString(KEY_USERNAME, user.username)
            .apply()
        currentUserId = user.uid
        currentUsername = user.username
        currentUser = user
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            // NOTE: conv_partners_<uid> keys are intentionally kept so the chat list
            // is restored when the same user logs back in.
            .apply()
        currentUserId = ""
        currentUsername = ""
        currentUser = null
        disconnectWebSocket()
    }

    // ─── HTTP Helpers ─────────────────────────────────────────────────────────

    private fun buildRequest(path: String, method: String = "GET", body: Any? = null): Request {
        val url = "${getBaseUrl()}$path"
        val token = getToken()
        val builder = Request.Builder().url(url)
        if (token != null) {
            builder.addHeader("Authorization", "Bearer $token")
        }
        return when (method.uppercase()) {
            "POST" -> {
                val json = if (body != null) gson.toJson(body) else "{}"
                builder.post(json.toRequestBody(JSON_MEDIA_TYPE)).build()
            }
            "PUT" -> {
                val json = if (body != null) gson.toJson(body) else "{}"
                builder.put(json.toRequestBody(JSON_MEDIA_TYPE)).build()
            }
            "DELETE" -> builder.delete().build()
            else -> builder.get().build()
        }
    }

    private fun <T> execute(request: Request, type: TypeToken<T>, callback: (T?, String?) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e.message ?: "Network error")
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errMsg = try {
                        val err = gson.fromJson(body, Map::class.java)
                        err["error"] as? String ?: "Error ${response.code}"
                    } catch (e: Exception) {
                        "Error ${response.code}"
                    }
                    callback(null, errMsg)
                    return
                }
                try {
                    val result = gson.fromJson<T>(body, type.type)
                    callback(result, null)
                } catch (e: Exception) {
                    callback(null, "Parse error: ${e.message}")
                }
            }
        })
    }

    private fun executeVoid(request: Request, callback: (Boolean, String?) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message ?: "Network error")
            }
            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful, if (!response.isSuccessful) "Error ${response.code}" else null)
            }
        })
    }

    // ─── Auth ─────────────────────────────────────────────────────────────────

    fun login(username: String, password: String, callback: (User?, String?) -> Unit) {
        val body = mapOf("username" to username, "password" to password)
        val req = buildRequest("/api/auth/login", "POST", body)
        execute(req, object : TypeToken<AuthResponse>() {}, { resp, err ->
            if (resp != null) {
                saveSession(resp.token, resp.user)
                callback(resp.user, null)
            } else {
                callback(null, err)
            }
        })
    }

    fun register(username: String, password: String, displayName: String, callback: (User?, String?) -> Unit) {
        val body = mapOf("username" to username, "password" to password, "displayName" to displayName)
        val req = buildRequest("/api/auth/register", "POST", body)
        execute(req, object : TypeToken<AuthResponse>() {}, { resp, err ->
            if (resp != null) {
                saveSession(resp.token, resp.user)
                callback(resp.user, null)
            } else {
                callback(null, err)
            }
        })
    }

    fun getMe(callback: (User?, String?) -> Unit) {
        val req = buildRequest("/api/me")
        execute(req, object : TypeToken<User>() {}, callback)
    }

    fun logout() {
        clearSession()
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    fun getUser(uid: String, callback: (User?, String?) -> Unit) {
        val req = buildRequest("/api/users/$uid")
        execute(req, object : TypeToken<User>() {}, callback)
    }

    fun searchUsers(query: String, callback: (List<User>?, String?) -> Unit) {
        val req = buildRequest("/api/users/search?q=${query.encodeUrl()}")
        execute(req, object : TypeToken<List<User>>() {}, callback)
    }

    fun updateProfile(displayName: String, photo: String, coverPhoto: String, status: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("displayName" to displayName, "photo" to photo, "coverPhoto" to coverPhoto, "status" to status)
        val req = buildRequest("/api/me", "PUT", body)
        executeVoid(req, callback)
    }

    fun updateFcmToken(token: String) {
        val body = mapOf("token" to token)
        val req = buildRequest("/api/me/fcm-token", "POST", body)
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) { response.close() }
        })
    }

    // ─── Messages ─────────────────────────────────────────────────────────────

    fun getMessages(uid: String, limit: Int = 200, offset: Int = 0, callback: (List<Message>?, String?) -> Unit) {
        val req = buildRequest("/api/messages/$uid?limit=$limit&offset=$offset")
        execute(req, object : TypeToken<List<Message>>() {}, callback)
    }

    // ─── Conversation Partner Tracking ───────────────────────────────────────
    /** Returns the SharedPreferences key for this user's conversation partners */
    private fun convPartnersKey(userId: String = currentUserId) = "conv_partners_$userId"

    /** Save a uid as a known DM conversation partner (persisted across logins for this user) */
    fun addConversationPartner(uid: String) {
        if (uid.isEmpty() || uid == currentUserId || currentUserId.isEmpty()) return
        val existing = getConversationPartners().toMutableSet()
        if (existing.add(uid)) {
            prefs.edit().putString(convPartnersKey(), gson.toJson(existing)).apply()
        }
    }

    /** Get all known DM conversation partner uids for the current user */
    fun getConversationPartners(): Set<String> {
        if (currentUserId.isEmpty()) return emptySet()
        val json = prefs.getString(convPartnersKey(), null) ?: return emptySet()
        return try {
            gson.fromJson(json, object : TypeToken<Set<String>>() {}.type)
        } catch (e: Exception) { emptySet() }
    }

    /**
     * Fetch conversation partners from the server sidecar and merge into local cache.
     * This ensures the receiver sees DMs even if their app was closed when the message arrived.
     */
    fun fetchConversationsFromServer(callback: (Set<String>) -> Unit) {
        val token = prefs.getString(KEY_TOKEN, null) ?: run { callback(emptySet()); return }
        val uid = currentUserId.ifEmpty { run { callback(emptySet()); return } }
        val sidecarUrl = getBaseUrl().replace(":8080", ":8081")
        val url = "$sidecarUrl/api/conversations?uid=$uid&token=${java.net.URLEncoder.encode(token, "UTF-8")}"
        // Use a short timeout so a slow/overloaded sidecar doesn't block the UI
        val shortClient = client.newBuilder()
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val req = okhttp3.Request.Builder().url(url).get().build()
        shortClient.newCall(req).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.w("ApiManager", "fetchConversations failed: ${e.message}")
                callback(emptySet())
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string() ?: run { callback(emptySet()); return }
                try {
                    val map = gson.fromJson(body, object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type) as? Map<*, *>
                    @Suppress("UNCHECKED_CAST")
                    val partners = (map?.get("partners") as? List<*>)?.mapNotNull { it?.toString() }?.toSet() ?: emptySet()
                    // Merge into local cache
                    partners.forEach { addConversationPartner(it) }
                    callback(getConversationPartners())
                } catch (e: Exception) {
                    Log.w("ApiManager", "fetchConversations parse error: ${e.message}")
                    callback(emptySet())
                }
            }
        })
    }

    /** Clear conversation partners for the current user (only if explicitly needed) */
    fun clearConversationPartners() {
        if (currentUserId.isEmpty()) return
        prefs.edit().remove(convPartnersKey()).apply()
    }

    fun sendMessage(
        to: String, text: String, type: String = "text", url: String = "",
        viewOnce: Boolean = false, isCamera: Boolean = false, isGallery: Boolean = false, transcript: String = "",
        replyTo: ReplyTo? = null,
        callback: (Message?, String?) -> Unit
    ) {
        // Track this as a known conversation partner
        addConversationPartner(to)
        val body = mutableMapOf<String, Any>(
            "to" to to, "text" to text, "type" to type, "url" to url,
            "viewOnce" to viewOnce, "isCamera" to isCamera, "isGallery" to isGallery, "transcript" to transcript
        )
        if (replyTo != null) {
            body["replyToId"] = replyTo.id.toIntOrNull() ?: 0
            body["replyToText"] = replyTo.text
            body["replyToSender"] = replyTo.sender
        }
        val req = buildRequest("/api/messages", "POST", body)
        execute(req, object : TypeToken<Message>() {}, callback)
    }

    fun markMessagesRead(fromUid: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/messages/$fromUid/read", "PUT")
        executeVoid(req, callback)
    }

    fun deleteMessage(msgId: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/messages/$msgId", "DELETE")
        executeVoid(req, callback)
    }

    fun addReaction(msgId: String, emoji: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("emoji" to emoji)
        val req = buildRequest("/api/messages/$msgId/reaction", "POST", body)
        executeVoid(req, callback)
    }

    // ─── Groups ───────────────────────────────────────────────────────────────

    fun createGroup(name: String, photo: String, members: List<String>, callback: (Group?, String?) -> Unit) {
        val body = mapOf("name" to name, "photo" to photo, "members" to members.mapNotNull { it.toIntOrNull() })
        val req = buildRequest("/api/groups", "POST", body)
        execute(req, object : TypeToken<Group>() {}, callback)
    }

    fun getUserGroups(callback: (List<Group>?, String?) -> Unit) {
        val req = buildRequest("/api/groups")
        execute(req, object : TypeToken<List<Group>>() {}, callback)
    }

    fun getGroup(groupId: String, callback: (Group?, String?) -> Unit) {
        val req = buildRequest("/api/groups/$groupId")
        execute(req, object : TypeToken<Group>() {}, callback)
    }

    fun getGroupMessages(groupId: String, limit: Int = 200, offset: Int = 0, callback: (List<GroupMessage>?, String?) -> Unit) {
        val req = buildRequest("/api/groups/$groupId/messages?limit=$limit&offset=$offset")
        execute(req, object : TypeToken<List<GroupMessage>>() {}, callback)
    }

    fun sendGroupMessage(groupId: String, text: String, type: String = "text", url: String = "", viewOnce: Boolean = false, isCamera: Boolean = false, isGallery: Boolean = false, transcript: String = "", callback: (GroupMessage?, String?) -> Unit) {
        val body = mapOf("text" to text, "type" to type, "url" to url, "viewOnce" to viewOnce, "isCamera" to isCamera, "isGallery" to isGallery, "transcript" to transcript)
        val req = buildRequest("/api/groups/$groupId/messages", "POST", body)
        execute(req, object : TypeToken<GroupMessage>() {}, callback)
    }

    // ─── Public Groups ────────────────────────────────────────────────────────

    fun createPublicGroup(tag: String, name: String, photo: String, callback: (PublicGroup?, String?) -> Unit) {
        val body = mapOf("tag" to tag, "name" to name, "photo" to photo)
        val req = buildRequest("/api/public-groups", "POST", body)
        execute(req, object : TypeToken<PublicGroup>() {}, callback)
    }

    fun searchPublicGroups(query: String, callback: (List<PublicGroup>?, String?) -> Unit) {
        val req = buildRequest("/api/public-groups/search?q=${query.encodeUrl()}")
        execute(req, object : TypeToken<List<PublicGroup>>() {}, callback)
    }

    fun getUserPublicGroups(callback: (List<PublicGroup>?, String?) -> Unit) {
        val req = buildRequest("/api/public-groups")
        execute(req, object : TypeToken<List<PublicGroup>>() {}, callback)
    }

    fun joinPublicGroup(groupId: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/public-groups/$groupId/join", "POST")
        executeVoid(req, callback)
    }

    fun leavePublicGroup(groupId: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/public-groups/$groupId/leave", "POST")
        executeVoid(req, callback)
    }

    fun getPublicGroupMessages(groupId: String, limit: Int = 200, offset: Int = 0, callback: (List<GroupMessage>?, String?) -> Unit) {
        val req = buildRequest("/api/public-groups/$groupId/messages?limit=$limit&offset=$offset")
        execute(req, object : TypeToken<List<GroupMessage>>() {}, callback)
    }

    fun sendPublicGroupMessage(groupId: String, text: String, type: String = "text", url: String = "", viewOnce: Boolean = false, isCamera: Boolean = false, isGallery: Boolean = false, transcript: String = "", callback: (GroupMessage?, String?) -> Unit) {
        val body = mapOf("text" to text, "type" to type, "url" to url, "viewOnce" to viewOnce, "isCamera" to isCamera, "isGallery" to isGallery, "transcript" to transcript)
        val req = buildRequest("/api/public-groups/$groupId/messages", "POST", body)
        execute(req, object : TypeToken<GroupMessage>() {}, callback)
    }

    // ─── Stories ──────────────────────────────────────────────────────────────

    fun getStories(callback: (List<Story>?, String?) -> Unit) {
        val req = buildRequest("/api/stories")
        execute(req, object : TypeToken<List<Story>>() {}, callback)
    }

    fun createStory(type: String, text: String, imageUrl: String, callback: (Story?, String?) -> Unit) {
        val body = mapOf("type" to type, "text" to text, "imageUrl" to imageUrl)
        val req = buildRequest("/api/stories", "POST", body)
        execute(req, object : TypeToken<Story>() {}, callback)
    }

    fun markStorySeen(storyId: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/stories/$storyId/seen", "POST")
        executeVoid(req, callback)
    }

    // ─── Feed ─────────────────────────────────────────────────────────────────

    fun getFeedPosts(limit: Int = 20, offset: Int = 0, callback: (List<FeedPost>?, String?) -> Unit) {
        val req = buildRequest("/api/feed?limit=$limit&offset=$offset")
        execute(req, object : TypeToken<List<FeedPost>>() {}, callback)
    }

    fun createFeedPost(text: String, mediaUrl: String, mediaType: String, callback: (FeedPost?, String?) -> Unit) {
        val body = mapOf("text" to text, "mediaUrl" to mediaUrl, "mediaType" to mediaType)
        val req = buildRequest("/api/feed", "POST", body)
        execute(req, object : TypeToken<FeedPost>() {}, callback)
    }

    fun toggleFeedLike(postId: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/feed/$postId/like", "POST")
        executeVoid(req, callback)
    }

    fun getFeedComments(postId: String, callback: (List<FeedComment>?, String?) -> Unit) {
        val req = buildRequest("/api/feed/$postId/comments")
        execute(req, object : TypeToken<List<FeedComment>>() {}, callback)
    }

    fun addFeedComment(postId: String, text: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("text" to text)
        val req = buildRequest("/api/feed/$postId/comments", "POST", body)
        executeVoid(req, callback)
    }

    // ─── Friends ──────────────────────────────────────────────────────────────

    fun sendFriendRequest(toId: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("toId" to (toId.toIntOrNull() ?: 0))
        val req = buildRequest("/api/friends/request", "POST", body)
        executeVoid(req, callback)
    }

    fun respondFriendRequest(requestId: String, accept: Boolean, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("accept" to accept)
        val req = buildRequest("/api/friends/request/$requestId", "PUT", body)
        executeVoid(req, callback)
    }

    fun getFriendRequests(callback: (List<FriendRequest>?, String?) -> Unit) {
        val req = buildRequest("/api/friends/requests")
        execute(req, object : TypeToken<List<FriendRequest>>() {}, callback)
    }

    fun getFriends(callback: (List<User>?, String?) -> Unit) {
        val req = buildRequest("/api/friends")
        execute(req, object : TypeToken<List<User>>() {}, callback)
    }

    // ─── ConzAI ───────────────────────────────────────────────────────────────

    fun getConzAIHistory(callback: (List<ConzAIMessage>?, String?) -> Unit) {
        val req = buildRequest("/api/conzai")
        execute(req, object : TypeToken<List<ConzAIMessage>>() {}, callback)
    }

    fun sendConzAIMessage(content: String, callback: (ConzAIMessage?, String?) -> Unit) {
        val body = mapOf("content" to content)
        val req = buildRequest("/api/conzai", "POST", body)
        execute(req, object : TypeToken<ConzAIMessage>() {}, callback)
    }

    // ─── Suggestions ──────────────────────────────────────────────────────────

    fun createSuggestion(text: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("text" to text)
        val req = buildRequest("/api/suggestions", "POST", body)
        executeVoid(req, callback)
    }

    // ─── Calls / Agora ─────────────────────────────────────────────────────────

    /**
     * Build a request to a specific port on the same server (e.g. sidecar on 8081).
     */
    fun buildPublicRequest(path: String, method: String = "GET", body: Any? = null, port: Int = 8080): Request {
        val base = getBaseUrl().replace(Regex(":\\d+$"), "")
        val url = "$base:$port$path"
        val token = getToken()
        val builder = Request.Builder().url(url)
        if (token != null) builder.addHeader("Authorization", "Bearer $token")
        return when (method.uppercase()) {
            "POST" -> {
                val json = if (body != null) gson.toJson(body) else "{}"
                builder.post(json.toRequestBody(JSON_MEDIA_TYPE)).build()
            }
            else -> builder.get().build()
        }
    }

    /**
     * Execute a request and return the raw response body string.
     */
    fun executeRaw(request: Request, callback: (String?, String?) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e.message ?: "Network error")
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    callback(null, "Error ${response.code}")
                } else {
                    callback(body, null)
                }
            }
        })
    }

    fun getAgoraToken(channelName: String, uid: Int = 0, callback: (String?, String?) -> Unit) {
        val body = mapOf("channel" to channelName, "uid" to uid, "token" to (getToken() ?: ""))
        val req = buildPublicRequest("/api/calls/token", "POST", body, port = 8081)
        executeRaw(req) { responseBody, err ->
            if (err != null || responseBody == null) {
                callback(null, err)
                return@executeRaw
            }
            try {
                val json = gson.fromJson(responseBody, Map::class.java)
                callback(json["token"] as? String, null)
            } catch (e: Exception) {
                callback(null, "Parse error")
            }
        }
    }

    // ─── App Config ───────────────────────────────────────────────────────────

    fun getAppConfig(callback: (AppConfig?, String?) -> Unit) {
        val req = buildRequest("/api/config")
        execute(req, object : TypeToken<AppConfig>() {}, callback)
    }

    // ─── File Upload ──────────────────────────────────────────────────────────

    fun uploadFile(file: File, mimeType: String, callback: (String?, String?) -> Unit) {
        val token = getToken() ?: run { callback(null, "Not authenticated"); return }
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create(mimeType.toMediaType(), file))
            .build()
        val request = Request.Builder()
            .url("${getBaseUrl()}/api/upload")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    callback(null, "Upload failed: ${response.code}")
                    return
                }
                try {
                    val result = gson.fromJson(body, Map::class.java)
                    callback(result["url"] as? String, null)
                } catch (e: Exception) {
                    callback(null, "Parse error")
                }
            }
        })
    }

    // ─── WebSocket ────────────────────────────────────────────────────────────

    fun connectWebSocket() {
        val token = getToken() ?: return
        val wsUrl = getBaseUrl()
            .replace("http://", "ws://")
            .replace("https://", "wss://") + "/ws?token=$token"
        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                wsReconnectAttempts = 0
                Log.d("ApiManager", "[WS] Connected")
                wsListeners.forEach { it.onConnected() }
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val msg = gson.fromJson(text, WSMessage::class.java)
                    // Track incoming DM senders as conversation partners
                    if (msg.type == "new_message") {
                        val payload = msg.payload
                        if (payload is Map<*, *>) {
                            // Gson deserialises JSON numbers as Double, so convert to int string
                            val rawFrom = payload["from_id"] ?: payload["from"]
                            val fromId = when (rawFrom) {
                                is Double -> rawFrom.toLong().toString()
                                is Long -> rawFrom.toString()
                                is Int -> rawFrom.toString()
                                is String -> rawFrom
                                else -> rawFrom?.toString()
                            }
                            if (!fromId.isNullOrEmpty() && fromId != currentUserId) {
                                addConversationPartner(fromId)
                            }
                        }
                    }
                    wsListeners.forEach { it.onMessage(msg) }
                } catch (e: Exception) {
                    Log.e("ApiManager", "[WS] Parse error: ${e.message}")
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ApiManager", "[WS] Error: ${t.message}")
                wsListeners.forEach { it.onDisconnected() }
                scheduleReconnect()
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ApiManager", "[WS] Closed: $reason")
                wsListeners.forEach { it.onDisconnected() }
            }
        })
    }

    fun disconnectWebSocket() {
        webSocket?.close(1000, "Logout")
        webSocket = null
    }

    fun sendWsMessage(type: String, payload: Any) {
        val msg = mapOf("type" to type, "payload" to payload)
        webSocket?.send(gson.toJson(msg))
    }

    fun sendTyping(toUid: String, typing: Boolean) {
        sendWsMessage("typing", mapOf("to" to toUid.toIntOrNull(), "typing" to typing))
    }

    fun sendGroupTyping(groupId: String, typing: Boolean) {
        sendWsMessage("typing", mapOf("groupId" to groupId.toIntOrNull(), "typing" to typing))
    }

    fun addWsListener(listener: WebSocketListener2) {
        if (!wsListeners.contains(listener)) wsListeners.add(listener)
    }

    fun removeWsListener(listener: WebSocketListener2) {
        wsListeners.remove(listener)
    }

    private fun scheduleReconnect() {
        if (wsReconnectAttempts >= 5) return
        wsReconnectAttempts++
        val delay = (wsReconnectAttempts * 3000).toLong()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isLoggedIn()) connectWebSocket()
        }, delay)
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun String.encodeUrl(): String = java.net.URLEncoder.encode(this, "UTF-8")

    // ─── Response Models ──────────────────────────────────────────────────────

    data class AuthResponse(val token: String, val user: User)
    data class WSMessage(val type: String, val payload: Any?)

    interface WebSocketListener2 {
        fun onConnected() {}
        fun onDisconnected() {}
        fun onMessage(msg: WSMessage)
    }

    // ─── Stories (additional) ─────────────────────────────────────────────────
    fun getUserStory(uid: String, callback: (Story?, String?) -> Unit) {
        val req = buildRequest("/api/stories/$uid")
        execute(req, object : TypeToken<Story>() {}, callback)
    }
    fun viewStory(storyId: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/stories/$storyId/view", "POST")
        executeVoid(req, callback)
    }
    fun postStory(imageUrl: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("type" to "image", "imageUrl" to imageUrl, "text" to "")
        val req = buildRequest("/api/stories", "POST", body)
        executeVoid(req, callback)
    }
    // ─── Feed (additional) ────────────────────────────────────────────────────
    fun createFeedPost(text: String, imageUrl: String?, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("text" to text, "mediaUrl" to (imageUrl ?: ""), "mediaType" to if (imageUrl != null) "image" else "")
        val req = buildRequest("/api/feed", "POST", body)
        executeVoid(req, callback)
    }
    fun addComment(postId: String, text: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("text" to text)
        val req = buildRequest("/api/feed/$postId/comments", "POST", body)
        executeVoid(req, callback)
    }
    fun getComments(postId: String, callback: (List<Comment>?, String?) -> Unit) {
        val req = buildRequest("/api/feed/$postId/comments")
        execute(req, object : TypeToken<List<Comment>>() {}, callback)
    }
    // ─── Groups (additional) ──────────────────────────────────────────────────
    fun leaveGroup(groupId: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/groups/$groupId/leave", "POST")
        executeVoid(req, callback)
    }
    fun getPublicGroup(groupId: String, callback: (PublicGroup?, String?) -> Unit) {
        // Use admin sidecar so we get the admins list (Go backend doesn't return it)
        val base = getBaseUrl().replace(Regex(":\\d+$"), "")
        val token = getToken() ?: ""
        val encodedToken = java.net.URLEncoder.encode(token, "UTF-8")
        val url = "$base/admin-api/admin/group-info/$groupId?token=$encodedToken"
        val req = Request.Builder()
            .url(url)
            .addHeader("X-Admin-Token", "ConzAdminDash2025!xK9mP")
            .get()
            .build()
        execute(req, object : TypeToken<PublicGroup>() {}, callback)
    }
    // ─── Public Group Management ──────────────────────────────────────────────
    fun updatePublicGroup(groupId: String, name: String, photo: String, description: String? = null, callback: (Boolean, String?) -> Unit) {
        val body = mutableMapOf<String, Any>("name" to name, "photo" to photo)
        if (description != null) body["description"] = description
        val req = buildRequest("/api/public-groups/$groupId", "PUT", body)
        executeVoid(req, callback)
    }

    fun updatePublicGroupSettings(groupId: String, settings: Map<String, Any>, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("settings" to settings)
        val req = buildRequest("/api/public-groups/$groupId/settings", "PUT", body)
        executeVoid(req, callback)
    }

    /**
     * Build a request to the admin-api sidecar.
     * Nginx proxies /admin-api/ -> port 8082 on the server.
     * We strip the :8080 port from the base URL to hit port 80 (nginx).
     */
    private fun buildAdminSidecarRequest(endpoint: String, body: Any): Request {
        val base = getBaseUrl().replace(Regex(":\\d+$"), "") // http://167.99.85.232
        val url = "$base/admin-api/admin/$endpoint"
        val json = gson.toJson(body)
        return Request.Builder()
            .url(url)
            .addHeader("X-Admin-Token", "ConzAdminDash2025!xK9mP")
            .post(json.toRequestBody(JSON_MEDIA_TYPE))
            .build()
    }

    fun promotePublicGroupAdmin(groupId: String, userId: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("token" to (getToken() ?: ""), "groupId" to groupId, "targetUid" to userId, "role" to "admin")
        val req = buildAdminSidecarRequest("group-promote", body)
        executeVoid(req, callback)
    }

    fun demotePublicGroupAdmin(groupId: String, userId: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("token" to (getToken() ?: ""), "groupId" to groupId, "targetUid" to userId, "role" to "member")
        val req = buildAdminSidecarRequest("group-promote", body)
        executeVoid(req, callback)
    }

    fun removePublicGroupMember(groupId: String, userId: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("token" to (getToken() ?: ""), "groupId" to groupId, "targetUid" to userId)
        val req = buildAdminSidecarRequest("group-remove", body)
        executeVoid(req, callback)
    }

    fun banPublicGroupMember(groupId: String, userId: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("token" to (getToken() ?: ""), "groupId" to groupId, "targetUid" to userId)
        val req = buildAdminSidecarRequest("group-ban", body)
        executeVoid(req, callback)
    }

    fun sendGroupSystemMessage(groupId: String, text: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("token" to (getToken() ?: ""), "groupId" to groupId, "text" to text)
        val req = buildAdminSidecarRequest("group-system-msg", body)
        executeVoid(req, callback)
    }

    fun addPublicGroupMember(groupId: String, userId: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("userId" to userId)
        val req = buildRequest("/api/public-groups/$groupId/add", "POST", body)
        executeVoid(req, callback)
    }

    fun setDmAllowed(allowed: Boolean, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("allowDms" to allowed)
        val req = buildRequest("/api/me/dm-settings", "PUT", body)
        executeVoid(req, callback)
    }

    fun getDmAllowed(userId: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/users/$userId/dm-settings")
        execute(req, object : TypeToken<Map<String, Any>>() {}) { map, _ ->
            val allowed = map?.get("allowDms") as? Boolean ?: true
            callback(allowed, null)
        }
    }

    // ─── Profile (additional) ─────────────────────────────────────────────────
    fun updateVibe(vibe: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("status" to vibe)
        val req = buildRequest("/api/me/profile", "PUT", body)
        executeVoid(req, callback)
    }
    fun deleteAccount(callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/me", "DELETE")
        executeVoid(req, callback)
    }
    fun changePassword(currentPassword: String, newPassword: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("currentPassword" to currentPassword, "newPassword" to newPassword)
        val req = buildRequest("/api/me/change-password", "POST", body)
        executeVoid(req, callback)
    }
    fun updateDisplayName(displayName: String, callback: (Boolean, String?) -> Unit) {
        val user = currentUser
        val photo = user?.photo ?: ""
        val cover = user?.coverPhoto ?: ""
        val status = user?.status ?: ""
        updateProfile(displayName, photo, cover, status, callback)
    }


    // ─── Block / Report ────────────────────────────────────────────────────────
    fun blockUser(uid: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/users/$uid/block", "POST")
        executeVoid(req, callback)
    }

    fun unblockUser(uid: String, callback: (Boolean, String?) -> Unit) {
        val req = buildRequest("/api/users/$uid/block", "DELETE")
        executeVoid(req, callback)
    }

    fun reportUser(uid: String, reason: String, callback: (Boolean, String?) -> Unit) {
        val body = mapOf("reason" to reason)
        val req = buildRequest("/api/users/$uid/report", "POST", body)
        executeVoid(req, callback)
    }

    data class UpdateInfo(val versionCode: Int = 0, val versionName: String = "")

    fun checkForUpdate(callback: (UpdateInfo?, String?) -> Unit) {
        getAppConfig { config, err ->
            if (config != null) {
                // Parse version string e.g. "4.1.0" into an integer code
                val parts = config.version.split(".")
                val code = try {
                    (parts.getOrNull(0)?.toInt() ?: 0) * 10000 +
                    (parts.getOrNull(1)?.toInt() ?: 0) * 100 +
                    (parts.getOrNull(2)?.toInt() ?: 0)
                } catch (e: Exception) { 0 }
                callback(UpdateInfo(versionCode = code, versionName = config.version), null)
            } else {
                callback(null, err)
            }
        }
    }
}
