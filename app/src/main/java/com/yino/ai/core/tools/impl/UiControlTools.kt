package com.yino.ai.core.tools.impl

import android.os.SystemClock
import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

/**
 * Herramientas de control de UI agnósticas a la app. Combinadas con
 * read_screen permiten al agente "manejar" cualquier aplicación (Jarvis):
 * lee la pantalla, encuentra un elemento por texto y hace clic, o escribe
 * en el campo editable visible.
 */
class UiClickTool : Tool {
    override val id = "ui_click"
    override val description =
        "Busca en la pantalla un elemento cuyo texto o descripción contenga el texto indicado y hace clic en él. Úsalo para pulsar botones, chats, contactos, 'Enviar', etc. Opcional: waitMs para esperar antes (ms)."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"text":{"type":"string"},"waitMs":{"type":"number"}},"required":["text"]}"""
    override val risk = ActionRisk.MEDIUM
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val text = arguments.optString("text")
        if (text.isBlank()) return ToolResult(false, "text requerido")
        val wait = arguments.optLong("waitMs", 0)
        if (wait > 0) SystemClock.sleep(wait)
        val svc = YinoAccessibilityService.instance()
            ?: return ToolResult(false, "Accesibilidad no disponible")
        val ok = svc.findAndClick(text)
        return ToolResult(ok, if (ok) "Clic en '$text'" else "No encontré '$text' en pantalla")
    }
}

class UiTypeTool : Tool {
    override val id = "ui_type"
    override val description =
        "Escribe el texto en el primer campo de texto editable visible (buscador, caja de mensaje, etc.). Opcional: waitMs para esperar antes (ms)."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"text":{"type":"string"},"waitMs":{"type":"number"}},"required":["text"]}"""
    override val risk = ActionRisk.MEDIUM
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val text = arguments.optString("text")
        if (text.isBlank()) return ToolResult(false, "text requerido")
        val wait = arguments.optLong("waitMs", 0)
        if (wait > 0) SystemClock.sleep(wait)
        val svc = YinoAccessibilityService.instance()
            ?: return ToolResult(false, "Accesibilidad no disponible")
        val ok = svc.findEditableAndType(text)
        return ToolResult(ok, if (ok) "Texto escrito" else "No encontré un campo editable")
    }
}

class UiWaitTool : Tool {
    override val id = "ui_wait"
    override val description = "Espera la cantidad de milisegundos indicada a que la UI cargue (p. ej. tras abrir una app)."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"ms":{"type":"number"}},"required":["ms"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val ms = arguments.optLong("ms", 1000)
        SystemClock.sleep(ms.coerceIn(0, 8000))
        return ToolResult(true, "Esperé $ms ms")
    }
}
