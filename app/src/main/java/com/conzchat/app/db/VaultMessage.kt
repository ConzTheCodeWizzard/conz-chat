package com.conzchat.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_messages")
data class VaultMessage(
    @PrimaryKey val messageId: String = "",
    val chatId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val isImage: Boolean = false,
    val imageUrl: String = ""
)
