package com.conzchat.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ScheduledMessage::class, VaultMessage::class, SavedAccount::class],
    version = 2,
    exportSchema = false
)
abstract class ConzDatabase : RoomDatabase() {
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun vaultMessageDao(): VaultMessageDao
    abstract fun savedAccountDao(): SavedAccountDao

    companion object {
        @Volatile private var INSTANCE: ConzDatabase? = null

        fun get(context: Context): ConzDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ConzDatabase::class.java,
                    "conzchat_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
