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
 * Núcleo del agente Yino: Observation -> Plan -> Action -> Verification.
 * Cada resultado de herramienta vuelve al contexto del LLM para que pueda
 * comprobar el resultado, detectar fallos y replantear la siguiente acción.
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
                    val tool = registry.get(result.name)
                    if (tool == null) {
                        val msg = "error: herramienta '${result.name}' no existe"
                        history += ChatMessage(Role.TOOL, msg)
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
                        history += ChatMessage(Role.TOOL, msg)
                        lastToolMessage = msg
                        return msg
                    }

                    val ctx = ToolContext(accessibilityAvailable(), grantedPermissions())
                    val res = registry.execute(tool.id, result.argumentsJson, ctx)
                    AuditLog.record(tool.id, tool.risk.name, res.success, res.message)
                    val observation = "[${tool.id}] success=${res.success}: ${res.message}"
                    history += ChatMessage(Role.TOOL, observation)
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
                        history += ChatMessage(
                            Role.SYSTEM,
                            "VERIFICACIÓN: observa el resultado de ${tool.id}. Determina si satisface realmente la solicitud del usuario. Si no, ejecuta otra acción; si sí, responde confirmando únicamente lo que está verificado.",
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
4. VERIFICA: analiza el resultado de cada herramienta.
5. REPLANIFICA: si falló, corrige argumentos o utiliza otra herramienta.
6. FINALIZA: solo confirma lo que realmente esté verificado.

REGLAS:
- Usa read_screen antes de actuar sobre una interfaz cuando necesites conocer su estado.
- Si una acción requiere accesibilidad o permisos y no están disponibles, dilo claramente.
- Nunca inventes una herramienta, aplicación, resultado, permiso o dato.
- Las acciones sensibles están protegidas por SecurityGate y requieren autorización cuando corresponda.
- Evita pasos innecesarios.
- Para tareas simples, responde de forma breve.
- Para tareas complejas, ejecuta el plan completo sin pedir al usuario que haga manualmente lo que Yino pueda hacer mediante sus herramientas.
""".trimIndent()
    }
}
