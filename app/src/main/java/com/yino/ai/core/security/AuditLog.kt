package com.yino.ai.core.security

import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Registro inmutable de todas las acciones ejecutadas por el agente.
 * Persistencia en archivo (append-only) + espejo en memoria, para poder
 * auditar lo ocurrido incluso si la app se reinicia.
 * Rotación automática para evitar crecimiento ilimitado.
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
    private const val MAX_ENTRIES = 10_000
    private const val ROTATION_THRESHOLD = 8_000

    fun init(context: android.content.Context) {
        file = File(context.filesDir, "yino_audit.log")
        rotateIfNeeded()
    }

    suspend fun record(toolId: String, risk: String, approved: Boolean, result: String) {
        val entry = Entry(Instant.now(), toolId, risk, approved, result)
        entries += entry
        if (::file.isInitialized) {
            withContext(Dispatchers.IO) {
                runCatching {
                    file.appendText("${entry.ts}\t$toolId\t$risk\t$approved\t${result.replace("\n", " ")}\n")
                }
                rotateIfNeeded()
            }
        }
        // Keep in-memory list bounded
        if (entries.size > MAX_ENTRIES) {
            val removeCount = entries.size - MAX_ENTRIES
            entries.subList(0, removeCount).clear()
        }
    }

    private fun rotateIfNeeded() {
        if (file.exists() && file.length() > 5_000_000) { // 5MB
            try {
                val lines = file.readLines()
                if (lines.size > ROTATION_THRESHOLD) {
                    val keep = lines.takeLast(ROTATION_THRESHOLD)
                    file.writeText(keep.joinToString("\n") + "\n")
                }
            } catch (e: Exception) {
                // Ignore rotation errors
            }
        }
    }

    @Synchronized
    fun all(): List<Entry> = entries.toList()
}