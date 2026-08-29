package com.yino.ai.core.tools.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

/**
 * Envía un mensaje a través de una app de mensajería. Soporta:
 *  - whatsapp : deep link wa.me con texto + auto-pulsar "Enviar" (accesibilidad)
 *  - telegram : deep link tg://msg con texto + auto-pulsar enviar
 *  - sms      : intent sms: con body + auto-pulsar enviar
 *  - instagram/facebook/tiktok/u otras: abre la app y delega en el agente
 *    (que puede usar ui_type / ui_click para terminar el envío).
 * Para apps donde no hay deep link de DM, se abre la app y se indica al
 * usuario/agente cómo completar.
 */
class SendMessageTool(private val context: Context) : Tool {
    override val id = "send_message"
    override val description =
        "Envía un mensaje a un contacto en una app de mensajería. " +
            "Parámetros: app (whatsapp|telegram|sms|instagram|facebook|tiktok|...), " +
            "contact (teléfono o usuario) y message (texto). WhatsApp/Telegram/SMS se envían solos; " +
            "otras apps se abren para completar con ui_type/ui_click."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"app":{"type":"string"},"contact":{"type":"string"},"message":{"type":"string"}},"required":["app","contact","message"]}"""
    override val risk = ActionRisk.HIGH
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val app = arguments.optString("app").ifBlank { "whatsapp" }
        val contact = arguments.optString("contact")
        val msg = arguments.optString("message")
        if (msg.isBlank()) return ToolResult(false, "message requerido")
        if (contact.isBlank()) return ToolResult(false, "contact requerido")

        return try {
            when (app.lowercase()) {
                "whatsapp" -> sendWhatsApp(contact, msg)
                "telegram" -> sendTelegram(contact, msg)
                "sms" -> sendSms(contact, msg)
                else -> openAndDelegate(app, contact, msg)
            }
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error enviando")
        }
    }

    private fun sendWhatsApp(contact: String, msg: String): ToolResult {
        val phone = contact.filter { it.isDigit() }
        if (phone.isEmpty()) return ToolResult(false, "WhatsApp requiere un número de teléfono en contact")
        val uri = Uri.parse("https://wa.me/$phone?text=${Uri.encode(msg)}")
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        SystemClock.sleep(1800)
        val svc = YinoAccessibilityService.instance()
        val sent = svc != null && (
            svc.findAndClick("Enviar") ||
            svc.findAndClick("Send")
        )
        return if (sent) {
            ToolResult(true, "Mensaje de WhatsApp enviado a $contact")
        } else {
            ToolResult(true, "Abrí el chat de WhatsApp con $contact. Revisa y pulsa enviar.")
        }
    }

    private fun sendTelegram(contact: String, msg: String): ToolResult {
        val handle = contact.trim().removePrefix("@")
        val target = if (handle.any { it.isDigit() }) handle else "@$handle"
        val uri = Uri.parse("tg://msg?to=$target&text=${Uri.encode(msg)}")
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        SystemClock.sleep(1800)
        val svc = YinoAccessibilityService.instance()
        val sent = svc != null && (
            svc.findAndClick("Enviar") ||
            svc.findAndClick("Send") ||
            svc.findAndClick("message")
        )
        return if (sent) {
            ToolResult(true, "Mensaje de Telegram enviado a $contact")
        } else {
            ToolResult(true, "Abrí Telegram con $contact. Revisa y pulsa enviar.")
        }
    }

    private fun sendSms(contact: String, msg: String): ToolResult {
        val phone = contact.filter { it.isDigit() }
        if (phone.isEmpty()) return ToolResult(false, "SMS requiere un número en contact")
        val uri = Uri.parse("sms:$phone?body=${Uri.encode(msg)}")
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        SystemClock.sleep(1200)
        val svc = YinoAccessibilityService.instance()
        val sent = svc != null && (
            svc.findAndClick("Enviar") ||
            svc.findAndClick("Send")
        )
        return if (sent) {
            ToolResult(true, "SMS enviado a $contact")
        } else {
            ToolResult(true, "Abrí la app de SMS con el mensaje. Revisa y pulsa enviar.")
        }
    }

    private fun openAndDelegate(app: String, contact: String, msg: String): ToolResult {
        val pkg = resolvePackage(app)
        pkg?.let {
            val intent = context.packageManager.getLaunchIntentForPackage(it)
                ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent != null) {
                context.startActivity(intent)
                return ToolResult(
                    true,
                    "Abrí $app. Para terminar: usa ui_type para escribir a '$contact' y ui_click en su chat, " +
                        "luego ui_type con \"$msg\" y ui_click en Enviar.",
                )
            }
        }
        return ToolResult(false, "No encontré la app '$app' instalada")
    }

    private fun resolvePackage(raw: String): String? {
        val r = raw.trim()
        if (r.contains(".")) return r
        val lower = r.lowercase()
        val aliases = mapOf(
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "messenger" to "com.facebook.orca",
            "whatsapp" to "com.whatsapp",
            "telegram" to "org.telegram.messenger",
            "tiktok" to "com.zhiliaoapp.musically",
            "youtube" to "com.google.android.youtube",
            "x" to "com.twitter.android",
            "twitter" to "com.twitter.android",
        )
        return aliases[lower] ?: try {
            context.packageManager.getInstalledApplications(0)
                .firstOrNull {
                    context.packageManager.getApplicationLabel(it)
                        .toString().equals(r, ignoreCase = true)
                }?.packageName
        } catch (e: Exception) {
            null
        }
    }
}
