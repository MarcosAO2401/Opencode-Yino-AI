package com.yino.ai.core.tools.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

/**
 * Envía un correo real vía la app de email del sistema (mailto:).
 */
class SendEmailTool(private val context: Context) : Tool {
    override val id = "send_email"
    override val description = "Envía un correo. Parámetros: to, subject, body."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"to":{"type":"string"},"subject":{"type":"string"},"body":{"type":"string"}},"required":["to"]}"""
    override val risk = ActionRisk.HIGH
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val to = arguments.optString("to")
        if (to.isBlank()) return ToolResult(false, "to requerido")
        val subject = arguments.optString("subject", "")
        val body = arguments.optString("body", "")
        return try {
            val uri = Uri.parse("mailto:$to?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
            context.startActivity(
                Intent(Intent.ACTION_SENDTO, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            ToolResult(true, "Abriendo correo para $to")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error al enviar correo")
        }
    }
}
