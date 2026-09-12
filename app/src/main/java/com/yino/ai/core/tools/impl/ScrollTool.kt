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
        if (!x.isFinite() || !y1.isFinite() || !y2.isFinite() || x < 0f || y1 < 0f || y2 < 0f) {
            return ToolResult(false, "Coordenadas inválidas para scroll")
        }
        val accepted = svc.swipe(x, y1, x, y2)
        return if (accepted) {
            ToolResult(true, "Scroll encolado de $y1 a $y2. Debe verificarse el estado posterior de la UI.")
        } else {
            ToolResult(false, "No se pudo encolar el scroll")
        }
    }
}
