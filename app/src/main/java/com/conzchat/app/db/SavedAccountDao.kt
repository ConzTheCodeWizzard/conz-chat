package com.conzchat.app.db

import androidx.room.*

@Dao
interface SavedAccountDao {
    @Query("SELECT * FROM saved_accounts ORDER BY savedAt DESC")
    suspend fun getAll(): List<SavedAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: SavedAccount)

    @Query("DELETE FROM saved_accounts WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM saved_accounts")
    suspend fun deleteAll()

    @Query("SELECT * FROM saved_accounts WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): SavedAccount?
}
