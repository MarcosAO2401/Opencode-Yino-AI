package com.yino.ai.core.tools.impl

import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

class TapTool : Tool {
    override val id = "tap"
    override val description = "Toca una coordenada de pantalla (x,y en píxeles)"
    override val parametersJsonSchema =
        """{"type":"object","properties":{"x":{"type":"number"},"y":{"type":"number"}},"required":["x","y"]}"""
    override val risk = ActionRisk.MEDIUM
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val x = arguments.optDouble("x").toFloat()
        val y = arguments.optDouble("y").toFloat()
        return try {
            YinoAccessibilityService.instance()?.tap(x, y)
            ToolResult(true, "Tap en ($x,$y)")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}
