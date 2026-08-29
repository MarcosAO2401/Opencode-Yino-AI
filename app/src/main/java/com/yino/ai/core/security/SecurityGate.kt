package com.yino.ai.core.security

import com.yino.ai.core.tools.ActionRisk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
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

    private val deferreds = LinkedHashMap<String, CompletableDeferred<Boolean>>()

    /**
     * Si es false, las acciones MEDIO/ALTO se niegan de inmediato (fail-closed)
     * sin esperar UI. Lo usa el servicio de voz en segundo plano, que no tiene
     * interfaz para pedir confirmación.
     */
    var interactive = true

    fun requiresConfirmation(risk: ActionRisk): Boolean = risk != ActionRisk.LOW

    /**
     * Solicita autorizacion. FAIL-CLOSED: si no hay respuesta explicita del
     * usuario (UI o confirmacion por voz), se deniega tras 120s.
     */
    suspend fun authorize(toolId: String, risk: ActionRisk, reason: String): Boolean {
        if (risk == ActionRisk.LOW) return true
        if (!interactive) return false
        val id = "$toolId-${System.currentTimeMillis()}-${deferreds.size}"
        val deferred = CompletableDeferred<Boolean>()
        synchronized(deferreds) { deferreds[id] = deferred }
        _pending.tryEmit(PendingApproval(id, toolId, risk, reason))
        return try {
            withTimeout(120.seconds) { deferred.await() }
        } catch (e: Exception) {
            synchronized(deferreds) { deferreds.remove(id) }
            false
        }
    }

    fun respond(requestId: String, approved: Boolean) {
        val d = synchronized(deferreds) { deferreds.remove(requestId) }
        d?.complete(approved)
    }
}
