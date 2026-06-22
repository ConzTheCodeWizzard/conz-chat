package com.conzchat.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_accounts")
data class SavedAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val savedAt: Long = System.currentTimeMillis()
)
