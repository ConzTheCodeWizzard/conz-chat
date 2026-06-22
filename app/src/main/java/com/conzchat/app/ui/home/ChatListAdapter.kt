package com.conzchat.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.conzchat.app.R
import com.conzchat.app.model.ChatListItem
import com.conzchat.app.util.TimeUtils

class ChatListAdapter(
    private val items: List<ChatListItem>,
    private val onClick: (ChatListItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvUnread: TextView = view.findViewById(R.id.tvUnread)
        val tvBadge: TextView = view.findViewById(R.id.tvBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Avatar
        if (item.type == "conzAI") {
            holder.ivAvatar.setImageResource(R.drawable.ic_conzai)
        } else if (item.photo.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.photo)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_default_avatar)
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        // Name
        holder.tvName.text = item.name

        // Last message
        holder.tvLastMessage.text = item.lastMessage.take(50)

        // Time
        holder.tvTime.text = if (item.lastTime > 0) TimeUtils.formatKikTime(item.lastTime) else ""

        // Unread badge
        if (item.unreadCount > 0) {
            holder.tvUnread.visibility = View.VISIBLE
            holder.tvUnread.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
        } else {
            holder.tvUnread.visibility = View.GONE
        }

        // Type badge
        when (item.type) {
            "conzAI" -> {
                holder.tvBadge.visibility = View.VISIBLE
                holder.tvBadge.text = "AI"
            }
            "group" -> {
                holder.tvBadge.visibility = View.VISIBLE
                holder.tvBadge.text = "GRP"
            }
            "publicGroup" -> {
                holder.tvBadge.visibility = View.VISIBLE
                holder.tvBadge.text = "#"
            }
            else -> holder.tvBadge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
