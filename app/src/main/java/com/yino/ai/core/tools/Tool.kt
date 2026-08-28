package com.yino.ai.core.tools

import org.json.JSONObject

/**
 * Nivel de riesgo de una acción. Define si requiere confirmación
 * del usuario antes de ejecutarse (ver SecurityGate).
 */
enum class ActionRisk { LOW, MEDIUM, HIGH }

/**
 * Contexto disponible para cualquier herramienta: acceso a la pantalla,
 * permisos concedidos y servicios del sistema.
 */
data class ToolContext(
    val accessibilityAvailable: Boolean,
    val permissions: Set<String>,
)

data class ToolResult(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any> = emptyMap(),
)

/**
 * Contrato de toda capacidad de Yino. Las herramientas se registran en
 * [ToolRegistry] y el AgentLoop las invoca según lo decida el LLM.
 */
interface Tool {
    val id: String
    val description: String
    val parametersJsonSchema: String
    val risk: ActionRisk
    val requiredPermissions: List<String>
    suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult
}
