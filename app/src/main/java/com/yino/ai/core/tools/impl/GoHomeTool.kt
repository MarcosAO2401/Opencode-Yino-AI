package com.yino.ai.core.tools.impl

import android.accessibilityservice.AccessibilityService
import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

class GoHomeTool : Tool {
    override val id = "go_home"
    override val description = "Vuelve a la pantalla de inicio (HOME)"
    override val parametersJsonSchema = """{"type":"object","properties":{}}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        return try {
            YinoAccessibilityService.instance()?.global(AccessibilityService.GLOBAL_ACTION_HOME)
            ToolResult(true, "Home")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}
