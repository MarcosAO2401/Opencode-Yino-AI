package com.yino.ai.core.tools.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

class SendMessageTool(private val context: Context) : Tool {
    override val id = "send_message"
    override val description = "Envía un mensaje a un contacto de una app de mensajería por paquete"
    override val parametersJsonSchema =
        """{"type":"object","properties":{"packageName":{"type":"string"},"contact":{"type":"string"},"message":{"type":"string"}},"required":["packageName","contact","message"]}"""
    override val risk = ActionRisk.HIGH
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val pkg = arguments.optString("packageName")
        val contact = arguments.optString("contact")
        val msg = arguments.optString("message")
        if (msg.isBlank()) return ToolResult(false, "message requerido")
        return try {
            if (pkg.contains("whatsapp", ignoreCase = true) && contact.isNotBlank()) {
                val phone = contact.filter { it.isDigit() }
                if (phone.isNotEmpty()) {
                    val uri = Uri.parse("https://wa.me/$phone?text=${Uri.encode(msg)}")
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                    return ToolResult(true, "Abriendo chat de WhatsApp con $contact (revisa y pulsa enviar)")
                }
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, msg)
                if (contact.isNotBlank()) putExtra("address", contact)
                if (pkg.isNotBlank()) setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(true, "Mensaje listo para enviar vía $pkg")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}
