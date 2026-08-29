package com.yino.ai.data.memory

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones de Room para Yino AI.
 * v1 -> v2: Añade índices para optimizar consultas frecuentes.
 */
object Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Índice para conversations.createdAt (ordenamiento por fecha)
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_conversations_createdAt ON conversations(createdAt)"
            )
            // Índice compuesto para messages (conversationId + ts) - consultas por conversación ordenadas por timestamp
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_messages_conversationId_ts ON messages(conversationId, ts)"
            )
        }
    }
}