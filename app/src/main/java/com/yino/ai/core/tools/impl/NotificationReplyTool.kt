package com.yino.ai.core.tools.impl

import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.yino.ai.automation.YinoNotificationListener
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

/**
 * Responde a la notificación más reciente de una app usando su acción de
 * respuesta integrada (RemoteInput). Es el método fiable para contestar DMs de
 * Instagram / Facebook Messenger sin abrir la app ni hacer scraping de UI.
 * Requiere que haya una notificación reciente de esa app en la barra.
 */
class NotificationReplyTool(private val context: Context) : Tool {
    override val id = "reply_notification"
    override val description =
        "Responde a la notificación más reciente de una app (p. ej. instagram, messenger, facebook) " +
            "usando su acción de respuesta integrada. Úsalo para contestar DMs sin abrir la app. " +
            "Parámetros: app y message. Requiere una notificación reciente de esa app."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"app":{"type":"string"},"message":{"type":"string"}},"required":["app","message"]}"""
    override val risk = ActionRisk.HIGH
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val app = arguments.optString("app")
        val msg = arguments.optString("message")
        if (msg.isBlank()) return ToolResult(false, "message requerido")
        val pkg = resolvePackage(app)
        if (pkg == null) return ToolResult(false, "app '$app' no reconocida")

        val target = YinoNotificationListener.instance()?.getReply(pkg)
            ?: return ToolResult(
                false,
                "No hay notificación con acción de respuesta de $app. " +
                    "Debe haber un DM reciente en la barra de notificaciones.",
            )
        return try {
            val intent = Intent()
            val bundle = Bundle()
            bundle.putCharSequence(target.remoteInput.resultKey, msg)
            RemoteInput.addResultsToIntent(arrayOf(target.remoteInput), intent, bundle)
            target.pendingIntent.send(context, 0, intent)
            ToolResult(true, "Respondido en $app: $msg")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error al responder")
        }
    }

    private fun resolvePackage(raw: String): String? {
        val r = raw.trim()
        if (r.contains(".")) return r
        val aliases = mapOf(
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "messenger" to "com.facebook.orca",
            "whatsapp" to "com.whatsapp",
            "telegram" to "org.telegram.messenger",
        )
        return aliases[r.lowercase()] ?: r
    }
}
