package com.yino.ai.core.tools.impl

import android.os.SystemClock
import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

class UiClickTool : Tool {
    override val id = "ui_click"
    override val description = "Busca un elemento visible por texto o descripción y hace clic. Úsalo para botones, chats, contactos o Enviar."
    override val parametersJsonSchema = """{"type":"object","properties":{"text":{"type":"string","minLength":1},"waitMs":{"type":"number","minimum":0}},"required":["text"]}"""
    override val risk = ActionRisk.MEDIUM
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val text = arguments.optString("text").trim()
        if (text.isBlank()) return ToolResult(false, "text requerido")
        val wait = arguments.optLong("waitMs", 0).coerceIn(0, 8000)
        if (wait > 0) SystemClock.sleep(wait)
        val svc = YinoAccessibilityService.instance() ?: return ToolResult(false, "Accesibilidad no disponible")
        val ok = svc.findAndClick(text)
        return ToolResult(ok, if (ok) "Clic aceptado en '$text'; requiere verificación posterior de UI" else "No encontré un elemento visible habilitado que coincida con '$text'")
    }
}

class UiTypeTool : Tool {
    override val id = "ui_type"
    override val description = "Escribe texto en un campo editable visible. Usa hint cuando se conozca el propósito del campo (por ejemplo 'buscar', 'mensaje', 'nombre'). El agente debe verificar después que el texto apareció."
    override val parametersJsonSchema = """{"type":"object","properties":{"text":{"type":"string","minLength":1},"hint":{"type":"string"},"waitMs":{"type":"number","minimum":0}},"required":["text"]}"""
    override val risk = ActionRisk.MEDIUM
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val text = arguments.optString("text")
        if (text.isBlank()) return ToolResult(false, "text requerido")
        val hint = arguments.optString("hint").trim().ifBlank { null }
        val wait = arguments.optLong("waitMs", 0).coerceIn(0, 8000)
        if (wait > 0) SystemClock.sleep(wait)
        val svc = YinoAccessibilityService.instance() ?: return ToolResult(false, "Accesibilidad no disponible")
        val ok = svc.findEditableAndType(text, hint)
        return ToolResult(ok, if (ok) "ACTION_SET_TEXT aceptado para un campo editable${hint?.let { " (hint='$it')" } ?: ""}; verificar que '$text' apareció" else "No se pudo escribir en un campo editable visible")
    }
}

class UiWaitTool : Tool {
    override val id = "ui_wait"
    override val description = "Espera la cantidad de milisegundos indicada a que la UI cargue."
    override val parametersJsonSchema = """{"type":"object","properties":{"ms":{"type":"number","minimum":0,"maximum":8000}},"required":["ms"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val ms = arguments.optLong("ms", 1000).coerceIn(0, 8000)
        SystemClock.sleep(ms)
        return ToolResult(true, "Esperé $ms ms")
    }
}
