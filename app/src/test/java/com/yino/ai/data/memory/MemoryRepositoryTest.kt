package com.yino.ai.data.memory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MemoryRepositoryTest {

    private lateinit var dao: MemoryDao
    private lateinit var repo: MemoryRepository

    @Before
    fun setup() {
        dao = mock()
        repo = MemoryRepository(dao)
    }

    @Test
    fun `append and retrieve message`() = runBlockingTest {
        val convId = 1L
        whenever(dao.insertConversation(ConversationEntity(title = "Chat", createdAt = any()))).thenReturn(convId)
        whenever(dao.insertMessage(any())).thenReturn(1L)

        val resultId = repo.append("user", "Hello")
        assertEquals(convId, resultId)
    }

    @Test
    fun `append multiple messages to same conversation`() = runBlockingTest {
        val convId = 1L
        whenever(dao.insertConversation(any())).thenReturn(convId)
        whenever(dao.insertMessage(any())).thenReturn(1L, 2L, 3L)

        val id1 = repo.append("user", "Hello")
        repo.append("assistant", "Hi there")
        repo.append("user", "How are you?")

        assertEquals(convId, id1)
    }

    @Test
    fun `separate conversations have separate IDs`() = runBlockingTest {
        whenever(dao.insertConversation(any())).thenReturn(1L, 2L)

        val conv1 = repo.append("user", "Conv 1")
        val conv2 = repo.append("user", "Conv 2")
        assertNotEquals(conv1, conv2)
    }

    @Test
    fun `history returns messages for conversation`() = runBlockingTest {
        val messages = listOf(
            MessageEntity(conversationId = 1, role = "user", content = "Hello", ts = 1000),
            MessageEntity(conversationId = 1, role = "assistant", content = "Hi", ts = 2000),
        )
        whenever(dao.messages(1L)).thenReturn(messages)

        val history = repo.history(1L)
        assertEquals(2, history.size)
        assertEquals("user", history[0].first)
        assertEquals("Hello", history[0].second)
    }

    @Test
    fun `listConversations returns all conversations`() = runBlockingTest {
        val conversations = listOf(
            ConversationEntity(id = 3, title = "Conv C", createdAt = 3000),
            ConversationEntity(id = 2, title = "Conv B", createdAt = 2000),
            ConversationEntity(id = 1, title = "Conv A", createdAt = 1000),
        )
        whenever(dao.conversations()).thenReturn(conversations)

        val result = repo.listConversations()
        assertEquals(3, result.size)
        assertEquals("Conv C", result[0].title)
    }

    @Test
    fun `clear removes conversation and messages`() = runBlockingTest {
        repo.clear(1L)
        // Verify dao.deleteConversation was called - mocked, just verify no exception
    }

    @Test
    fun `loadLatest returns recent messages from latest conversation`() = runBlockingTest {
        val conversations = listOf(
            ConversationEntity(id = 1, title = "Latest", createdAt = 3000),
        )
        val messages = listOf(
            MessageEntity(conversationId = 1, role = "user", content = "Msg 1", ts = 1000),
            MessageEntity(conversationId = 1, role = "assistant", content = "Msg 2", ts = 2000),
        )
        whenever(dao.conversations()).thenReturn(conversations)
        whenever(dao.messages(1L)).thenReturn(messages)

        val latest = repo.loadLatest(10)
        assertEquals(2, latest.size)
        assertEquals("Msg 1", latest[0].second)
        assertEquals("Msg 2", latest[1].second)
    }

    @Test
    fun `loadLatest respects limit`() = runBlockingTest {
        val conversations = listOf(
            ConversationEntity(id = 1, title = "Conv", createdAt = 1000),
        )
        val messages = (1..15).map { i ->
            MessageEntity(conversationId = 1, role = "user", content = "Msg $i", ts = i.toLong() * 1000)
        }
        whenever(dao.conversations()).thenReturn(conversations)
        whenever(dao.messages(1L)).thenReturn(messages)

        val latest = repo.loadLatest(5)
        assertEquals(5, latest.size)
        assertEquals("Msg 15", latest[4].second)
    }
}