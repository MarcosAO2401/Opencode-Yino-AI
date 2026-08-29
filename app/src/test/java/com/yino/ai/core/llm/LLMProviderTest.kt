package com.yino.ai.core.llm

import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test

class LLMProviderTest {

    @Test
    fun `LLMRequest carries messages and tools`() {
        val request = LLMRequest(
            messages = listOf(
                ChatMessage(Role.SYSTEM, "You are helpful"),
                ChatMessage(Role.USER, "Hello")
            ),
            tools = listOf(ToolSpec("test_tool", "Test", "{}"))
        )

        assertEquals(2, request.messages.size)
        assertEquals(1, request.tools.size)
        assertEquals("test_tool", request.tools[0].name)
    }

    @Test
    fun `LLMResult.Text holds content`() {
        val result = LLMResult.Text("Hello world")
        assertEquals("Hello world", result.content)
    }

    @Test
    fun `LLMResult.ToolCall holds name and arguments`() {
        val result = LLMResult.ToolCall("test_tool", """{"key":"value"}""")
        assertEquals("test_tool", result.name)
        assertEquals("""{"key":"value"}""", result.arguments)
    }

    @Test
    fun `ToolSpec carries schema`() {
        val spec = ToolSpec(
            name = "test",
            description = "Test tool",
            parametersJsonSchema = """{"type":"object","properties":{"x":{"type":"string"}}}"""
        )
        assertEquals("test", spec.name)
        assertEquals("Test tool", spec.description)
        assertTrue(spec.parametersJsonSchema.contains("x"))
    }
}