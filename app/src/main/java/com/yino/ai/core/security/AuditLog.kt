package com.yino.ai.core.security

import java.time.Instant

/**
 * Registro inmutable de todas las acciones ejecutadas por el agente.
 * Persistencia en memoria; en producción se vuelca a Room (ver data/).
 */
object AuditLog {

    data class Entry(
        val ts: Instant,
        val toolId: String,
        val risk: String,
        val approved: Boolean,
        val result: String,
    )

    private val entries = mutableListOf<Entry>()

    @Synchronized
    fun record(toolId: String, risk: String, approved: Boolean, result: String) {
        entries += Entry(Instant.now(), toolId, risk, approved, result)
    }

    @Synchronized
    fun all(): List<Entry> = entries.toList()
}
