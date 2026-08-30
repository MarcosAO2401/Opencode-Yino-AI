package com.yino.ai.data.memory

import java.util.concurrent.atomic.AtomicLong

class InMemoryMemoryDao : MemoryDao {

    private val conversations = mutableListOf<ConversationEntity>()
    private val messages = mutableListOf<Pair<MessageEntity, Long>>()
    private val convIdSeq = AtomicLong(0)
    private val msgIdSeq = AtomicLong(0)
    private val msgOrderSeq = AtomicLong(0)

    override suspend fun insertConversation(c: ConversationEntity): Long {
        val id = if (c.id != 0L) c.id else convIdSeq.incrementAndGet()
        conversations.add(c.copy(id = id))
        return id
    }

    override suspend fun insertMessage(m: MessageEntity) {
        val id = if (m.id != 0L) m.id else msgIdSeq.incrementAndGet()
        messages.add(m.copy(id = id) to msgOrderSeq.incrementAndGet())
    }

    override suspend fun conversations(): List<ConversationEntity> {
        return conversations.sortedByDescending { it.createdAt }
    }

    override suspend fun messages(c: Long): List<MessageEntity> {
        return messages
            .filter { it.first.conversationId == c }
            .sortedWith(compareBy({ it.first.ts }, { it.second }))
            .map { it.first }
    }

    override suspend fun deleteConversation(c: Long) {
        conversations.removeIf { it.id == c }
        messages.removeIf { it.first.conversationId == c }
    }
}
