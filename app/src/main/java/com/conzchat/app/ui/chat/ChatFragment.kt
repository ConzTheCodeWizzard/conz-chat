package com.conzchat.app.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentChatBinding
import com.conzchat.app.model.Message
import com.conzchat.app.model.ReplyTo
import com.conzchat.app.util.ApiManager
import com.conzchat.app.util.HarleyThemeHelper
import com.conzchat.app.util.toast
import com.bumptech.glide.Glide
import java.io.File

class ChatFragment : Fragment(), ApiManager.WebSocketListener2 {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var otherUid = ""
    private var otherName = ""
    private var otherPhoto = ""
    private var replyTo: ReplyTo? = null
    private val uid get() = ApiManager.currentUserId
    private var isTyping = false
    private var typingHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val stopTypingRunnable = Runnable {
        isTyping = false
        ApiManager.sendTyping(otherUid, false)
    }

    // ── Receipt polling ───────────────────────────────────────────────────────
    // Since the backend does not push receipt_update WebSocket events, we poll
    // the message list after sending to pick up S→D→R transitions.
    private val receiptPollHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var receiptPollCount = 0
    private val maxReceiptPolls = 12  // poll up to 12 times (60 seconds total)
    private val receiptPollRunnable = object : Runnable {
        override fun run() {
            if (_binding == null || receiptPollCount >= maxReceiptPolls) return
            receiptPollCount++
            refreshReceiptsOnly()
            // Poll every 5 seconds
            receiptPollHandler.postDelayed(this, 5_000)
        }
    }

    // ── Voice recording (inline mic button) ─────────────────────────────────
    private var voiceRecorder: MediaRecorder? = null
    private var voiceFile: File? = null
    private var isVoiceRecording = false

    // Pending gallery media (shown in preview bar before send)
    private var pendingMediaUri: Uri? = null
    private var pendingMediaIsVideo = false

    // Camera result receiver (called by CameraFragment via setFragmentResult)
    private val cameraResultKey = "camera_result"

    companion object {
        fun newInstance(uid: String, name: String, photo: String) = ChatFragment().apply {
            arguments = Bundle().apply {
                putString("uid", uid); putString("name", name); putString("photo", photo)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)
        otherUid = arguments?.getString("uid") ?: ""
        otherName = arguments?.getString("name") ?: ""
        otherPhoto = arguments?.getString("photo") ?: ""

        binding.tvChatName.text = otherName
        Glide.with(this).load(otherPhoto).circleCrop().placeholder(R.drawable.ic_default_avatar).into(binding.ivChatAvatar)
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // ── Call buttons ──────────────────────────────────────────────────────
        binding.ivVoiceCall?.setOnClickListener { launchCall("voice") }
        binding.ivVideoCall?.setOnClickListener { launchCall("video") }

        binding.ivChatAvatar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, com.conzchat.app.ui.profile.ProfileFragment.newInstance(otherUid))
                .addToBackStack(null).commit()
        }

        adapter = MessageAdapter(
            messages = messages,
            myUid = uid,
            onReaction = { msgId, emoji -> ApiManager.addReaction(msgId, emoji) { _, _ -> } },
            onReply = { msg -> setReply(msg) },
            onDelete = { msgId -> ApiManager.deleteMessage(msgId) { ok, _ ->
                if (ok) {
                    activity?.runOnUiThread {
                        val idx = messages.indexOfFirst { it.id == msgId }
                        if (idx >= 0) { messages[idx] = messages[idx].copy(deleted = true, text = ""); adapter.notifyItemChanged(idx) }
                    }
                }
            }},
            onImageClick = { url -> openFullImage(url) },
            onViewOnce = { _, _, _ -> },
            onProfileClick = { uid ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, com.conzchat.app.ui.profile.ProfileFragment.newInstance(uid))
                    .addToBackStack(null).commit()
            }
        )
        // Populate avatar cache so incoming messages show the other person's photo
        if (otherPhoto.isNotEmpty()) {
            adapter.avatarCache[otherUid] = otherPhoto
        }
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = this@ChatFragment.adapter
        }

        binding.btnSend.setOnClickListener { sendMessageOrMedia() }
        // + button opens the media drawer (Camera / Gallery / GIF)
        binding.btnMedia.setOnClickListener {
            MediaDrawerBottomSheet(
                onCamera = { openCameraFragment() },
                onGallery = { openGallerySheet() },
                onGif = {
                    GifPickerBottomSheet.newInstance(otherUid, isGroup = false, isPublicGroup = false) { gifUrl ->
                        sendMediaMessage(gifUrl, "gif", isCamera = false, isGallery = false)
                    }.show(childFragmentManager, "gif")
                }
            ).show(childFragmentManager, "mediaDrawer")
        }
        binding.btnCancelMediaPreview.setOnClickListener { clearMediaPreview() }
        binding.btnCancelReply.setOnClickListener { clearReply() }

        // Mic button: hold to record, drag left to discard, release to send
        var micDownX = 0f
        binding.btnMic.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { micDownX = event.rawX; startVoiceRecording(); true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - micDownX
                    val threshold = 80 * resources.displayMetrics.density
                    binding.tvSlideToCancel.setTextColor(
                        if (dx < -threshold) 0xFFFF0033.toInt() else 0xFF888888.toInt())
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX - micDownX
                    val threshold = 80 * resources.displayMetrics.density
                    if (dx < -threshold) discardVoiceRecording() else stopAndSendVoice()
                    true
                }
                MotionEvent.ACTION_CANCEL -> { discardVoiceRecording(); true }
                else -> false
            }
        }

        // Listen for camera results (photo or video from CameraFragment)
        parentFragmentManager.setFragmentResultListener(cameraResultKey, viewLifecycleOwner) { _, bundle ->
            val url = bundle.getString("url") ?: return@setFragmentResultListener
            val type = bundle.getString("type") ?: "image"
            sendMediaMessage(url, type, isCamera = true, isGallery = false)
        }

        binding.etMessage.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isTyping) { isTyping = true; ApiManager.sendTyping(otherUid, true) }
                typingHandler.removeCallbacks(stopTypingRunnable)
                typingHandler.postDelayed(stopTypingRunnable, 3000)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        loadMessages()
        ApiManager.addWsListener(this)
        ApiManager.markMessagesRead(otherUid) { _, _ -> }
    }

    private fun loadMessages() {
        ApiManager.getMessages(otherUid) { msgs, _ ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                messages.clear()
                if (msgs != null) messages.addAll(msgs.sortedBy { it.time })
                adapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) binding.rvMessages.scrollToPosition(messages.size - 1)
                // Start receipt polling whenever we load messages so receipts
                // update even if we didn't send anything in this session.
                val hasPendingReceipts = messages.any { it.from == uid && it.receipt != "R" }
                if (hasPendingReceipts) startReceiptPolling(resetCount = true)
            }
        }
    }

    /**
     * Refresh only the receipt fields of existing messages without scrolling or
     * clearing the list. Called by the receipt polling mechanism.
     */
    private fun refreshReceiptsOnly() {
        ApiManager.getMessages(otherUid) { msgs, _ ->
            activity?.runOnUiThread {
                if (_binding == null || msgs == null) return@runOnUiThread
                var changed = false
                val sorted = msgs.sortedBy { it.time }
                for (fresh in sorted) {
                    val idx = messages.indexOfFirst { it.id == fresh.id }
                    if (idx >= 0 && messages[idx].receipt != fresh.receipt) {
                        messages[idx] = messages[idx].copy(receipt = fresh.receipt)
                        adapter.notifyItemChanged(idx)
                        changed = true
                    }
                }
                // If all my sent messages are "R", stop polling early
                val allRead = messages.filter { it.from == uid }.all { it.receipt == "R" }
                if (allRead) {
                    receiptPollHandler.removeCallbacks(receiptPollRunnable)
                }
            }
        }
    }

    /** Start or extend receipt polling. Safe to call multiple times — only resets
     *  the counter when called from loadMessages (fresh open), not on every send/receive. */
    private fun startReceiptPolling(resetCount: Boolean = false) {
        receiptPollHandler.removeCallbacks(receiptPollRunnable)
        if (resetCount) receiptPollCount = 0
        receiptPollHandler.postDelayed(receiptPollRunnable, 3_000)
    }

    private fun sendMessageOrMedia() {
        // If there's a pending gallery media, send it (with optional caption text)
        val pendingUri = pendingMediaUri
        if (pendingUri != null) {
            val caption = binding.etMessage.text.toString().trim()
            binding.etMessage.text.clear()
            clearMediaPreview()
            uploadAndSendFromGallery(pendingUri, caption)
            return
        }
        sendMessage()
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return
        binding.etMessage.text.clear()
        ApiManager.sendMessage(otherUid, text, replyTo = replyTo) { msg, err ->
            activity?.runOnUiThread {
                if (err != null) context?.toast("Failed to send")
                else if (msg != null) {
                    messages.add(msg)
                    adapter.notifyItemInserted(messages.size - 1)
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                    startReceiptPolling(resetCount = true)
                }
            }
        }
        clearReply()
    }



    private fun reactMessage(msg: Message, emoji: String) {
        ApiManager.addReaction(msg.id, emoji) { _, _ -> }
    }

    /** Open the gallery bottom-sheet picker */
    private fun openGallerySheet() {
        GalleryPickerBottomSheet.newInstance { uri, isVideo ->
            pendingMediaUri = uri
            pendingMediaIsVideo = isVideo
            showMediaPreview(uri, isVideo)
        }.show(childFragmentManager, "galleryPicker")
    }

    private fun showMediaPreview(uri: Uri, isVideo: Boolean) {
        binding.mediaPreviewBar.visibility = View.VISIBLE
        Glide.with(this).load(uri).centerCrop().into(binding.ivMediaPreview)
        binding.ivMediaPreviewVideoIcon.visibility = if (isVideo) View.VISIBLE else View.GONE
        binding.tvMediaPreviewLabel.text = if (isVideo) "Video ready · tap send" else "Photo ready · tap send"
    }

    private fun clearMediaPreview() {
        pendingMediaUri = null
        pendingMediaIsVideo = false
        binding.mediaPreviewBar.visibility = View.GONE
    }

    private fun uploadAndSendFromGallery(uri: Uri, caption: String = "") {
        val ctx = context ?: return
        val file = uriToFile(ctx, uri) ?: return
        val mimeType = ctx.contentResolver.getType(uri) ?: "application/octet-stream"
        val type = when {
            mimeType.startsWith("image/") -> "image"
            mimeType.startsWith("video/") -> "video"
            else -> "file"
        }
        ApiManager.uploadFile(file, mimeType) { url, err ->
            activity?.runOnUiThread {
                if (url != null) {
                    ApiManager.sendMessage(otherUid, caption, type = type, url = url, isGallery = true) { msg, _ ->
                        activity?.runOnUiThread {
                            if (msg != null) {
                                messages.add(msg)
                                adapter.notifyItemInserted(messages.size - 1)
                                binding.rvMessages.scrollToPosition(messages.size - 1)
                                startReceiptPolling(resetCount = true)
                            }
                        }
                    }
                } else {
                    context?.toast("Upload failed: $err")
                }
            }
        }
    }

    /** Send an already-uploaded URL as a message (used by camera result and GIF picker) */
    private fun sendMediaMessage(url: String, type: String, isCamera: Boolean, isGallery: Boolean) {
        ApiManager.sendMessage(otherUid, "", type = type, url = url,
            isCamera = isCamera, isGallery = isGallery) { msg, _ ->
            activity?.runOnUiThread {
                if (msg != null) {
                    messages.add(msg)
                    adapter.notifyItemInserted(messages.size - 1)
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                    startReceiptPolling(resetCount = true)
                }
            }
        }
    }

    /** Open the in-app camera fragment (tap=photo, hold=video) */
    private fun openCameraFragment() {
        val cameraFrag = CameraFragment.newInstance(cameraResultKey)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, cameraFrag)
            .addToBackStack(null)
            .commit()
    }

    // ── Inline voice recording ────────────────────────────────────────────────
    private val recordTimerHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var recordStartMs = 0L
    private val MAX_RECORD_MS = 120_000L // 2 minutes
    private val recordTimerRunnable = object : Runnable {
        override fun run() {
            if (!isVoiceRecording || _binding == null) return
            val elapsed = System.currentTimeMillis() - recordStartMs
            val secs = (elapsed / 1000).toInt()
            binding.tvRecordTimer.text = "%d:%02d".format(secs / 60, secs % 60)
            if (elapsed >= MAX_RECORD_MS) { stopAndSendVoice(); return }
            recordTimerHandler.postDelayed(this, 500)
        }
    }
    private fun startVoiceRecording() {
        val ctx = context ?: return
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ctx.toast("Microphone permission required")
            return
        }
        try {
            val file = File(ctx.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            voiceFile = file
            voiceRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                MediaRecorder(ctx) else @Suppress("DEPRECATION") MediaRecorder()
            voiceRecorder!!.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
                        isVoiceRecording = true
            recordStartMs = System.currentTimeMillis()
            binding.voiceRecordingOverlay.visibility = View.VISIBLE
            binding.tvRecordTimer.text = "0:00"
            recordTimerHandler.post(recordTimerRunnable)
        } catch (e: Exception) {
            ctx.toast("Could not start recording: ${e.message}")
            cleanupVoiceRecorder()
        }
    }
    private fun discardVoiceRecording() {
        recordTimerHandler.removeCallbacks(recordTimerRunnable)
        binding.voiceRecordingOverlay.visibility = View.GONE
        binding.tvSlideToCancel.setTextColor(0xFF888888.toInt())
        cleanupVoiceRecorder()
        context?.toast("Recording discarded")
    }
    private fun stopAndSendVoice() {
        if (!isVoiceRecording) return
        isVoiceRecording = false
        recordTimerHandler.removeCallbacks(recordTimerRunnable)
        binding.voiceRecordingOverlay.visibility = View.GONE
        binding.tvSlideToCancel.setTextColor(0xFF888888.toInt())
        try { voiceRecorder?.stop() } catch (e: Exception) {
            cleanupVoiceRecorder(); context?.toast("Recording too short"); return
        } finally { voiceRecorder?.release(); voiceRecorder = null }
        val file = voiceFile ?: return
        if (!file.exists() || file.length() < 1000) { context?.toast("Recording too short"); return }
        ApiManager.uploadFile(file, "audio/mpeg") { url, err ->
            activity?.runOnUiThread {
                if (url != null) {
                    ApiManager.sendMessage(otherUid, "", "voice", url) { msg, _ ->
                        activity?.runOnUiThread {
                            if (msg != null) {
                                messages.add(msg)
                                adapter.notifyItemInserted(messages.size - 1)
                                binding.rvMessages.scrollToPosition(messages.size - 1)
                                startReceiptPolling(resetCount = true)
                            }
                        }
                    }
                } else { context?.toast("Failed to upload voice: $err") }
            }
        }
    }

    private fun cleanupVoiceRecorder() {
        isVoiceRecording = false
        recordTimerHandler.removeCallbacks(recordTimerRunnable)
        if (_binding != null) binding.voiceRecordingOverlay.visibility = View.GONE
        try { voiceRecorder?.stop() } catch (_: Exception) {}
        try { voiceRecorder?.release() } catch (_: Exception) {}
        voiceRecorder = null
        voiceFile?.delete(); voiceFile = null
    }

    private fun setReply(msg: Message) {
        replyTo = ReplyTo(id = msg.id, text = msg.text, sender = msg.from)
        binding.tvReplyText.text = msg.text
        binding.replyBar.visibility = View.VISIBLE
    }

    private fun clearReply() {
        replyTo = null
        binding.replyBar.visibility = View.GONE
    }

    private fun openFullImage(url: String) {
        val fragment = com.conzchat.app.ui.chat.FullImageFragment.newInstance(url)
        parentFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, fragment).addToBackStack(null).commit()
    }

    override fun onMessage(msg: ApiManager.WSMessage) {
        activity?.runOnUiThread {
            when (msg.type) {
                "new_message" -> {
                    val payload = ApiManager.gson.fromJson(ApiManager.gson.toJson(msg.payload), Message::class.java)
                    if ((payload.from == otherUid && payload.to == uid) || (payload.from == uid && payload.to == otherUid)) {
                        if (messages.none { it.id == payload.id }) {
                            messages.add(payload)
                            adapter.notifyItemInserted(messages.size - 1)
                            binding.rvMessages.scrollToPosition(messages.size - 1)
                            if (payload.from == otherUid) {
                                ApiManager.markMessagesRead(otherUid) { _, _ -> }
                                // Refresh receipts so sender sees "R" quickly
                                startReceiptPolling(resetCount = false)
                            }
                        }
                    }
                }
                "typing" -> {
                    val p = msg.payload as? Map<*, *>
                    val fromId = p?.get("from")?.toString() ?: ""
                    val typing = p?.get("typing") as? Boolean ?: false
                    if (fromId == otherUid) {
                        // tvTyping not in layout - typing indicator removed
                    }
                }
                "message_deleted" -> {
                    val p = msg.payload as? Map<*, *>
                    val msgId = p?.get("id")?.toString() ?: ""
                    val idx = messages.indexOfFirst { it.id == msgId }
                    if (idx >= 0) {
                        messages[idx] = messages[idx].copy(deleted = true, text = "")
                        adapter.notifyItemChanged(idx)
                    }
                }
                "reaction" -> {
                    val p = msg.payload as? Map<*, *>
                    val msgId = p?.get("messageId")?.toString() ?: ""
                    val emoji = p?.get("emoji")?.toString() ?: ""
                    val reactorId = p?.get("userId")?.toString() ?: ""
                    val idx = messages.indexOfFirst { it.id == msgId }
                    if (idx >= 0) {
                        val updated = messages[idx].reactions.toMutableMap()
                        updated[reactorId] = emoji
                        messages[idx] = messages[idx].copy(reactions = updated)
                        adapter.notifyItemChanged(idx)
                    }
                }
            }
        }
    }

    // ── Call launch ──────────────────────────────────────────────────────────────────
    private fun launchCall(type: String) {
        if (otherUid.isEmpty()) return
        // Generate a unique channel name for this call session
        val myUid = ApiManager.currentUserId
        val sorted = listOf(myUid, otherUid).sorted()
        val channelName = "dm_${sorted[0]}_${sorted[1]}_${System.currentTimeMillis() / 1000}"
        val callId = "call_${System.currentTimeMillis()}"

        // Fetch Agora token then launch CallFragment
        ApiManager.getAgoraToken(channelName) { token, err ->
            activity?.runOnUiThread {
                if (err != null || token == null) {
                    context?.toast("Could not start call — check connection")
                    return@runOnUiThread
                }
                val callFrag = com.conzchat.app.ui.call.CallFragment.newInstance(
                    toUid = otherUid,
                    toName = otherName,
                    toPhoto = otherPhoto,
                    callType = type,
                    isIncoming = false,
                    callId = callId,
                    channelName = channelName,
                    agoraToken = token
                )
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, callFrag)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ApiManager.removeWsListener(this)
        typingHandler.removeCallbacks(stopTypingRunnable)
        receiptPollHandler.removeCallbacks(receiptPollRunnable)
        cleanupVoiceRecorder()
        _binding = null
    }
}

private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val ext = context.contentResolver.getType(uri)?.substringAfterLast("/") ?: "tmp"
        val file = File.createTempFile("upload_", ".$ext", context.cacheDir)
        file.outputStream().use { inputStream.copyTo(it) }
        file
    } catch (e: Exception) { null }
}
