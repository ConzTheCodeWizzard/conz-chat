package com.conzchat.app.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.conzchat.app.R
import com.conzchat.app.model.Message
import com.conzchat.app.util.TimeUtils
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(
    private val messages: List<Message>,
    private val myUid: String,
    private val onReaction: (String, String) -> Unit,
    private val onReply: (Message) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onImageClick: (String) -> Unit,
    private val onViewOnce: (String, String, String) -> Unit,
    private val onProfileClick: (String) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    /** Populated by ChatFragment after fetching the other user's profile */
    val avatarCache = mutableMapOf<String, String>()

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llRow: LinearLayout = view.findViewById(R.id.llRow)
        val ivAvatar: CircleImageView = view.findViewById(R.id.ivAvatar)
        val spacerStart: View = view.findViewById(R.id.spacerStart)
        val spacerEnd: View = view.findViewById(R.id.spacerEnd)
        val llBubble: LinearLayout = view.findViewById(R.id.llBubble)
        val tvReplyPreview: TextView = view.findViewById(R.id.tvReplyPreview)
        val tvText: TextView = view.findViewById(R.id.tvText)
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val videoView: VideoView = view.findViewById(R.id.videoView)
        val llVoice: LinearLayout = view.findViewById(R.id.llVoice)
        val tvVoiceLabel: TextView = view.findViewById(R.id.tvVoiceLabel)
        val tvTranscript: TextView = view.findViewById(R.id.tvTranscript)
        val tvViewOnce: TextView = view.findViewById(R.id.tvViewOnce)
        val tvMeta: TextView = view.findViewById(R.id.tvMeta)
        val tvReceipt: TextView = view.findViewById(R.id.tvReceipt)
        val llReactions: LinearLayout = view.findViewById(R.id.llReactions)
        val tvCameraLabel: TextView = view.findViewById(R.id.tvCameraLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messages[position]
        val isMine = msg.from == myUid

        // ── Alignment ─────────────────────────────────────────────────────
        // "me"   → spacerStart visible (pushes bubble RIGHT), avatar gone, receipt visible
        // "them" → spacerEnd visible (pushes bubble LEFT), avatar visible, receipt gone
        if (isMine) {
            holder.spacerStart.visibility = View.VISIBLE
            holder.spacerEnd.visibility = View.GONE
            holder.ivAvatar.visibility = View.GONE
            // Bubble background: red
            holder.llBubble.setBackgroundResource(R.drawable.bg_bubble_me)
            // Timestamp aligned right
            holder.tvMeta.layoutParams = (holder.tvMeta.layoutParams as LinearLayout.LayoutParams).also {
                it.gravity = android.view.Gravity.END
                it.marginStart = 0
                it.marginEnd = 8
            }
        } else {
            holder.spacerStart.visibility = View.GONE
            holder.spacerEnd.visibility = View.VISIBLE
            // Avatar
            holder.ivAvatar.visibility = View.VISIBLE
            holder.ivAvatar.setOnClickListener { onProfileClick(msg.from) }
            val photoUrl = avatarCache[msg.from]
            if (!photoUrl.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(holder.ivAvatar)
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }
            // Bubble background: dark
            holder.llBubble.setBackgroundResource(R.drawable.bg_bubble_them)
            // Timestamp aligned left
            holder.tvMeta.layoutParams = (holder.tvMeta.layoutParams as LinearLayout.LayoutParams).also {
                it.gravity = android.view.Gravity.START
                it.marginStart = 40
                it.marginEnd = 0
            }
        }

        // ── Reset content views ───────────────────────────────────────────
        holder.tvText.visibility = View.GONE
        holder.ivImage.visibility = View.GONE
        holder.videoView.visibility = View.GONE
        holder.llVoice.visibility = View.GONE
        holder.tvViewOnce.visibility = View.GONE
        holder.tvCameraLabel.visibility = View.GONE
        holder.tvReplyPreview.visibility = View.GONE

        // ── Reply preview ─────────────────────────────────────────────────
        if (msg.replyTo != null) {
            holder.tvReplyPreview.visibility = View.VISIBLE
            holder.tvReplyPreview.text = "↩ ${msg.replyTo.sender}: ${msg.replyTo.text.take(60)}"
        }

        // ── Message content ───────────────────────────────────────────────
        if (msg.deleted) {
            holder.tvText.visibility = View.VISIBLE
            holder.tvText.text = "🗑️ This message was deleted"
            holder.tvText.setTextColor(0xFF888888.toInt())
            holder.tvText.setTypeface(null, android.graphics.Typeface.ITALIC)
        } else {
            holder.tvText.setTextColor(0xFFFFFFFF.toInt())
            holder.tvText.setTypeface(null, android.graphics.Typeface.NORMAL)

            when (msg.type) {
                "text" -> {
                    holder.tvText.visibility = View.VISIBLE
                    holder.tvText.text = msg.text
                }
                "image" -> {
                    if (msg.viewOnce) {
                        holder.tvViewOnce.visibility = View.VISIBLE
                        when {
                            msg.viewed && !isMine -> {
                                holder.tvViewOnce.text = "🔥 Photo opened"
                                holder.tvViewOnce.setOnClickListener(null)
                            }
                            !isMine -> {
                                holder.tvViewOnce.text = "🔥 Tap to open · Disappears after viewing"
                                holder.tvViewOnce.setOnClickListener {
                                    onViewOnce(msg.id, "image", msg.url)
                                }
                            }
                            else -> {
                                holder.tvViewOnce.text = if (msg.viewed) "🔥 Disappearing photo · Opened" else "🔥 Disappearing photo · Unopened"
                                holder.tvViewOnce.setOnClickListener(null)
                            }
                        }
                    } else {
                        holder.ivImage.visibility = View.VISIBLE
                        Glide.with(holder.itemView.context).load(msg.url).into(holder.ivImage)
                        holder.ivImage.setOnClickListener { onImageClick(msg.url) }
                        if (msg.isCamera) holder.tvCameraLabel.visibility = View.VISIBLE
                    }
                }
                "video" -> {
                    if (msg.viewOnce) {
                        holder.tvViewOnce.visibility = View.VISIBLE
                        if (msg.viewed && !isMine) {
                            holder.tvViewOnce.text = "🔥 Video opened"
                        } else if (!isMine) {
                            holder.tvViewOnce.text = "🔥 Tap to open video"
                            holder.tvViewOnce.setOnClickListener { onViewOnce(msg.id, "video", msg.url) }
                        } else {
                            holder.tvViewOnce.text = if (msg.viewed) "🔥 Disappearing video · Opened" else "🔥 Disappearing video · Unopened"
                        }
                    } else {
                        holder.videoView.visibility = View.VISIBLE
                        holder.videoView.setVideoPath(msg.url)
                        holder.videoView.setOnPreparedListener { mp -> mp.isLooping = false }
                        val mc = MediaController(holder.itemView.context)
                        mc.setAnchorView(holder.videoView)
                        holder.videoView.setMediaController(mc)
                        if (msg.isCamera) holder.tvCameraLabel.visibility = View.VISIBLE
                    }
                }
                "voice" -> {
                    holder.llVoice.visibility = View.VISIBLE
                    if (msg.transcript.isNotEmpty()) {
                        holder.tvTranscript.visibility = View.VISIBLE
                        holder.tvTranscript.text = "\"${msg.transcript}\""
                    } else {
                        holder.tvTranscript.visibility = View.GONE
                    }
                    val audioUrl = msg.url
                    holder.tvVoiceLabel.text = "🎤 Voice Note — tap to play"
                    holder.llVoice.setOnClickListener {
                        val mp = android.media.MediaPlayer()
                        try {
                            mp.setDataSource(audioUrl)
                            mp.prepareAsync()
                            mp.setOnPreparedListener { it.start() }
                            holder.tvVoiceLabel.text = "🎤 Playing..."
                            mp.setOnCompletionListener { holder.tvVoiceLabel.text = "🎤 Voice Note — tap to play" }
                        } catch (_: Exception) { }
                    }
                }
                "gif" -> {
                    holder.ivImage.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context).asGif().load(msg.url).into(holder.ivImage)
                    holder.ivImage.setOnClickListener { onImageClick(msg.url) }
                }
            }
        }

        // ── Reactions ─────────────────────────────────────────────────────
        holder.llReactions.removeAllViews()
        if (msg.reactions.isNotEmpty()) {
            val counts = mutableMapOf<String, Int>()
            msg.reactions.values.forEach { emoji -> counts[emoji] = (counts[emoji] ?: 0) + 1 }
            counts.forEach { (emoji, count) ->
                val tv = TextView(holder.itemView.context).apply {
                    text = if (count > 1) "$emoji $count" else emoji
                    textSize = 13f
                    setTextColor(0xFFFFFFFF.toInt())
                    setBackgroundResource(R.drawable.bg_reaction)
                    setPadding(8, 4, 8, 4)
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.setMargins(0, 0, 4, 0)
                    layoutParams = lp
                }
                holder.llReactions.addView(tv)
            }
        }

        // ── Timestamp (outside bubble, hidden by default, tap to show) ────
        holder.tvMeta.text = TimeUtils.formatKikTime(msg.time)
        holder.tvMeta.visibility = View.GONE  // hidden; toggled on bubble tap

        // ── WhatsApp-style tick receipt ───────────────────────────────────
        if (isMine) {
            holder.tvReceipt.visibility = View.VISIBLE
            when (msg.receipt) {
                "R" -> {
                    holder.tvReceipt.text = "✓✓"
                    holder.tvReceipt.setTextColor(0xFF00BFFF.toInt())  // blue = read
                }
                "D" -> {
                    holder.tvReceipt.text = "✓✓"
                    holder.tvReceipt.setTextColor(0xFF888888.toInt())  // grey = delivered
                }
                else -> {
                    holder.tvReceipt.text = "✓"
                    holder.tvReceipt.setTextColor(0xFF666666.toInt())  // grey = sent
                }
            }
        } else {
            holder.tvReceipt.visibility = View.GONE
        }

        // ── Tap bubble to toggle timestamp ────────────────────────────────
        holder.llBubble.setOnClickListener {
            holder.tvMeta.visibility =
                if (holder.tvMeta.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // ── Long press for action sheet ───────────────────────────────────
        holder.llBubble.setOnLongClickListener {
            showActionSheet(holder.itemView, msg, isMine)
            true
        }
    }

    private fun showActionSheet(anchor: View, msg: Message, isMine: Boolean) {
        val context = anchor.context
        val popup = android.widget.PopupMenu(context, anchor)

        val emojis = listOf("❤️", "😂", "😮", "😢", "👍", "🔥")
        emojis.forEachIndexed { i, emoji ->
            popup.menu.add(0, i, i, emoji)
        }
        popup.menu.add(0, 100, 100, "↩️ Reply")
        if (isMine && !msg.deleted) popup.menu.add(0, 101, 101, "🗑️ Delete")

        popup.setOnMenuItemClickListener { item ->
            when {
                item.itemId < 100 -> onReaction(msg.id, emojis[item.itemId])
                item.itemId == 100 -> onReply(msg)
                item.itemId == 101 -> onDelete(msg.id)
            }
            true
        }
        popup.show()
    }

    override fun getItemCount() = messages.size
}
