package com.yino.ai.core.tools

import com.yino.ai.core.llm.ToolSpec
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import org.json.JSONObject

class ToolRegistry {

    private val tools = LinkedHashMap<String, Tool>()
    private val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
    private val schemas = mutableMapOf<String, JsonSchema>()

    fun register(tool: Tool) {
        tools[tool.id] = tool
        try {
            val schemaNode = com.fasterxml.jackson.databind.ObjectMapper().readTree(tool.parametersJsonSchema)
            schemas[tool.id] = schemaFactory.getSchema(schemaNode)
        } catch (_: Exception) {
            // Un esquema inválido no debe derribar el registro completo de herramientas.
        }
    }

    fun get(id: String): Tool? = tools[id]

    fun all(): List<Tool> = tools.values.toList()

    fun specs(): List<ToolSpec> = tools.values.map {
        ToolSpec(it.id, it.description, it.parametersJsonSchema)
    }

    suspend fun execute(id: String, argumentsJson: String, ctx: ToolContext): ToolResult {
        val tool = tools[id] ?: return ToolResult(false, "herramienta desconocida (no existe): $id")

        val missingPermissions = tool.requiredPermissions.filterNot { it in ctx.permissions }
        if (missingPermissions.isNotEmpty()) {
            return ToolResult(
                false,
                "No se puede ejecutar $id: faltan permisos requeridos (${missingPermissions.joinToString()}).",
            )
        }

        val schema = schemas[id]
        if (schema != null) {
            try {
                val argsNode = com.fasterxml.jackson.databind.ObjectMapper().readTree(argumentsJson)
                val errors: Set<ValidationMessage> = schema.validate(argsNode)
                if (errors.isNotEmpty()) {
                    return ToolResult(false, "Argumentos inválidos para $id: ${errors.joinToString(", ") { it.message }}")
                }
            } catch (e: Exception) {
                return ToolResult(false, "JSON inválido para $id: ${e.message}")
            }
        }

        return runCatching { tool.execute(JSONObject(argumentsJson), ctx) }
            .getOrElse { e -> ToolResult(false, "Error ejecutando $id: ${e.message ?: e.javaClass.simpleName}") }
    }
}
