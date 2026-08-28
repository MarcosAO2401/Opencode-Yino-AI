package com.yino.ai.core.tools.impl

import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

class ScrollTool : Tool {
    override val id = "scroll"
    override val description = "Desplaza la pantalla (scroll). Por defecto scroll vertical; coords opcionales en JSON"
    override val parametersJsonSchema =
        """{"type":"object","properties":{"x":{"type":"number"},"y1":{"type":"number"},"y2":{"type":"number"}}}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val svc = YinoAccessibilityService.instance()
            ?: return ToolResult(false, "Accesibilidad no disponible")
        val x = arguments.optDouble("x", 500.0).toFloat()
        val y1 = arguments.optDouble("y1", 1500.0).toFloat()
        val y2 = arguments.optDouble("y2", 500.0).toFloat()
        svc.swipe(x, y1, x, y2)
        return ToolResult(true, "Scroll ejecutado de $y1 a $y2")
    }
}
