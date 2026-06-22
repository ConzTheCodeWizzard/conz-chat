package com.conzchat.app.db

import androidx.room.*

@Dao
interface ScheduledMessageDao {
    @Query("SELECT * FROM scheduled_messages WHERE sent = 0 ORDER BY scheduledAt ASC")
    suspend fun getPending(): List<ScheduledMessage>

    @Query("SELECT * FROM scheduled_messages ORDER BY scheduledAt DESC")
    suspend fun getAll(): List<ScheduledMessage>

    @Insert
    suspend fun insert(msg: ScheduledMessage)

    @Query("UPDATE scheduled_messages SET sent = 1 WHERE id = :id")
    suspend fun markSent(id: Int)

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM scheduled_messages")
    suspend fun deleteAll()
}
