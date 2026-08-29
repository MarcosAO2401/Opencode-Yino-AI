package com.yino.ai.data.memory

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yino.ai.data.memory.ConversationEntity
import com.yino.ai.data.memory.MessageEntity
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MemoryRepositoryTest {

    private lateinit var db: MemoryDatabase
    private lateinit var repo: MemoryRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, MemoryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = MemoryRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `append and retrieve message`() = runBlockingTest {
        val convId = repo.append("user", "Hello")
        assertTrue("Conversation ID should be positive", convId > 0)

        val history = repo.history(convId)
        assertEquals(1, history.size)
        assertEquals("user", history[0].first)
        assertEquals("Hello", history[0].second)
    }

    @Test
    fun `append multiple messages to same conversation`() = runBlockingTest {
        val convId = repo.append("user", "Hello")
        repo.append("assistant", "Hi there")
        repo.append("user", "How are you?")

        val history = repo.history(convId)
        assertEquals(3, history.size)
        assertEquals("user", history[0].first)
        assertEquals("Hello", history[0].second)
        assertEquals("assistant", history[1].first)
        assertEquals("Hi there", history[1].second)
        assertEquals("user", history[2].first)
        assertEquals("How are you?", history[2].second)
    }

    @Test
    fun `separate conversations have separate IDs`() = runBlockingTest {
        val conv1 = repo.append("user", "Conv 1")
        val conv2 = repo.append("user", "Conv 2")
        assertNotEquals("Conversations should have different IDs", conv1, conv2)
    }

    @Test
    fun `loadLatest returns recent messages`() = runBlockingTest {
        repo.append("user", "Old message")
        repo.append("assistant", "Old reply")
        repo.append("user", "New message")
        repo.append("assistant", "New reply")

        val latest = repo.loadLatest(10)
        assertEquals(4, latest.size)
        assertEquals("Old message", latest[0].second)
        assertEquals("New reply", latest[3].second)
    }

    @Test
    fun `loadLatest respects limit`() = runBlockingTest {
        repeat(15) { i ->
            repo.append("user", "Msg $i")
        }
        val latest = repo.loadLatest(5)
        assertEquals(5, latest.size)
        assertEquals("Msg 14", latest[4].second) // Most recent
    }

    @Test
    fun `listConversations returns all conversations`() = runBlockingTest {
        repo.append("user", "Conv A")
        repo.append("user", "Conv B")
        repo.append("user", "Conv C")

        val conversations = repo.listConversations()
        assertEquals(3, conversations.size)
        assertEquals("Conv C", conversations[0].title) // Most recent first
    }

    @Test
    fun `clear removes conversation and messages`() = runBlockingTest {
        val convId = repo.append("user", "To be deleted")
        repo.append("assistant", "Reply")
        repo.clear(convId)

        val history = repo.history(convId)
        assertTrue("History should be empty after clear", history.isEmpty())

        val conversations = repo.listConversations()
        assertTrue("Conversation should be removed", conversations.none { it.id == convId })
    }

    @Test
    fun `foreign key cascade deletes messages when conversation deleted`() = runBlockingTest {
        val convId = repo.append("user", "Test")
        repo.append("assistant", "Reply 1")
        repo.append("assistant", "Reply 2")

        val before = repo.history(convId)
        assertEquals(3, before.size)

        repo.clear(convId)

        val after = repo.history(convId)
        assertTrue(after.isEmpty())
    }
}