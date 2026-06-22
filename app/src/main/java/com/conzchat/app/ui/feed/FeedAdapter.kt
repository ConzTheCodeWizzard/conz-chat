package com.conzchat.app.ui.feed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.conzchat.app.R
import com.conzchat.app.databinding.ItemFeedPostBinding
import com.conzchat.app.model.FeedPost
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class FeedAdapter(
    private val posts: List<FeedPost>,
    private val context: Context,
    private val onLike: (FeedPost) -> Unit,
    private val onComment: (FeedPost) -> Unit,
    private val onShare: (FeedPost) -> Unit,
    private val onProfileClick: (FeedPost) -> Unit
) : RecyclerView.Adapter<FeedAdapter.VH>() {

    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val dateFormat = SimpleDateFormat("EEE h:mm a", Locale.getDefault())

    inner class VH(val binding: ItemFeedPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemFeedPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val post = posts[position]
        val b = holder.binding

        // Avatar
        if (post.avatarUrl.isNotEmpty()) {
            Glide.with(context).load(post.avatarUrl).placeholder(R.drawable.ic_default_avatar).into(b.ivAvatar)
        } else {
            b.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        // Username and time
        b.tvUsername.text = post.username.ifEmpty { "Anonymous" }
        b.tvTime.text = if (post.timestamp > 0) dateFormat.format(Date(post.timestamp)) else ""

        // Post text
        b.tvPostText.text = post.text
        b.tvPostText.visibility = if (post.text.isNotEmpty()) View.VISIBLE else View.GONE

        // Media
        if (post.mediaUrl.isNotEmpty() && post.mediaType.startsWith("image")) {
            b.ivMedia.visibility = View.VISIBLE
            b.videoPlayOverlay.visibility = View.GONE
            Glide.with(context).load(post.mediaUrl).into(b.ivMedia)
        } else if (post.mediaUrl.isNotEmpty() && post.mediaType.startsWith("video")) {
            b.ivMedia.visibility = View.VISIBLE
            b.videoPlayOverlay.visibility = View.VISIBLE
            Glide.with(context).load(post.mediaUrl).into(b.ivMedia)
        } else {
            b.ivMedia.visibility = View.GONE
            b.videoPlayOverlay.visibility = View.GONE
        }

        // Like
        val liked = uid in post.likes
        b.btnLike.setImageResource(if (liked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        b.tvLikeCount.text = post.likes.size.toString()

        // Comment count
        b.tvCommentCount.text = post.commentCount.toString()

        // Click listeners
        b.btnLike.setOnClickListener { onLike(post) }
        b.btnComment.setOnClickListener { onComment(post) }
        b.btnShare.setOnClickListener { onShare(post) }
        b.ivAvatar.setOnClickListener { onProfileClick(post) }
        b.tvUsername.setOnClickListener { onProfileClick(post) }
    }
}
