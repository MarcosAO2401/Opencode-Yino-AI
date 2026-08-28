package com.yino.ai.core.security

import java.io.File
import java.time.Instant

/**
 * Registro inmutable de todas las acciones ejecutadas por el agente.
 * Persistencia en archivo (append-only) + espejo en memoria, para poder
 * auditar lo ocurrido incluso si la app se reinicia.
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
    private lateinit var file: File

    fun init(context: android.content.Context) {
        file = File(context.filesDir, "yino_audit.log")
    }

    @Synchronized
    fun record(toolId: String, risk: String, approved: Boolean, result: String) {
        val entry = Entry(Instant.now(), toolId, risk, approved, result)
        entries += entry
        if (::file.isInitialized) {
            runCatching {
                file.appendText("${entry.ts}\t$toolId\t$risk\t$approved\t${result.replace("\n", " ")}\n")
            }
        }
    }

    @Synchronized
    fun all(): List<Entry> = entries.toList()
}
