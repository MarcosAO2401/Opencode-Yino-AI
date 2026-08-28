package com.yino.ai.core.tools.impl

import com.yino.ai.automation.YinoNotificationListener
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

class ReadNotificationsTool : Tool {
    override val id = "read_notifications"
    override val description = "Lee las notificaciones recientes entrantes (requiere permiso de listener)"
    override val parametersJsonSchema = """{"type":"object","properties":{}}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val listener = YinoNotificationListener.instance()
            ?: return ToolResult(false, "Notification listener no habilitado")
        val recent = listener.recent()
        if (recent.isEmpty()) return ToolResult(true, "(sin notificaciones recientes)")
        val text = recent.joinToString("\n") { "[${it.packageName}] ${it.title}: ${it.text}" }
        return ToolResult(true, text)
    }
}
