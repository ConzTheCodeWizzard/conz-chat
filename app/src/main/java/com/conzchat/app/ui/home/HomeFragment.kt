package com.conzchat.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.conzchat.app.ConzChatApp
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentHomeBinding
import com.conzchat.app.model.ChatListItem
import com.conzchat.app.model.Story
import com.conzchat.app.ui.chat.ChatFragment
import com.conzchat.app.ui.chat.ConzAIChatFragment
import com.conzchat.app.ui.groups.GroupChatFragment
import com.conzchat.app.ui.groups.PublicGroupChatFragment
import com.conzchat.app.ui.groups.CreateGroupFragment
import com.conzchat.app.ui.groups.PublicGroupsFragment
import com.conzchat.app.ui.profile.ProfileFragment
import com.conzchat.app.ui.stories.StoriesFragment
import com.conzchat.app.ui.settings.SettingsFragment
import com.conzchat.app.ui.home.SuggestionsFragment
import com.conzchat.app.ui.dev.DevMenuFragment
import com.conzchat.app.ui.feed.FeedFragment
import com.conzchat.app.util.AppPreferences
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.TimeUtils
import com.conzchat.app.util.toast
import com.conzchat.app.ui.call.CallFragment
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var storyRailAdapter: StoryRailAdapter

    private val chatItems = mutableListOf<ChatListItem>()
    private val storyItems = mutableListOf<Story>()

    private var messagesListener: ListenerRegistration? = null
    private var groupsListener: ListenerRegistration? = null
    private var publicGroupsListener: ListenerRegistration? = null
    private var storiesListener: ListenerRegistration? = null
    private var userDataListener: ListenerRegistration? = null
    private var friendRequestListener: ListenerRegistration? = null
    private var appConfigListener: ListenerRegistration? = null
    private var incomingCallListener: ListenerRegistration? = null
    private var handledCallIds = mutableSetOf<String>()

    private var fabOpen = false
    private var currentUserData: Map<String, Any> = emptyMap()
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupClickListeners()
        loadUserData()
        loadChats()
        loadGroups()
        loadPublicGroups()
        loadStories()
        listenFriendRequests()
        listenAppConfig()
        listenIncomingCalls()
    }

    private fun setupRecyclerViews() {
        // Story rail
        storyRailAdapter = StoryRailAdapter(storyItems,
            onAddStory = { openAddStory() },
            onStoryClick = { story -> openStoryViewer(story) }
        )
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = storyRailAdapter
        }

        // Chat list
        chatListAdapter = ChatListAdapter(chatItems) { item ->
            when (item.type) {
                "conzAI" -> openConzAI()
                "group" -> openGroup(item)
                "publicGroup" -> openPublicGroup(item)
                else -> openChat(item)
            }
        }
        binding.rvChats.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }
    }

    private fun setupClickListeners() {
        // Profile avatar
        binding.ivMyAvatar.setOnClickListener {
            openProfile(uid)
        }

        // Search icon → Friend Requests
        binding.ivSearch.setOnClickListener {
            openFragment(com.conzchat.app.ui.friends.FriendRequestsFragment.newInstance())
        }

        // ConzFeed icon
        binding.ivConzFeed.setOnClickListener {
            openFragment(FeedFragment.newInstance())
        }

        // FAB
        binding.fabMain.setOnClickListener {
            toggleFab()
        }

        // FAB menu items
        binding.fabNewDm.setOnClickListener {
            closeFab()
            openUserSearch()
        }
        binding.fabNewGroup.setOnClickListener {
            closeFab()
            openFragment(CreateGroupFragment())
        }
        binding.fabPublicGroups.setOnClickListener {
            closeFab()
            openFragment(PublicGroupsFragment())
        }
        // Dev Inbox — only wired for DEV_UID, visibility set in loadUserData
        binding.fabDevInbox.setOnClickListener {
            closeFab()
            openFragment(DevMenuFragment())
        }

        // Dismiss FAB on background tap
        binding.fabOverlay.setOnClickListener {
            closeFab()
        }
    }

    private fun loadUserData() {
        userDataListener = FirebaseManager.usersRef.document(uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) return@addSnapshotListener
                currentUserData = snap.data ?: emptyMap()

                // Update avatar
                val photo = currentUserData["photo"] as? String ?: ""
                binding.ivMyAvatar.loadAvatar(photo)

                // Show Dev Inbox button only for DEV_UID
                val isDev = uid == ConzChatApp.DEV_UID
                binding.fabDevInbox.visibility = if (isDev) View.VISIBLE else View.GONE

                // Check for premium popup
                val premiumPopup = currentUserData["premiumPopup"] as? String ?: ""
                if (premiumPopup.isNotEmpty()) {
                    showDialog("Premium", premiumPopup)
                    FirebaseManager.usersRef.document(uid).update("premiumPopup", "")
                }

                // Check banned
                val banned = currentUserData["banned"] as? Boolean ?: false
                if (banned) {
                    showDialog("Banned", "This account is permanently banned from ConzChat.")
                    FirebaseManager.auth.signOut()
                    return@addSnapshotListener
                }

                // Check force logout
                val forceLogout = currentUserData["forceLogout"] as? Boolean ?: false
                if (forceLogout) {
                    val msg = currentUserData["logoutMessage"] as? String ?: "Logged out"
                    FirebaseManager.usersRef.document(uid).update(
                        mapOf("forceLogout" to false, "logoutMessage" to "")
                    )
                    showDialog("Notice", msg)
                    FirebaseManager.auth.signOut()
                }
            }
    }

    private fun loadChats() {
        messagesListener = FirebaseManager.messagesRef
            .orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val convMap = mutableMapOf<String, ChatListItem>()

                val unreadMap = mutableMapOf<String, Int>()

                snap.documents.forEach { doc ->
                    val m = doc.data ?: return@forEach
                    val from = m["from"] as? String ?: return@forEach
                    val to = m["to"] as? String ?: return@forEach
                    if (from != uid && to != uid) return@forEach

                    val other = if (from == uid) to else from
                    val time = m["time"] as? Long ?: 0L
                    val text = m["text"] as? String ?: ""
                    val type = m["type"] as? String ?: "text"
                    val deleted = m["deleted"] as? Boolean ?: false
                    val receipt = m["receipt"] as? String ?: "S"

                    // Count unread: messages FROM other TO me that are not yet read
                    if (from == other && to == uid && receipt != "R") {
                        unreadMap[other] = (unreadMap[other] ?: 0) + 1
                    }

                    if (!convMap.containsKey(other)) {
                        val lastMsg = when {
                            deleted -> "This message was deleted"
                            type == "image" -> "📷 Photo"
                            type == "video" -> "🎥 Video"
                            type == "voice" -> "🎤 Voice note"
                            type == "gif" -> "🎦 GIF"
                            else -> text
                        }
                        convMap[other] = ChatListItem(
                            uid = other,
                            name = other,
                            photo = "",
                            lastMessage = lastMsg,
                            lastTime = time,
                            type = "dm"
                        )
                    }
                }

                // Resolve user names/photos with real unread counts
                resolveAndUpdateChatList(convMap, unreadMap)
            }
    }

    private fun resolveAndUpdateChatList(convMap: Map<String, ChatListItem>, unreadMap: Map<String, Int> = emptyMap()) {
        if (convMap.isEmpty()) {
            updateChatListUI(emptyList())
            return
        }

        val resolved = mutableListOf<ChatListItem>()
        var count = 0
        convMap.forEach { (otherUid, item) ->
            FirebaseManager.usersRef.document(otherUid).get()
                .addOnSuccessListener { doc ->
                    val u = doc.data
                    val name = u?.get("displayName") as? String
                        ?: u?.get("username") as? String ?: otherUid
                    val photo = u?.get("photo") as? String ?: ""
                    val unread = unreadMap[otherUid] ?: 0
                    resolved.add(item.copy(name = name, photo = photo, unreadCount = unread))
                    count++
                    if (count == convMap.size) {
                        val sorted = resolved.sortedByDescending { it.lastTime }
                        updateChatListUI(sorted)
                    }
                }
                .addOnFailureListener {
                    count++
                    resolved.add(item)
                    if (count == convMap.size) {
                        val sorted = resolved.sortedByDescending { it.lastTime }
                        updateChatListUI(sorted)
                    }
                }
        }
    }

    private fun loadGroups() {
        groupsListener = FirebaseManager.groupsRef
            .whereArrayContains("members", uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val groupItems = snap.documents.mapNotNull { doc ->
                    val d = doc.data ?: return@mapNotNull null
                    ChatListItem(
                        uid = doc.id,
                        name = d["name"] as? String ?: "Group",
                        photo = d["photo"] as? String ?: "",
                        lastMessage = d["lastMessage"] as? String ?: "",
                        lastTime = d["lastTime"] as? Long ?: 0L,
                        type = "group",
                        groupId = doc.id
                    )
                }
                mergeGroupsIntoChatList(groupItems, "group")
            }
    }

    private fun loadPublicGroups() {
        publicGroupsListener = FirebaseManager.publicGroupsRef
            .whereArrayContains("members", uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val pgItems = snap.documents.mapNotNull { doc ->
                    val d = doc.data ?: return@mapNotNull null
                    ChatListItem(
                        uid = doc.id,
                        name = "#${d["tag"] as? String ?: d["name"] as? String ?: "group"}",
                        photo = d["photo"] as? String ?: "",
                        lastMessage = d["lastMessage"] as? String ?: "",
                        lastTime = d["lastTime"] as? Long ?: 0L,
                        type = "publicGroup",
                        groupId = doc.id,
                        publicGroupTag = d["tag"] as? String ?: ""
                    )
                }
                mergeGroupsIntoChatList(pgItems, "publicGroup")
            }
    }

    private fun mergeGroupsIntoChatList(newItems: List<ChatListItem>, type: String) {
        chatItems.removeAll { it.type == type }
        chatItems.addAll(newItems)
        chatItems.sortByDescending { it.lastTime }
        // Add Conz AI at top
        ensureConzAIRow()
        chatListAdapter.notifyDataSetChanged()
    }

    private fun ensureConzAIRow() {
        if (chatItems.none { it.type == "conzAI" }) {
            chatItems.add(0, ChatListItem(
                uid = "conz_ai_bot",
                name = "Conz AI",
                photo = "",
                lastMessage = "Chat with the AI assistant",
                lastTime = Long.MAX_VALUE,
                type = "conzAI"
            ))
        }
    }

    private fun updateChatListUI(dmItems: List<ChatListItem>) {
        chatItems.removeAll { it.type == "dm" }
        chatItems.addAll(dmItems)
        chatItems.sortByDescending { it.lastTime }
        ensureConzAIRow()
        chatListAdapter.notifyDataSetChanged()
    }

    private fun loadStories() {
        val now = System.currentTimeMillis()
        storiesListener = FirebaseManager.storiesRef
            .whereGreaterThan("expires", now)
            .orderBy("expires")
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                storyItems.clear()
                snap.documents.forEach { doc ->
                    val d = doc.data ?: return@forEach
                    storyItems.add(Story(
                        id = doc.id,
                        uid = d["uid"] as? String ?: "",
                        name = d["name"] as? String ?: "",
                        photo = d["photo"] as? String ?: "",
                        type = d["type"] as? String ?: "text",
                        text = d["text"] as? String ?: "",
                        imageUrl = d["imageUrl"] as? String ?: "",
                        time = d["time"] as? Long ?: 0L,
                        expires = d["expires"] as? Long ?: 0L,
                        seenBy = (d["seenBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    ))
                }
                storyRailAdapter.notifyDataSetChanged()
            }
    }

    private fun listenFriendRequests() {
        friendRequestListener = FirebaseManager.friendRequestsRef
            .whereEqualTo("to", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snap, _ ->
                val count = snap?.size() ?: 0
                binding.tvFriendBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
                binding.tvFriendBadge.text = count.toString()
            }
    }

    private var announcementListener: ListenerRegistration? = null

    private fun listenAppConfig() {
        appConfigListener = FirebaseManager.appConfigRef.document("version")
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) return@addSnapshotListener
                val version = snap.getString("version") ?: return@addSnapshotListener
                val lastSeen = AppPreferences.getLastVersionSeen(requireContext())
                if (version.isNotEmpty() && version != lastSeen) {
                    val title = snap.getString("updateTitle") ?: "Update Available"
                    val msg = snap.getString("updateMessage") ?: "A new version of ConzChat is available!"
                    AppPreferences.setLastVersionSeen(requireContext(), version)
                    showDialog(title, msg)
                }
            }

        // Listen for dev announcements and show a dismissible banner
        announcementListener = FirebaseManager.appConfigRef.document("announcement")
            .addSnapshotListener { snap, _ ->
                if (_binding == null || snap == null || !snap.exists()) return@addSnapshotListener
                val text = snap.getString("text") ?: return@addSnapshotListener
                if (text.isEmpty()) {
                    binding.tvAnnouncement.visibility = View.GONE
                    return@addSnapshotListener
                }
                val lastShown = AppPreferences.getLastSeen(requireContext(), "announcement", "")
                val announcementTime = snap.getLong("time") ?: 0L
                if (announcementTime.toString() != lastShown) {
                    binding.tvAnnouncement.text = "📢 $text"
                    binding.tvAnnouncement.visibility = View.VISIBLE
                    binding.tvAnnouncement.setOnClickListener {
                        AppPreferences.setLastSeen(requireContext(), "announcement", announcementTime.toString())
                        binding.tvAnnouncement.visibility = View.GONE
                    }
                }
            }
    }

    private fun toggleFab() {
        fabOpen = !fabOpen
        binding.fabMenu.visibility = if (fabOpen) View.VISIBLE else View.GONE
        binding.fabOverlay.visibility = if (fabOpen) View.VISIBLE else View.GONE
        binding.fabMain.rotation = if (fabOpen) 45f else 0f
    }

    private fun closeFab() {
        fabOpen = false
        binding.fabMenu.visibility = View.GONE
        binding.fabOverlay.visibility = View.GONE
        binding.fabMain.rotation = 0f
    }

    private fun openChat(item: ChatListItem) {
        val fragment = ChatFragment.newInstance(item.uid, item.name, item.photo)
        openFragment(fragment)
    }

    private fun openConzAI() {
        openFragment(ConzAIChatFragment())
    }

    private fun openGroup(item: ChatListItem) {
        val fragment = GroupChatFragment.newInstance(item.groupId, item.name, item.photo)
        openFragment(fragment)
    }

    private fun openPublicGroup(item: ChatListItem) {
        val fragment = PublicGroupChatFragment.newInstance(item.groupId, item.name, item.photo, item.publicGroupTag)
        openFragment(fragment)
    }

    private fun openProfile(uid: String) {
        val fragment = ProfileFragment.newInstance(uid)
        openFragment(fragment)
    }

    private fun openUserSearch() {
        openFragment(UserSearchFragment())
    }

    private fun openAddStory() {
        openFragment(StoriesFragment.newInstance(null))
    }

    private fun openStoryViewer(story: Story) {
        openFragment(StoriesFragment.newInstance(story.uid))
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showDialog(title: String, message: String) {
        if (!isAdded) return
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun listenIncomingCalls() {
        incomingCallListener = FirebaseManager.callsRef
            .whereEqualTo("to", uid)
            .whereEqualTo("status", "ringing")
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                snap.documents.forEach { doc ->
                    val callId = doc.id
                    if (callId in handledCallIds) return@forEach
                    handledCallIds.add(callId)
                    val d = doc.data ?: return@forEach
                    val fromUid = d["from"] as? String ?: return@forEach
                    val callType = d["type"] as? String ?: "voice"
                    // Fetch caller info then show incoming call screen
                    FirebaseManager.usersRef.document(fromUid).get().addOnSuccessListener { userSnap ->
                        if (_binding == null) return@addOnSuccessListener
                        val callerName = userSnap.getString("displayName") ?: userSnap.getString("username") ?: "Unknown"
                        val callerPhoto = userSnap.getString("photo") ?: ""
                        val fragment = CallFragment.newInstance(
                            toUid = fromUid, toName = callerName, toPhoto = callerPhoto,
                            callType = callType, isIncoming = true, callId = callId
                        )
                        openFragment(fragment)
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagesListener?.remove()
        groupsListener?.remove()
        publicGroupsListener?.remove()
        storiesListener?.remove()
        userDataListener?.remove()
        friendRequestListener?.remove()
        appConfigListener?.remove()
        announcementListener?.remove()
        incomingCallListener?.remove()
        _binding = null
    }
}

// Extension function for ImageView in HomeFragment
fun android.widget.ImageView.loadAvatar(url: String?) {
    if (url.isNullOrEmpty()) {
        setImageResource(com.conzchat.app.R.drawable.ic_default_avatar)
    } else {
        com.bumptech.glide.Glide.with(this)
            .load(url)
            .apply(com.bumptech.glide.request.RequestOptions.circleCropTransform())
            .placeholder(com.conzchat.app.R.drawable.ic_default_avatar)
            .into(this)
    }
}
