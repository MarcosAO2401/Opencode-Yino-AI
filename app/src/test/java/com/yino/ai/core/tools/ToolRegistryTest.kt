package com.yino.ai.core.tools

import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test

class ToolRegistryTest {

    @Test
    fun `register and get tool by id`() = runBlockingTest {
        val registry = ToolRegistry()
        val tool = object : Tool {
            override val id = "test_tool"
            override val description = "Test tool"
            override val parametersJsonSchema = "{}"
            override val risk = ActionRisk.LOW
            override val requiredPermissions = emptyList()
            override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult =
                ToolResult(true, "ok")
        }

        registry.register(tool)
        val retrieved = registry.get("test_tool")
        assertNotNull("Tool should be registered", retrieved)
        assertEquals("test_tool", retrieved?.id)
    }

    @Test
    fun `get returns null for unknown tool`() = runBlockingTest {
        val registry = ToolRegistry()
        val retrieved = registry.get("unknown_tool")
        assertNull("Unknown tool should return null", retrieved)
    }

    @Test
    fun `execute calls tool and returns result`() = runBlockingTest {
        val registry = ToolRegistry()
        val tool = object : Tool {
            override val id = "exec_tool"
            override val description = "Exec tool"
            override val parametersJsonSchema = "{}"
            override val risk = ActionRisk.LOW
            override val requiredPermissions = emptyList()
            override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult =
                ToolResult(true, "executed")
        }
        registry.register(tool)

        val ctx = ToolContext(accessibilityAvailable = false, grantedPermissions = emptySet())
        val result = registry.execute("exec_tool", "{}", ctx)
        assertTrue("Execution should succeed", result.success)
        assertEquals("executed", result.message)
    }

    @Test
    fun `execute returns error for unknown tool`() = runBlockingTest {
        val registry = ToolRegistry()
        val ctx = ToolContext(accessibilityAvailable = false, grantedPermissions = emptySet())
        val result = registry.execute("unknown", "{}", ctx)
        assertFalse("Unknown tool should fail", result.success)
        assertTrue("Error message should mention unknown", result.message.contains("no existe"))
    }

    @Test
    fun `specs returns all registered tools schemas`() = runBlockingTest {
        val registry = ToolRegistry()
        val tool1 = object : Tool {
            override val id = "tool1"
            override val description = "Tool 1"
            override val parametersJsonSchema = "{\"type\":\"object\"}"
            override val risk = ActionRisk.LOW
            override val requiredPermissions = emptyList()
            override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult = ToolResult(true, "")
        }
        val tool2 = object : Tool {
            override val id = "tool2"
            override val description = "Tool 2"
            override val parametersJsonSchema = "{\"type\":\"object\",\"properties\":{\"x\":{\"type\":\"string\"}}}"
            override val risk = ActionRisk.MEDIUM
            override val requiredPermissions = emptyList()
            override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult = ToolResult(true, "")
        }
        registry.register(tool1)
        registry.register(tool2)

        val specs = registry.specs()
        assertEquals(2, specs.size)
        val spec1 = specs.find { it.name == "tool1" }
        val spec2 = specs.find { it.name == "tool2" }
        assertNotNull(spec1)
        assertNotNull(spec2)
        assertEquals(ActionRisk.LOW, spec1?.risk)
        assertEquals(ActionRisk.MEDIUM, spec2?.risk)
    }

    @Test
    fun `duplicate registration overwrites`() = runBlockingTest {
        val registry = ToolRegistry()
        val tool1 = object : Tool {
            override val id = "dup_tool"
            override val description = "v1"
            override val parametersJsonSchema = "{}"
            override val risk = ActionRisk.LOW
            override val requiredPermissions = emptyList()
            override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult = ToolResult(true, "v1")
        }
        val tool2 = object : Tool {
            override val id = "dup_tool"
            override val description = "v2"
            override val parametersJsonSchema = "{}"
            override val risk = ActionRisk.HIGH
            override val requiredPermissions = emptyList()
            override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult = ToolResult(true, "v2")
        }
        registry.register(tool1)
        registry.register(tool2)

        val retrieved = registry.get("dup_tool")
        assertNotNull(retrieved)
        assertEquals(ActionRisk.HIGH, retrieved?.risk) // should be overwritten
    }
}