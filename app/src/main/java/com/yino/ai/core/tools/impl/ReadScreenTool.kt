package com.yino.ai.core.tools.impl

import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import com.yino.ai.core.vision.ScreenUnderstandingEngine
import org.json.JSONObject

class ReadScreenTool : Tool {
    override val id = "read_screen"
    override val description = "Lee el contenido visible de la pantalla actual para entender el estado de la UI"
    override val parametersJsonSchema = """{"type":"object","properties":{}}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        if (!YinoAccessibilityService.isEnabled()) {
            return ToolResult(false, "Accesibilidad no disponible")
        }
        val root = YinoAccessibilityService.instance()?.root() ?: return ToolResult(false, "Sin root")
        val summary = ScreenUnderstandingEngine.summarize(root)
        return ToolResult(true, summary)
    }
}
