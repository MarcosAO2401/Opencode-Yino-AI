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
        if (!ctx.accessibilityAvailable || !YinoAccessibilityService.isEnabled()) {
            return ToolResult(false, "No se puede tocar la pantalla: Accesibilidad no está disponible.")
        }
        val x = arguments.optDouble("x")
        val y = arguments.optDouble("y")
        if (!x.isFinite() || !y.isFinite() || x < 0.0 || y < 0.0) {
            return ToolResult(false, "Coordenadas inválidas: x e y deben ser números finitos no negativos.")
        }
        val accepted = YinoAccessibilityService.instance()?.tap(x.toFloat(), y.toFloat()) == true
        return if (accepted) {
            ToolResult(true, "Tap en ($x,$y) encolado para ejecución. Debe verificarse el estado posterior de la UI.")
        } else {
            ToolResult(false, "No se pudo encolar el tap en ($x,$y).")
        }
    }
}
