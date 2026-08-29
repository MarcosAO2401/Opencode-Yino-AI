package com.yino.ai.data.memory

import android.content.Context
import androidx.room.RoomDatabase

class MemoryRepository private constructor(private val store: MemoryStore) {

    constructor(context: Context) : this(MemoryStore(MemoryDatabase.getInstance(context).dao()))

    constructor(dao: MemoryDao) : this(MemoryStore(dao))

    suspend fun append(role: String, content: String): Long = store.append(role, content)

    suspend fun history(convId: Long): List<Pair<String, String>> = store.history(convId)

    suspend fun listConversations(): List<ConversationEntity> = store.listConversations()

    suspend fun clear(convId: Long) = store.clear(convId)

    suspend fun loadLatest(limit: Int = 50): List<Pair<String, String>> {
        val latest = store.listConversations().firstOrNull() ?: return emptyList()
        return store.history(latest.id).takeLast(limit)
    }
}