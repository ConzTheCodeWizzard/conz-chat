package com.conzchat.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_messages")
data class ScheduledMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String = "",
    val recipientName: String = "",
    val message: String = "",
    val scheduledAt: Long = 0L,
    val sent: Boolean = false
)
