package com.conzchat.app.db

import androidx.room.*

@Dao
interface VaultMessageDao {
    @Query("SELECT * FROM vault_messages ORDER BY timestamp DESC")
    suspend fun getAll(): List<VaultMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(msg: VaultMessage)

    @Query("DELETE FROM vault_messages WHERE messageId = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM vault_messages")
    suspend fun deleteAll()
}
