package com.yino.ai.core.tools.impl

import android.content.Context
import android.content.Intent
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
    override val requiredPermissions = listOf("android.permission.SEND_SMS")

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val pkg = arguments.optString("packageName")
        val contact = arguments.optString("contact")
        val msg = arguments.optString("message")
        if (pkg.isBlank() || msg.isBlank()) {
            return ToolResult(false, "packageName y message requeridos")
        }
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, msg)
                if (contact.isNotBlank()) putExtra("address", contact)
                setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(true, "Mensaje enviado a $contact vía $pkg")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}
