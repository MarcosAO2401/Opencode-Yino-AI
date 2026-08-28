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
        val pkg = arguments.optString("packageName")
        if (pkg.isBlank()) return ToolResult(false, "packageName requerido")
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(pkg)
                ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult(true, "Abriendo $pkg")
        } catch (e: Exception) {
            ToolResult(false, "No se pudo abrir $pkg: ${e.message}")
        }
    }
}
