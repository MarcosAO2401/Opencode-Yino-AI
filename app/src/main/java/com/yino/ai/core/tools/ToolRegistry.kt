package com.yino.ai.core.tools

import com.yino.ai.core.llm.ToolSpec
import org.json.JSONObject

class ToolRegistry {

    private val tools = LinkedHashMap<String, Tool>()

    fun register(tool: Tool) {
        tools[tool.id] = tool
    }

    fun get(id: String): Tool? = tools[id]

    fun all(): List<Tool> = tools.values.toList()

    fun specs(): List<ToolSpec> = tools.values.map {
        ToolSpec(it.id, it.description, it.parametersJsonSchema)
    }

    suspend fun execute(id: String, argumentsJson: String, ctx: ToolContext): ToolResult {
        val tool = tools[id] ?: return ToolResult(false, "herramienta desconocida: $id")
        return runCatching { tool.execute(JSONObject(argumentsJson), ctx) }
            .getOrDefault(ToolResult(false, "error ejecutando $id"))
    }
}
