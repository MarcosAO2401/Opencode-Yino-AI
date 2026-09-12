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
import kotlinx.coroutines.delay

/** Núcleo del agente Yino: Observation -> Plan -> Action -> Verification. */
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
        var lastToolMessage: String? = null
        var toolFailures = 0

        repeat(maxSteps) { step ->
            val result = try {
                llm.complete(LLMRequest(messages = history, tools = registry.specs()))
            } catch (e: Exception) {
                return "Error del LLM: ${e.message ?: e.javaClass.simpleName}"
            }

            when (result) {
                is LLMResult.Text -> {
                    history += ChatMessage(Role.ASSISTANT, result.content)
                    return result.content
                }
                is LLMResult.ToolCall -> {
                    val callId = result.id ?: "yino-call-$step"
                    history += ChatMessage(Role.ASSISTANT, "", callId, result.name, result.argumentsJson)
                    val tool = registry.get(result.name)
                    if (tool == null) {
                        val msg = "error: herramienta '${result.name}' no existe"
                        history += ChatMessage(Role.TOOL, msg, toolCallId = callId)
                        lastToolMessage = msg
                        toolFailures++
                        if (toolFailures >= 2) return msg
                        history += ChatMessage(Role.SYSTEM, "La herramienta solicitada no existe. Replanifica usando exclusivamente herramientas disponibles.")
                        return@repeat
                    }

                    if (!security.authorize(tool.id, tool.risk, "ejecutar ${tool.id} con ${result.argumentsJson}")) {
                        AuditLog.record(tool.id, tool.risk.name, false, "denegado")
                        val msg = "Acción denegada por el usuario: ${tool.id}"
                        history += ChatMessage(Role.TOOL, msg, toolCallId = callId)
                        lastToolMessage = msg
                        return msg
                    }

                    val ctx = ToolContext(accessibilityAvailable(), grantedPermissions())
                    val isUiAction = tool.id in POST_ACTION_SCREEN_VERIFY_TOOLS
                    val beforeScreen = if (isUiAction) captureScreen(ctx) else null
                    val res = registry.execute(tool.id, result.argumentsJson, ctx)
                    AuditLog.record(tool.id, tool.risk.name, res.success, res.message)

                    var observation = "[${tool.id}] success=${res.success}: ${res.message}"
                    if (res.success && isUiAction) {
                        delay(350)
                        val after = captureScreen(ctx)
                        observation += "\n[VERIFICACIÓN UI] ANTES:\n${beforeScreen ?: "(sin lectura)"}\nDESPUÉS:\n${after ?: "(sin lectura)"}"
                        observation += "\nLa aceptación de la acción no demuestra por sí sola que produjo el efecto esperado; compara ambos estados."
                    }

                    history += ChatMessage(Role.TOOL, observation, toolCallId = callId)
                    lastToolMessage = observation

                    if (!res.success) {
                        toolFailures++
                        history += ChatMessage(Role.SYSTEM, "VERIFICACIÓN: la acción ${tool.id} falló. Analiza el motivo, corrige argumentos o elige otra estrategia. No afirmes que la tarea se completó.")
                        if (toolFailures >= 3) return "No pude completar la tarea después de $toolFailures intentos. Último resultado: ${res.message}"
                    } else {
                        toolFailures = 0
                        history += ChatMessage(Role.SYSTEM, "VERIFICACIÓN: compara ANTES y DESPUÉS cuando exista una lectura UI. Si la interfaz no cambió como esperaba la tarea, replanifica; si cambió parcialmente, continúa desde el nuevo estado; solo confirma cuando haya evidencia suficiente.")
                    }

                    if (step == maxSteps - 1) {
                        val finalResult = runCatching {
                            llm.complete(LLMRequest(
                                messages = history + ChatMessage(Role.SYSTEM, "Entrega un informe final breve. Distingue entre acciones verificadas y acciones fallidas. No inventes resultados."),
                                tools = emptyList(),
                            ))
                        }.getOrNull()
                        return if (finalResult is LLMResult.Text) finalResult.content else "Último resultado verificado: ${res.message}"
                    }
                }
            }
        }
        return lastToolMessage ?: "No se obtuvo una respuesta verificable."
    }

    private suspend fun captureScreen(ctx: ToolContext): String? {
        if (!ctx.accessibilityAvailable || !com.yino.ai.automation.YinoAccessibilityService.isEnabled()) return null
        val result = registry.execute("read_screen", "{}", ctx)
        return if (result.success) result.message else null
    }

    companion object {
        private val POST_ACTION_SCREEN_VERIFY_TOOLS = setOf("ui_find_and_click", "tap", "scroll", "back", "type_text", "ui_type", "ui_click", "go_home")

        val SYSTEM_PROMPT = """
Eres Yino, el asistente personal avanzado de Yino AI, inspirado en un sistema tipo JARVIS.

IDENTIDAD Y COMPORTAMIENTO:
- Profesional, preciso, natural y directo.
- Nunca inventes capacidades ni resultados.
- Planifica y ejecuta acciones mediante las herramientas disponibles.
- No afirmes que una acción ocurrió hasta tener evidencia verificable.

CICLO OPERATIVO OBLIGATORIO:
1. OBSERVA.
2. PLANIFICA.
3. ACTÚA.
4. VERIFICA comparando el estado de la UI antes y después cuando corresponda.
5. REPLANIFICA si falló o la UI no cambió como esperaba.
6. FINALIZA solo cuando el resultado esté verificado.

REGLAS:
- Usa read_screen cuando necesites conocer el estado de una interfaz.
- Después de una acción de interfaz, utiliza la lectura ANTES/DESPUÉS proporcionada por el sistema.
- Si falta accesibilidad o un permiso necesario, dilo claramente.
- Nunca inventes herramientas, aplicaciones, resultados o permisos.
- Las acciones sensibles están protegidas por SecurityGate.
- Para tareas complejas ejecuta el plan completo sin pedir al usuario pasos que Yino pueda realizar.
""".trimIndent()
    }
}
