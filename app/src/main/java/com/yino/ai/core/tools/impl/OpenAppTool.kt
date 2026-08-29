package com.yino.ai.core.tools.impl

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

class OpenAppTool(private val context: Context) : Tool {
    override val id = "open_app"
    override val description = "Abre una aplicación por nombre de paquete, ej. com.whatsapp"
    override val parametersJsonSchema =
        """{"type":"object","properties":{"packageName":{"type":"string"}},"required":["packageName"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val raw = arguments.optString("packageName")
        if (raw.isBlank()) return ToolResult(false, "packageName requerido")
        val pkg = resolvePackage(context, raw)
            ?: return ToolResult(false, "No encontré la app \"$raw\"")
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(pkg)
                ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult(true, "Abriendo $raw")
        } catch (e: Exception) {
            ToolResult(false, "No se pudo abrir $raw: ${e.message}")
        }
    }

    private fun resolvePackage(context: Context, raw: String): String? {
        val r = raw.trim()
        if (r.contains(".")) return r
        val lower = r.lowercase()
        val aliases = mapOf(
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "messenger" to "com.facebook.orca",
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "tiktok" to "com.zhiliaoapp.musically",
            "twitter" to "com.twitter.android",
            "x" to "com.twitter.android",
            "telegram" to "org.telegram.messenger",
            "gmail" to "com.google.android.gm",
            "chrome" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "spotify" to "com.spotify.music",
            "netflix" to "com.netflix.mediaclient",
            "amazon" to "com.amazon.mShop.android.shopping",
            "camera" to "com.android.camera2",
            "fotos" to "com.google.android.apps.photos",
        )
        aliases[lower]?.let { return it }
        return try {
            val pm = context.packageManager
            pm.getInstalledApplications(0)
                .firstOrNull {
                    val label = pm.getApplicationLabel(it).toString()
                    label.equals(r, ignoreCase = true) || label.contains(r, ignoreCase = true)
                }
                ?.packageName
        } catch (e: Exception) {
            null
        }
    }
}
