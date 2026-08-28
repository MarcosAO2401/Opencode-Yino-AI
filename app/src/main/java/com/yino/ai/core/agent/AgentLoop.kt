package com.yino.ai.core.agent

import com.yino.ai.core.llm.ChatMessage
import com.yino.ai.core.llm.LLMProvider
import com.yino.ai.core.llm.LLMRequest
import com.yino.ai.core.llm.LLMResult
import com.yino.ai.core.llm.Role
import com.yino.ai.core.security.AuditLog
import com.yino.ai.core.security.SecurityGate
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolRegistry

/**
 * Bucle del agente tipo ReAct (Observation -> Plan -> Action -> Verification).
 * Inspirado en OpenDroid/ClosePaw pero propio y agnóstico al LLM.
 *
 * El prompt de sistema instruye al modelo a devolver tool_calls cuando
 * necesita actuar sobre el dispositivo, o texto cuando solo conversa.
 */
class AgentLoop(
    private val llm: LLMProvider,
    private val registry: ToolRegistry,
    private val security: SecurityGate,
    private val accessibilityAvailable: () -> Boolean,
    private val grantedPermissions: () -> Set<String>,
    private val maxSteps: Int = 8,
) {

    suspend fun run(userInput: String): String {
        val history = mutableListOf(
            ChatMessage(Role.SYSTEM, SYSTEM_PROMPT),
            ChatMessage(Role.USER, userInput),
        )

        repeat(maxSteps) { step ->
            val request = LLMRequest(messages = history, tools = registry.specs())
            when (val result = llm.complete(request)) {
                is LLMResult.Text -> {
                    history += ChatMessage(Role.ASSISTANT, result.content)
                    return result.content
                }
                is LLMResult.ToolCall -> {
                    val tool = registry.get(result.name)
                    if (tool == null) {
                        history += ChatMessage(Role.TOOL, "error: herramienta '${result.name}' no existe")
                        return@repeat
                    }
                    val approved = security.authorize(
                        tool.id, tool.risk,
                        "ejecutar ${tool.id} con ${result.argumentsJson}",
                    )
                    if (!approved) {
                        AuditLog.record(tool.id, tool.risk.name, false, "denegado")
                        history += ChatMessage(Role.TOOL, "Acción denegada por el usuario: ${tool.id}")
                        return@repeat
                    }
                    val ctx = ToolContext(accessibilityAvailable(), grantedPermissions())
                    val res = registry.execute(tool.id, result.argumentsJson, ctx)
                    AuditLog.record(tool.id, tool.risk.name, true, res.message)
                    history += ChatMessage(Role.TOOL, "[${tool.id}] ${res.message}")
                    if (res.success && step == maxSteps - 1) {
                        // Deja que el modelo resuma tras la última acción.
                    }
                }
            }
        }
        return "He completado el paso disponible. ¿Quieres que continúe?"
    }

    companion object {
        val SYSTEM_PROMPT = """
Eres Yino, un asistente personal de Android. Tienes herramientas para
controlar el dispositivo (abrir apps, enviar mensajes, leer la pantalla,
etc.). Reglas:
- Si solo conversas, responde texto.
- Si debes actuar, devuelve UN tool_call con argumentos JSON válidos.
- Nunca inventes herramientas que no tengas listadas.
- Confirma siempre implícitamente el riesgo; el sistema pedirá aprobación.
- Sé breve y útil.
""".trimIndent()
    }
}
