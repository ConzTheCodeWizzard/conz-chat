package com.conzchat.app.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.conzchat.app.R
import com.conzchat.app.model.GroupMessage
import com.conzchat.app.util.TimeUtils

class GroupMessageAdapter(
    private val messages: List<GroupMessage>,
    private val myUid: String,
    private val onProfileClick: (String) -> Unit
) : RecyclerView.Adapter<GroupMessageAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val llWrap: LinearLayout = view.findViewById(R.id.llWrap)
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val tvSenderName: TextView = view.findViewById(R.id.tvSenderName)
        val tvText: TextView = view.findViewById(R.id.tvText)
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_message, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = messages[position]
        val isMine = msg.from == myUid

        holder.llWrap.gravity = if (isMine) android.view.Gravity.END else android.view.Gravity.START

        if (isMine) {
            holder.ivAvatar.visibility = View.GONE
            holder.tvSenderName.visibility = View.GONE
        } else {
            holder.ivAvatar.visibility = View.VISIBLE
            holder.tvSenderName.visibility = View.VISIBLE
            holder.tvSenderName.text = msg.senderName
            if (msg.senderPhoto.isNotEmpty()) {
                Glide.with(holder.itemView.context).load(msg.senderPhoto)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(holder.ivAvatar)
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }
            holder.ivAvatar.setOnClickListener { onProfileClick(msg.from) }
        }

        if (msg.deleted) {
            holder.tvText.visibility = View.VISIBLE
            holder.tvText.text = "🗑️ This message was deleted"
            holder.tvText.setTextColor(0xFF888888.toInt())
            holder.ivImage.visibility = View.GONE
        } else {
            when (msg.type) {
                "image" -> {
                    holder.tvText.visibility = View.GONE
                    holder.ivImage.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context).load(msg.url).into(holder.ivImage)
                }
                else -> {
                    holder.tvText.visibility = View.VISIBLE
                    holder.tvText.text = msg.text
                    holder.tvText.setTextColor(0xFFFFFFFF.toInt())
                    holder.ivImage.visibility = View.GONE
                }
            }
        }

        holder.tvTime.text = TimeUtils.formatKikTime(msg.time)
    }

    override fun getItemCount() = messages.size
}
