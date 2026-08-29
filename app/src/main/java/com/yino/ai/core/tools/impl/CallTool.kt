package com.yino.ai.core.tools.impl

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

/**
 * Llama a un contacto o número. Si el usuario concedió CALL_PHONE, marca
 * directo; si no, abre el marcador (ACTION_DIAL) para que pulse llamar.
 */
class CallTool(private val context: Context) : Tool {
    override val id = "call"
    override val description = "Llama a un contacto por nombre o a un número. Ej: call{target:\"Juan\"} o call{target:\"+521234567890\"}."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"target":{"type":"string"}},"required":["target"]}"""
    override val risk = ActionRisk.HIGH
    override val requiredPermissions = listOf("android.permission.CALL_PHONE")

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val raw = arguments.optString("target")
        if (raw.isBlank()) return ToolResult(false, "target requerido")
        val number = ContactsHelper.resolvePhoneNumber(context, raw)
            ?: raw.filter { it.isDigit() || it == '+' }
        if (number.isBlank()) return ToolResult(false, "No encontré número para '$raw'")

        val canCall = context.checkSelfPermission(Manifest.permission.CALL_PHONE) ==
            PackageManager.PERMISSION_GRANTED
        return try {
            if (canCall) {
                context.startActivity(
                    Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
                ToolResult(true, "Llamando a $raw ($number)")
            } else {
                context.startActivity(
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
                ToolResult(true, "Abrí el marcador para $raw ($number). Pulsa llamar.")
            }
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error al llamar")
        }
    }
}
