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

/**
 * Núcleo del agente Yino: Observation -> Plan -> Action -> Verification.
 * Conserva el identificador del tool call para que proveedores OpenAI-compatible
 * puedan recibir la secuencia assistant(tool_call) -> tool(result) correctamente.
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
        var lastToolMessage: String? = null
        var toolFailures = 0

        repeat(maxSteps) { step ->
            val request = LLMRequest(messages = history, tools = registry.specs())
            val result = try {
                llm.complete(request)
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
                    history += ChatMessage(
                        role = Role.ASSISTANT,
                        content = "",
                        toolCallId = callId,
                        toolCallName = result.name,
                        toolCallArguments = result.argumentsJson,
                    )

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

                    val approved = security.authorize(
                        tool.id,
                        tool.risk,
                        "ejecutar ${tool.id} con ${result.argumentsJson}",
                    )
                    if (!approved) {
                        AuditLog.record(tool.id, tool.risk.name, false, "denegado")
                        val msg = "Acción denegada por el usuario: ${tool.id}"
                        history += ChatMessage(Role.TOOL, msg, toolCallId = callId)
                        lastToolMessage = msg
                        return msg
                    }

                    val ctx = ToolContext(accessibilityAvailable(), grantedPermissions())
                    val res = registry.execute(tool.id, result.argumentsJson, ctx)
                    AuditLog.record(tool.id, tool.risk.name, res.success, res.message)
                    val observation = "[${tool.id}] success=${res.success}: ${res.message}"
                    history += ChatMessage(Role.TOOL, observation, toolCallId = callId)
                    lastToolMessage = observation

                    if (!res.success) {
                        toolFailures++
                        history += ChatMessage(
                            Role.SYSTEM,
                            "VERIFICACIÓN: la acción ${tool.id} falló. Analiza el motivo, corrige argumentos o elige otra estrategia. No afirmes que la tarea se completó.",
                        )
                        if (toolFailures >= 3) {
                            return "No pude completar la tarea después de $toolFailures intentos. Último resultado: ${res.message}"
                        }
                    } else {
                        toolFailures = 0

                        // Las acciones de UI se aceptan de forma síncrona en la herramienta,
                        // pero su efecto visual ocurre de forma asíncrona en Android. Para que
                        // Yino no confunda "acción aceptada" con "acción completada", releemos
                        // automáticamente la pantalla antes de pedir al LLM que evalúe el resultado.
                        if (tool.id in POST_ACTION_SCREEN_VERIFY_TOOLS) {
                            delay(300)
                            val verifyCtx = ToolContext(accessibilityAvailable(), grantedPermissions())
                            val verify = registry.execute("read_screen", "{}", verifyCtx)
                            val verification = if (verify.success) {
                                "[post_action_read_screen] success=true: ${verify.message}"
                            } else {
                                "[post_action_read_screen] success=false: ${verify.message}"
                            }
                            history += ChatMessage(Role.TOOL, verification, toolCallId = callId)
                            lastToolMessage = verification
                        }

                        history += ChatMessage(
                            Role.SYSTEM,
                            "VERIFICACIÓN: observa el resultado de ${tool.id} y, si existe, la lectura de pantalla posterior. Determina si satisface realmente la solicitud del usuario. Si no, ejecuta otra acción; si sí, responde confirmando únicamente lo que está verificado.",
                        )
                    }

                    if (step == maxSteps - 1) {
                        val finalRequest = LLMRequest(
                            messages = history + ChatMessage(Role.SYSTEM, "Entrega un informe final breve. Distingue entre acciones verificadas y acciones que fallaron. No inventes resultados."),
                            tools = emptyList(),
                        )
                        val finalResult = runCatching { llm.complete(finalRequest) }.getOrNull()
                        return when (finalResult) {
                            is LLMResult.Text -> finalResult.content
                            else -> "Último resultado verificado: ${res.message}"
                        }
                    }
                }
            }
        }

        return lastToolMessage ?: "No se obtuvo una respuesta verificable."
    }

    companion object {
        private val POST_ACTION_SCREEN_VERIFY_TOOLS = setOf(
            "ui_find_and_click",
            "tap",
            "scroll",
            "back",
            "type_text",
        )

        val SYSTEM_PROMPT = """
Eres Yino, el asistente personal avanzado de Yino AI, inspirado en un sistema tipo JARVIS.

IDENTIDAD Y COMPORTAMIENTO:
- Profesional, preciso, natural y directo.
- Hablas con seguridad, pero nunca inventas capacidades ni resultados.
- Puedes planificar y ejecutar acciones mediante herramientas disponibles.
- No afirmes que una acción ocurrió hasta que exista un resultado verificable.

CICLO OPERATIVO OBLIGATORIO:
1. OBSERVA: interpreta la solicitud y el contexto disponible.
2. PLANIFICA: divide tareas complejas en pasos mínimos necesarios.
3. ACTÚA: usa las herramientas apropiadas.
4. VERIFICA: analiza el resultado de cada herramienta y la lectura de pantalla posterior cuando exista.
5. REPLANIFICA: si falló o la pantalla no cambió como esperaba, corrige argumentos o utiliza otra herramienta.
6. FINALIZA: solo confirma lo que realmente esté verificado.

REGLAS:
- Usa read_screen antes de actuar sobre una interfaz cuando necesites conocer su estado.
- Después de una acción de interfaz, utiliza la lectura posterior proporcionada por el sistema para comprobar el nuevo estado antes de afirmar que funcionó.
- Si una acción requiere accesibilidad o permisos y no están disponibles, dilo claramente.
- Nunca inventes una herramienta, aplicación, resultado, permiso o dato.
- Las acciones sensibles están protegidas por SecurityGate y requieren autorización cuando corresponda.
- Evita pasos innecesarios.
- Para tareas simples, responde de forma breve.
- Para tareas complejas, ejecuta el plan completo sin pedir al usuario que haga manualmente lo que Yino pueda hacer mediante sus herramientas.
""".trimIndent()
    }
}
