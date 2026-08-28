package com.yino.ai.data.memory

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MemoryDao {
    @Insert
    suspend fun insertConversation(c: ConversationEntity): Long

    @Insert
    suspend fun insertMessage(m: MessageEntity)

    @Query("SELECT * FROM conversations ORDER BY createdAt DESC")
    suspend fun conversations(): List<ConversationEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :c ORDER BY ts ASC")
    suspend fun messages(c: Long): List<MessageEntity>

    @Query("DELETE FROM conversations WHERE id = :c")
    suspend fun deleteConversation(c: Long)
}
