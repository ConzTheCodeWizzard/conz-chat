package com.conzchat.app.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.conzchat.app.R
import com.conzchat.app.model.Message
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.TimeUtils

class ConzAIMessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ConzAIMessageAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val llWrap: LinearLayout = view.findViewById(R.id.llWrap)
        val tvText: TextView = view.findViewById(R.id.tvText)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ai_message, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = messages[position]
        val isMe = msg.from == FirebaseManager.currentUid
        holder.llWrap.gravity = if (isMe) android.view.Gravity.END else android.view.Gravity.START
        holder.tvText.text = msg.text
        holder.tvTime.text = TimeUtils.formatKikTime(msg.time)
        holder.tvText.setBackgroundResource(if (isMe) R.drawable.bg_bubble_me else R.drawable.bg_bubble_them)
    }

    override fun getItemCount() = messages.size
}
