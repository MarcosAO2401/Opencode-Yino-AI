package com.yino.ai.data.memory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MemoryDatabase : RoomDatabase() {
    abstract fun dao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: MemoryDatabase? = null

        fun getInstance(context: Context): MemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MemoryDatabase::class.java,
                    "yino_memory"
                ).addMigrations(Migrations.MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }
    }
}
