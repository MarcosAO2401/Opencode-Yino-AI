package com.yino.ai.core.security

import com.yino.ai.core.tools.ActionRisk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * Puerta de seguridad del agente. Toda herramienta de riesgo MEDIO/ALTO
 * debe ser autorizada por el usuario antes de ejecutarse. La UI observa
 * [pendingApprovals] y responde con [respond].
 */
class SecurityGate {

    data class PendingApproval(
        val requestId: String,
        val toolId: String,
        val risk: ActionRisk,
        val reason: String,
    )

    private val _pending = MutableSharedFlow<PendingApproval>(extraBufferCapacity = 16)
    val pendingApprovals = _pending

    private val responses = LinkedHashMap<String, Boolean>()

    fun requiresConfirmation(risk: ActionRisk): Boolean = risk != ActionRisk.LOW

    /**
     * Solicita autorizacion. FAIL-CLOSED: si no hay respuesta explicita del
     * usuario (UI o confirmacion por voz), se deniega. Esto evita que el
     * asistente en segundo plano (servicio de voz sin UI) ejecute acciones de
     * riesgo MEDIO/ALTO solo por el wake-word. El Chat tiene un dialogo que
     * llama a respond(id, true) y aprueba explicitamente.
     */
    suspend fun authorize(toolId: String, risk: ActionRisk, reason: String): Boolean {
        if (risk == ActionRisk.LOW) return true
        val id = "$toolId-${System.currentTimeMillis()}"
        _pending.tryEmit(PendingApproval(id, toolId, risk, reason))
        return try {
            withTimeout(120.seconds) {
                responses[id] ?: false
            }
        } catch (e: Exception) {
            responses.remove(id)
            false
        }
    }

    fun respond(requestId: String, approved: Boolean) {
        responses[requestId] = approved
    }
}
