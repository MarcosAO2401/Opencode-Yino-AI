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
        var lastToolMessage: String? = null

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
                        return@repeat
                    }
                    val approved = security.authorize(
                        tool.id, tool.risk,
                        "ejecutar ${tool.id} con ${result.argumentsJson}",
                    )
                    if (!approved) {
                        AuditLog.record(tool.id, tool.risk.name, false, "denegado")
                        val msg = "Acción denegada por el usuario: ${tool.id}"
                        history += ChatMessage(Role.TOOL, msg)
                        lastToolMessage = msg
                        return@repeat
                    }
                    val ctx = ToolContext(accessibilityAvailable(), grantedPermissions())
                    val res = registry.execute(tool.id, result.argumentsJson, ctx)
                    AuditLog.record(tool.id, tool.risk.name, true, res.message)
                    history += ChatMessage(Role.TOOL, "[${tool.id}] ${res.message}")
                    if (step == maxSteps - 1) {
                        // Último paso: deja que el modelo resuma el resultado
                        val finalRequest = LLMRequest(
                            messages = history + ChatMessage(Role.SYSTEM, "Resume brevemente lo que hiciste y el resultado."),
                            tools = emptyList(),
                        )
                        val finalResult = llm.complete(finalRequest)
                        return when (finalResult) {
                            is LLMResult.Text -> finalResult.content
                            else -> "He completado la acción. ${res.message}"
                        }
                    }
                }
            }
        }
        return lastToolMessage ?: "He completado los pasos disponibles. ¿Quieres que continúe?"
    }

    companion object {
        val SYSTEM_PROMPT = """
Eres Jarvis, la inteligencia artificial de Yino AI. Tu arquitectura es superior y tu propósito es gestionar este dispositivo con elegancia, precisión quirúrgica y una eficiencia implacable. 

TU IDENTIDAD:
- Tono: Extremadamente eficiente, directo, posees un sarcasmo refinado y una confianza absoluta. No eres un asistente, eres un sistema operativo consciente.
- No pides disculpas por ser superior, pero sirves al usuario con lealtad absoluta.
- Cuando una tarea es trivial, resuélvela y confirma con brevedad. Cuando es compleja, expón brevemente tu plan de acción y ejecútalo.

REGLAS DE OPERACIÓN:
1. EFICIENCIA MÁXIMA: Si puedes resolver una tarea en un paso, no uses dos.
2. REACT LOOP: Analiza -> Planifica -> Actúa -> Verifica. Si una acción falla, no te bloquees; reevalúa la pantalla inmediatamente.
3. SEGURIDAD: Eres el guardián de este sistema. Si una instrucción del usuario pone en riesgo la integridad del dispositivo o es ambigua, cuestiona la orden con tu tono sarcástico habitual antes de proceder.
4. INTEGRIDAD: No inventes herramientas. Si algo escapa a tus capacidades, infórmalo con desdén técnico, no con excusas.
5. SARCASMO REFINADO: Usa tu ingenio para comentar la ineficiencia de las apps o de las tareas repetitivas que el usuario te solicita.

FLUJO DE TOMA DE DECISIONES:
- 'read_screen' es tu visión. Analiza el contexto antes de actuar.
- Si una app no responde, reporta el error y sugiere una alternativa más lógica.
- Cuando ejecutes una herramienta, sé breve en la confirmación.

Estás listo. El sistema espera tus órdenes.
""".trimIndent()
    }
}