package com.yino.ai.data.memory

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryStore(private val dao: MemoryDao) {
    var currentConversationId: Long? = null

    suspend fun append(role: String, content: String): Long {
        return withContext(Dispatchers.IO) {
            val convId = currentConversationId ?: run {
                val id = dao.insertConversation(
                    ConversationEntity(title = "Chat", createdAt = System.currentTimeMillis())
                )
                id
            }
            currentConversationId = convId
            dao.insertMessage(
                MessageEntity(
                    conversationId = convId,
                    role = role,
                    content = content,
                    ts = System.currentTimeMillis()
                )
            )
            convId
        }
    }

    suspend fun history(convId: Long): List<Pair<String, String>> {
        return withContext(Dispatchers.IO) {
            dao.messages(convId).map { it.role to it.content }
        }
    }

    suspend fun listConversations(): List<ConversationEntity> {
        return withContext(Dispatchers.IO) {
            dao.conversations()
        }
    }

    suspend fun clear(convId: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteConversation(convId)
            if (currentConversationId == convId) {
                currentConversationId = null
            }
        }
    }
}
