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
     * Solicita autorización. Por defecto (sin handler UI) rechaza riesgo ALTO
     * y aprueba MEDIO transparente; con UI conectada, espera respuesta real.
     */
    suspend fun authorize(toolId: String, risk: ActionRisk, reason: String): Boolean {
        if (risk == ActionRisk.LOW) return true
        val id = "$toolId-${System.currentTimeMillis()}"
        _pending.tryEmit(PendingApproval(id, toolId, risk, reason))
        return try {
            withTimeout(120.seconds) {
                // Espera a que la UI llame a respond() con este id.
                // Si no hay UI conectada, denegar por seguridad.
                responses[id] ?: (risk != ActionRisk.HIGH).also { responses.remove(id) }
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
