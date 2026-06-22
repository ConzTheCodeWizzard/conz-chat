package com.conzchat.app.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.conzchat.app.BuildConfig
import com.conzchat.app.ConzChatApp
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentChatBinding
import com.conzchat.app.model.Message
import com.conzchat.app.model.ReplyTo
import com.conzchat.app.ui.call.CallFragment
import com.conzchat.app.ui.profile.ProfileFragment
import com.conzchat.app.util.ConzMods
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.ImageUtils
import com.conzchat.app.util.OneSignalNotifier
import com.conzchat.app.util.toast
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.io.File
import com.conzchat.app.util.HarleyThemeHelper

class ChatFragment : Fragment() {

    companion object {
        fun newInstance(uid: String, name: String, photo: String): ChatFragment {
            return ChatFragment().apply {
                arguments = Bundle().apply {
                    putString("uid", uid)
                    putString("name", name)
                    putString("photo", photo)
                }
            }
        }
    }

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    private var messagesListener: ListenerRegistration? = null
    private var typingListener: ListenerRegistration? = null

    private var otherUid = ""
    private var otherName = ""
    private var otherPhoto = ""
    private var replyTo: ReplyTo? = null
    private var viewOnceMode = false
    private var mediaBarOpen = false
    private var cameraUri: Uri? = null
    private var typingHandler = Handler(Looper.getMainLooper())
    private var typingRunnable: Runnable? = null
    private val uid get() = FirebaseManager.currentUid

    // Activity result launchers
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { sendImageFromUri(it) }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { sendImageFromUri(it, isCamera = true) }
    }
    private val videoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { sendVideoFromUri(it) }
    }
    private val audioLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { sendVoiceFromUri(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)

        otherUid = arguments?.getString("uid") ?: ""
        otherName = arguments?.getString("name") ?: ""
        otherPhoto = arguments?.getString("photo") ?: ""

        setupUI()
        loadMessages()
        listenTyping()
    }

    private fun setupUI() {
        // Top bar
        binding.tvChatName.text = otherName
        binding.tvChatName.setOnClickListener {
            openFragment(ProfileFragment.newInstance(otherUid))
        }
        if (otherPhoto.isNotEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(otherPhoto)
                .apply(com.bumptech.glide.request.RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_default_avatar)
                .into(binding.ivChatAvatar)
        }
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Call buttons
        binding.ivVoiceCall.setOnClickListener { startCall("voice") }
        binding.ivVideoCall.setOnClickListener { startCall("video") }

        // Chat search
        binding.ivChatSearch.setOnClickListener { toggleChatSearch() }

        // Load topbar badge (premium/dev) and status
        loadTopbarInfo()

        // Message list
        messageAdapter = MessageAdapter(
            messages = messages,
            myUid = uid,
            onReaction = { msgId, emoji -> addReaction(msgId, emoji) },
            onReply = { msg -> startReply(msg) },
            onDelete = { msgId -> deleteMessage(msgId) },
            onImageClick = { url -> openFullImage(url) },
            onViewOnce = { msgId, type, url -> openViewOnce(msgId, type, url) },
            onProfileClick = { uid -> openFragment(ProfileFragment.newInstance(uid)) }
        )
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = messageAdapter
        }

        // Swipe-to-reply
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(rv: androidx.recyclerview.widget.RecyclerView, vh: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.adapterPosition
                if (pos >= 0 && pos < messages.size) {
                    startReply(messages[pos])
                }
                messageAdapter.notifyItemChanged(pos)
            }
            override fun getSwipeThreshold(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) = 0.3f
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvMessages)

        // Input
        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasText = s?.isNotEmpty() == true
                binding.btnSend.visibility = if (hasText) View.VISIBLE else View.GONE
                binding.btnMedia.visibility = if (hasText) View.GONE else View.VISIBLE
                if (hasText) setTyping()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnSend.setOnClickListener { sendTextMessage() }
        binding.btnMedia.setOnClickListener { toggleMediaBar() }

        // Media bar
        binding.btnCamera.setOnClickListener { openCamera() }
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnVideo.setOnClickListener { videoLauncher.launch("video/*") }
        binding.btnVoice.setOnClickListener { openVoiceRecorder() }
        binding.btnGif.setOnClickListener { openGifPicker() }
        binding.btnViewOnce.setOnClickListener {
            viewOnceMode = !viewOnceMode
            binding.btnViewOnce.alpha = if (viewOnceMode) 1.0f else 0.5f
            context?.toast(if (viewOnceMode) "View-once ON" else "View-once OFF")
        }

        // Reply bar close
        binding.btnCancelReply.setOnClickListener { cancelReply() }

        // Conz super menu (type "conz" to open)
        setupConzMenu()
    }

    private fun setupConzMenu() {
        binding.btnConzMenuClose.setOnClickListener {
            binding.conzMenu.visibility = View.GONE
        }
        binding.btnPremiumMenu.setOnClickListener {
            binding.conzMenu.visibility = View.GONE
            binding.premiumMenu.visibility = View.VISIBLE
        }
        binding.btnClosePremiumMenu.setOnClickListener {
            binding.premiumMenu.visibility = View.GONE
            binding.conzMenu.visibility = View.VISIBLE
        }
        binding.btnGivePremium.setOnClickListener { givePremium() }
        binding.btnStartSpam.setOnClickListener { startAnimatedMessage() }
        binding.btnStopSpam.setOnClickListener { stopAnimatedMessage() }

        // Dev-only: boot user
        if (uid == ConzChatApp.DEV_UID) {
            binding.btnBootUser.visibility = View.VISIBLE
            binding.btnBanUser.visibility = View.VISIBLE
        }
        binding.btnBootUser.setOnClickListener { bootUser() }
        binding.btnBanUser.setOnClickListener { banUser() }
    }

    private fun loadMessages() {
        messagesListener = FirebaseManager.messagesRef
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                messages.clear()
                snap.documents.forEach { doc ->
                    val m = doc.data ?: return@forEach
                    val from = m["from"] as? String ?: return@forEach
                    val to = m["to"] as? String ?: return@forEach
                    if (from != uid && to != uid) return@forEach
                    val other = if (from == uid) to else from
                    if (other != otherUid) return@forEach

                    val isMine = from == uid
                    val msgId = doc.id

                    // Self-destruct: delete expired messages
                    val selfDestruct = m["selfDestruct"] as? Boolean ?: false
                    val selfDestructAt = m["selfDestructAt"] as? Long ?: 0L
                    if (selfDestruct && selfDestructAt > 0 && System.currentTimeMillis() > selfDestructAt) {
                        FirebaseManager.messagesRef.document(msgId).delete()
                        return@forEach
                    }

                    // Update receipts
                    if (!ConzMods.isDisableReceipts(requireContext())) {
                        if (!isMine && m["receipt"] != "R") {
                            FirebaseManager.messagesRef.document(msgId).update("receipt", "R")
                        }
                    }

                    val replyToData = m["replyTo"] as? Map<*, *>
                    val replyTo = if (replyToData != null) ReplyTo(
                        id = replyToData["id"] as? String ?: "",
                        text = replyToData["text"] as? String ?: "",
                        sender = replyToData["sender"] as? String ?: ""
                    ) else null

                    val reactionsRaw = m["reactions"] as? Map<*, *>
                    val reactions = reactionsRaw?.entries?.associate {
                        (it.key as? String ?: "") to (it.value as? String ?: "")
                    } ?: emptyMap()

                    messages.add(Message(
                        id = msgId,
                        from = from,
                        to = to,
                        time = m["time"] as? Long ?: 0L,
                        text = m["text"] as? String ?: "",
                        type = m["type"] as? String ?: "text",
                        url = m["url"] as? String ?: "",
                        receipt = m["receipt"] as? String ?: "S",
                        deleted = m["deleted"] as? Boolean ?: false,
                        viewOnce = m["viewOnce"] as? Boolean ?: false,
                        viewed = m["viewed"] as? Boolean ?: false,
                        isCamera = m["isCamera"] as? Boolean ?: false,
                        transcript = m["transcript"] as? String ?: "",
                        replyTo = replyTo,
                        reactions = reactions
                    ))
                }
                messageAdapter.notifyDataSetChanged()
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
    }

    private fun listenTyping() {
        typingListener = FirebaseManager.dmTypingRef
            .whereEqualTo("to", uid)
            .whereEqualTo("from", otherUid)
            .addSnapshotListener { snap, _ ->
                var isTyping = false
                snap?.documents?.forEach { doc ->
                    val d = doc.data ?: return@forEach
                    val typing = d["typing"] as? Boolean ?: false
                    val ts = d["ts"] as? Long ?: 0L
                    if (typing && (System.currentTimeMillis() - ts) < 5000) isTyping = true
                }
                // Show typing indicator in the topbar subtitle (Kik-style)
                if (isTyping) {
                    binding.tvChatStatus.visibility = View.VISIBLE
                    binding.tvChatStatus.text = "$otherName is typing..."
                    binding.tvChatStatus.setTextColor(0xFFCC0022.toInt())
                } else {
                    // Restore status or hide
                    val savedStatus = binding.tvChatStatus.tag as? String
                    if (!savedStatus.isNullOrEmpty()) {
                        binding.tvChatStatus.visibility = View.VISIBLE
                        binding.tvChatStatus.text = savedStatus
                        binding.tvChatStatus.setTextColor(0xFF888888.toInt())
                    } else {
                        binding.tvChatStatus.visibility = View.GONE
                    }
                }
            }
    }

    private fun setTyping() {
        if (ConzMods.isDisableTyping(requireContext())) return
        FirebaseManager.dmTypingRef.document("${uid}_${otherUid}").set(
            mapOf("from" to uid, "to" to otherUid, "typing" to true, "ts" to System.currentTimeMillis())
        )
        typingRunnable?.let { typingHandler.removeCallbacks(it) }
        typingRunnable = Runnable { clearTyping() }
        typingHandler.postDelayed(typingRunnable!!, 3000)
    }

    private fun clearTyping() {
        FirebaseManager.dmTypingRef.document("${uid}_${otherUid}").set(
            mapOf("from" to uid, "to" to otherUid, "typing" to false, "ts" to System.currentTimeMillis())
        )
    }

    private fun sendTextMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        // Check for Conz super menu trigger
        if (text.lowercase() == "conz") {
            binding.etMessage.setText("")
            binding.conzMenu.visibility = if (binding.conzMenu.isVisible()) View.GONE else View.VISIBLE
            return
        }

        clearTyping()

        val msgData = hashMapOf<String, Any>(
            "from" to uid,
            "to" to otherUid,
            "time" to System.currentTimeMillis(),
            "text" to text,
            "type" to "text",
            "receipt" to "S"
        )
        replyTo?.let { msgData["replyTo"] = mapOf("id" to it.id, "text" to it.text, "sender" to it.sender) }
        // Self-Destruct: add timer if enabled (30 seconds)
        if (ConzMods.isSelfDestruct(requireContext())) {
            msgData["selfDestruct"] = true
            msgData["selfDestructAt"] = System.currentTimeMillis() + 30000L
        }
        FirebaseManager.messagesRef.add(msgData)
        // Send push notification via OneSignal
        sendDmPushNotification(text)
        binding.etMessage.setText("")
        cancelReply()
        activity?.window?.decorView?.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun sendDmPushNotification(messageText: String) {
        val myUid = uid
        if (myUid.isEmpty() || otherUid.isEmpty()) return
        // Get sender's display name from Firestore then send notification via FCM v1 API
        FirebaseManager.usersRef.document(myUid).get().addOnSuccessListener { snap ->
            val senderName = snap.getString("displayName") ?: snap.getString("username") ?: "Someone"
            com.conzchat.app.util.FcmNotifier.sendDmNotification(
                toUid = otherUid,
                senderName = senderName,
                messageText = messageText,
                senderUid = myUid
            )
        }
    }

    private fun sendImageFromUri(uri: Uri, isCamera: Boolean = false) {
        val base64 = ImageUtils.compressImageToBase64ForChat(requireContext(), uri) ?: return
        val msgData = hashMapOf<String, Any>(
            "from" to uid, "to" to otherUid,
            "time" to System.currentTimeMillis(),
            "type" to "image", "url" to base64,
            "text" to "", "receipt" to "S",
            "isCamera" to (isCamera || ConzMods.isFakeCamera(requireContext())),
            "viewOnce" to viewOnceMode, "viewed" to false
        )
        replyTo?.let { msgData["replyTo"] = mapOf("id" to it.id, "text" to it.text, "sender" to it.sender) }
        FirebaseManager.messagesRef.add(msgData)
        sendDmPushNotification("📷 Photo")
        cancelReply()
        closeMediaBar()
    }

    private fun sendVideoFromUri(uri: Uri) {
        context?.toast("Uploading video...")
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()
        val base64 = "data:video/mp4;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        val msgData = hashMapOf<String, Any>(
            "from" to uid, "to" to otherUid,
            "time" to System.currentTimeMillis(),
            "type" to "video", "url" to base64,
            "text" to "", "receipt" to "S",
            "viewOnce" to viewOnceMode, "viewed" to false
        )
        FirebaseManager.messagesRef.add(msgData)
        sendDmPushNotification("🎥 Video")
        closeMediaBar()
    }

    private fun sendVoiceFromUri(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()
        val base64 = "data:audio/mpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        val msgData = hashMapOf<String, Any>(
            "from" to uid, "to" to otherUid,
            "time" to System.currentTimeMillis(),
            "type" to "voice", "url" to base64,
            "text" to "", "receipt" to "S"
        )
        FirebaseManager.messagesRef.add(msgData)
        sendDmPushNotification("🎤 Voice message")
        closeMediaBar()
    }

    fun sendGif(url: String) {
        val msgData = hashMapOf<String, Any>(
            "from" to uid, "to" to otherUid,
            "time" to System.currentTimeMillis(),
            "type" to "gif", "url" to url,
            "text" to "", "receipt" to "S"
        )
        FirebaseManager.messagesRef.add(msgData)
        sendDmPushNotification("GIF")
    }

    private fun addReaction(msgId: String, emoji: String) {
        FirebaseManager.messagesRef.document(msgId)
            .update("reactions.$uid", emoji)
    }

    private fun startReply(msg: Message) {
        replyTo = ReplyTo(id = msg.id, text = msg.text.ifEmpty { "📎 Media" }, sender = if (msg.from == uid) "You" else otherName)
        binding.replyBar.visibility = View.VISIBLE
        binding.tvReplyText.text = replyTo!!.text.take(60)
        binding.tvReplySender.text = replyTo!!.sender
        binding.etMessage.requestFocus()
    }

    private fun cancelReply() {
        replyTo = null
        binding.replyBar.visibility = View.GONE
    }

    private fun deleteMessage(msgId: String) {
        FirebaseManager.messagesRef.document(msgId).update(
            mapOf("deleted" to true, "text" to "This message was deleted", "type" to "text")
        )
    }

    private fun openFullImage(url: String) {
        val fragment = FullImageFragment.newInstance(url)
        openFragment(fragment)
    }

    private fun openViewOnce(msgId: String, type: String, url: String) {
        FirebaseManager.messagesRef.document(msgId).update("viewed", true)
        val fragment = FullImageFragment.newInstance(url)
        openFragment(fragment)
    }

    private fun toggleMediaBar() {
        mediaBarOpen = !mediaBarOpen
        binding.mediaBar.visibility = if (mediaBarOpen) View.VISIBLE else View.GONE
    }

    private fun closeMediaBar() {
        mediaBarOpen = false
        binding.mediaBar.visibility = View.GONE
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
            return
        }
        val photoFile = File.createTempFile("photo_", ".jpg", requireContext().cacheDir)
        cameraUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
        cameraLauncher.launch(cameraUri!!)
    }

    private fun openVoiceRecorder() {
        val fragment = VoiceRecorderBottomSheet.newInstance(otherUid)
        fragment.show(childFragmentManager, "voice_recorder")
    }

    private fun openGifPicker() {
        val fragment = GifPickerBottomSheet.newInstance()
        fragment.setOnGifSelected { url -> sendGif(url) }
        fragment.show(childFragmentManager, "gif_picker")
    }

    private fun startCall(type: String) {
        val callId = FirebaseManager.db.collection("calls").document().id
        val callData = hashMapOf(
            "from" to uid, "to" to otherUid,
            "type" to type, "status" to "ringing",
            "time" to System.currentTimeMillis()
        )
        FirebaseManager.callsRef.document(callId).set(callData)
        val fragment = CallFragment.newInstance(
            toUid = otherUid, toName = otherName, toPhoto = otherPhoto,
            callType = type, isIncoming = false, callId = callId
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun givePremium() {
        if (uid != ConzChatApp.DEV_UID) { context?.toast("YOU AINT A DEV!"); return }
        FirebaseManager.usersRef.document(otherUid).update(
            mapOf(
                "premium" to true,
                "premiumPopup" to "Premium has been successfully added to your account, ENJOY! Please refresh the app to activate premium features."
            )
        )
        context?.toast("Premium granted!")
        binding.premiumMenu.visibility = View.GONE
    }

    private var spamRunnable: Runnable? = null
    private val spamHandler = Handler(Looper.getMainLooper())

    private fun startAnimatedMessage() {
        val text = binding.etAnimatedMsg.text.toString().trim()
        if (text.isEmpty()) return
        spamRunnable = object : Runnable {
            override fun run() {
                val msgData = hashMapOf<String, Any>(
                    "from" to uid, "to" to otherUid,
                    "time" to System.currentTimeMillis(),
                    "text" to "🎭 $text 🎭", "type" to "text", "receipt" to "S"
                )
                FirebaseManager.messagesRef.add(msgData)
                spamHandler.postDelayed(this, 150)
            }
        }
        spamHandler.post(spamRunnable!!)
        binding.tvPremiumConsole.text = "Spam started"
    }

    private fun stopAnimatedMessage() {
        spamRunnable?.let { spamHandler.removeCallbacks(it) }
        binding.tvPremiumConsole.text = "Spam stopped"
    }

    private fun bootUser() {
        if (uid != ConzChatApp.DEV_UID) return
        FirebaseManager.usersRef.document(otherUid).update(
            mapOf("forceLogout" to true, "logoutMessage" to "You have been booted by the developer.")
        )
        context?.toast("User booted")
        binding.conzMenu.visibility = View.GONE
    }

    private fun banUser() {
        if (uid != ConzChatApp.DEV_UID) return
        FirebaseManager.usersRef.document(otherUid).update("banned", true)
        context?.toast("User banned")
        binding.conzMenu.visibility = View.GONE
    }

    private fun loadTopbarInfo() {
        FirebaseManager.usersRef.document(otherUid).addSnapshotListener { snap, _ ->
            if (_binding == null || snap == null) return@addSnapshotListener
            val isPremium = snap.getBoolean("premium") ?: false
            val isDev = otherUid == ConzChatApp.DEV_UID
            val status = snap.getString("status") ?: ""
            val isOnline = snap.getBoolean("online") ?: false
            val lastSeen = snap.getLong("lastSeen") ?: 0L
            // Badge
            when {
                isDev -> {
                    binding.tvTopbarBadge.text = "🔧 DEV"
                    binding.tvTopbarBadge.visibility = View.VISIBLE
                }
                isPremium -> {
                    binding.tvTopbarBadge.text = "👑 PREMIUM"
                    binding.tvTopbarBadge.visibility = View.VISIBLE
                }
                else -> binding.tvTopbarBadge.visibility = View.GONE
            }
            // Online/offline status in topbar subtitle
            val statusText = when {
                isOnline -> "Online"
                lastSeen > 0L -> "Last seen " + com.conzchat.app.util.TimeUtils.formatKikTime(lastSeen)
                status.isNotEmpty() -> status
                else -> ""
            }
            if (statusText.isNotEmpty()) {
                binding.tvChatStatus.text = statusText
                binding.tvChatStatus.tag = statusText
                binding.tvChatStatus.visibility = View.VISIBLE
                binding.tvChatStatus.setTextColor(
                    if (isOnline) 0xFF00CC66.toInt() else 0xFF888888.toInt()
                )
            } else {
                binding.tvChatStatus.visibility = View.GONE
            }
            // Populate avatar cache for the other user
            val photoUrl = snap.getString("photo") ?: ""
            if (photoUrl.isNotEmpty()) {
                messageAdapter.avatarCache[otherUid] = photoUrl
                messageAdapter.notifyDataSetChanged()
            }
        }
    }

    private var chatSearchOpen = false

    private fun toggleChatSearch() {
        chatSearchOpen = !chatSearchOpen
        if (chatSearchOpen) {
            // Show a search dialog
            val input = android.widget.EditText(requireContext()).apply {
                hint = "Search messages..."
                setTextColor(0xFFFFFFFF.toInt())
                setHintTextColor(0xFF888888.toInt())
            }
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Search in chat")
                .setView(input)
                .setPositiveButton("Search") { _, _ ->
                    val query = input.text.toString().trim().lowercase()
                    if (query.isEmpty()) return@setPositiveButton
                    val idx = messages.indexOfLast { it.text.lowercase().contains(query) }
                    if (idx >= 0) {
                        binding.rvMessages.scrollToPosition(idx)
                        context?.toast("Found: \"${messages[idx].text.take(40)}\"")
                    } else {
                        context?.toast("No messages found for \"$query\"")
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> chatSearchOpen = false }
                .show()
        } else {
            chatSearchOpen = false
        }
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun View.isVisible() = visibility == View.VISIBLE

    override fun onDestroyView() {
        super.onDestroyView()
        messagesListener?.remove()
        typingListener?.remove()
        clearTyping()
        typingRunnable?.let { typingHandler.removeCallbacks(it) }
        spamRunnable?.let { spamHandler.removeCallbacks(it) }
        _binding = null
    }
}
