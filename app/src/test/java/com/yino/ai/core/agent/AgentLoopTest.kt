package com.yino.ai.core.agent

import com.yino.ai.core.llm.LLMProvider
import com.yino.ai.core.llm.LLMRequest
import com.yino.ai.core.llm.LLMResult
import com.yino.ai.core.llm.Role
import com.yino.ai.core.security.SecurityGate
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolRegistry
import com.yino.ai.core.tools.ToolResult
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test

class AgentLoopTest {

    private class MockLLMProvider(var response: LLMResult) : LLMProvider {
        override val id = "mock"
        override val supportsTools = true
        override suspend fun complete(request: LLMRequest): LLMResult = response
    }

    private class TestTool(
        override val id: String,
        val result: ToolResult
    ) : Tool {
        override val description = "Test tool"
        override val parametersJsonSchema = "{}"
        override val risk = ActionRisk.LOW
        override val requiredPermissions = emptyList<String>()
        override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult = result
    }

    @Test
    fun `returns text response directly when LLM returns text`() = runBlockingTest {
        val llm = MockLLMProvider(LLMResult.Text("Hello world"))
        val registry = ToolRegistry()
        val gate = SecurityGate().apply { interactive = false }
        val agent = AgentLoop(llm, registry, gate, { false }, { emptySet() })

        val result = agent.run("Hello")
        assertEquals("Hello world", result)
    }

    @Test
    fun `executes tool and continues with tool result`() = runBlockingTest {
        var callCount = 0
        val llm = object : LLMProvider {
            override val id = "test"
            override val supportsTools = true
            override suspend fun complete(request: LLMRequest): LLMResult {
                callCount++
                return if (callCount == 1) {
                    LLMResult.ToolCall("echo_tool", """{"message":"test"}""")
                } else {
                    LLMResult.Text("Done")
                }
            }
        }
        val registry = ToolRegistry().apply {
            register(object : Tool {
                override val id = "echo_tool"
                override val description = "Echo"
                override val parametersJsonSchema = "{}"
                override val risk = ActionRisk.LOW
                override val requiredPermissions = emptyList<String>()
                override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult =
                    ToolResult(true, "Echoed: test")
            })
        }
        val gate = SecurityGate().apply { interactive = false }
        val agent = AgentLoop(llm, registry, gate, { false }, { emptySet() }, maxSteps = 3)

        val result = agent.run("Echo test")
        assertEquals("Done", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `handles tool execution error gracefully`() = runBlockingTest {
        val llm = MockLLMProvider(LLMResult.ToolCall("fail_tool", "{}"))
        val registry = ToolRegistry().apply {
            register(object : Tool {
                override val id = "fail_tool"
                override val description = "Fails"
                override val parametersJsonSchema = "{}"
                override val risk = ActionRisk.LOW
                override val requiredPermissions = emptyList<String>()
                override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult =
                    ToolResult(false, "Tool failed")
            })
        }
        val gate = SecurityGate().apply { interactive = false }
        val agent = AgentLoop(llm, registry, gate, { false }, { emptySet() }, maxSteps = 3)

        val result = agent.run("Test")
        assertTrue("Should handle error gracefully", result.contains("Tool failed") || result.contains("fallido"))
    }

    @Test
    fun `denies tool execution when security gate denies`() = runBlockingTest {
        val llm = MockLLMProvider(LLMResult.ToolCall("risky_tool", "{}"))
        val registry = ToolRegistry().apply {
            register(object : Tool {
                override val id = "risky_tool"
                override val description = "Risky"
                override val parametersJsonSchema = "{}"
                override val risk = ActionRisk.HIGH
                override val requiredPermissions = emptyList<String>()
                override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult =
                    ToolResult(true, "Should not execute")
            })
        }
        val gate = SecurityGate().apply { interactive = false } // fail-closed
        val agent = AgentLoop(llm, registry, gate, { false }, { emptySet() }, maxSteps = 3)

        val result = agent.run("Test")
        assertTrue("Should indicate denial", result.contains("denegad") || result.contains("denied") || result.contains("Acción denegada"))
    }

    @Test
    fun `stops at maxSteps and summarizes`() = runBlockingTest {
        var callCount = 0
        val llm = object : LLMProvider {
            override val id = "test"
            override val supportsTools = true
            override suspend fun complete(request: LLMRequest): LLMResult {
                callCount++
                // Always return tool call to force maxSteps
                return LLMResult.ToolCall("step_tool", "{}")
            }
        }
        val registry = ToolRegistry().apply {
            register(object : Tool {
                override val id = "step_tool"
                override val description = "Step"
                override val parametersJsonSchema = "{}"
                override val risk = ActionRisk.LOW
                override val requiredPermissions = emptyList<String>()
                override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult =
                    ToolResult(true, "step done")
            })
        }
        val gate = SecurityGate().apply { interactive = false }
        val agent = AgentLoop(llm, registry, gate, { false }, { emptySet() }, maxSteps = 2)

        val result = agent.run("Test")
        // Should make 2 tool calls + 1 final summary call = 3 total
        assertEquals(3, callCount)
        // Result should be the summary from the final LLM call
        assertTrue("Should indicate max steps reached with summary", result.contains("completado") || result.contains("continu") || result.contains("step done"))
    }

    @Test
    fun `handles unknown tool gracefully`() = runBlockingTest {
        val llm = MockLLMProvider(LLMResult.ToolCall("unknown_tool", "{}"))
        val registry = ToolRegistry() // empty
        val gate = SecurityGate().apply { interactive = false }
        val agent = AgentLoop(llm, registry, gate, { false }, { emptySet() })

        val result = agent.run("Test")
        assertTrue("Should handle unknown tool", result.contains("no existe") || result.contains("unknown"))
    }

    @Test
    fun `handles LLM exception gracefully`() = runBlockingTest {
        val llm = object : LLMProvider {
            override val id = "error"
            override val supportsTools = true
            override suspend fun complete(request: LLMRequest): LLMResult =
                throw RuntimeException("Network error")
        }
        val registry = ToolRegistry()
        val gate = SecurityGate().apply { interactive = false }
        val agent = AgentLoop(llm, registry, gate, { false }, { emptySet() })

        val result = agent.run("Test")
        assertTrue("Should handle exception", result.contains("Error") || result.contains("Error del LLM"))
    }
}