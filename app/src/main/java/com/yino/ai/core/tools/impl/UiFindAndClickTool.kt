package com.yino.ai.core.tools.impl

import android.view.accessibility.AccessibilityNodeInfo
import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

/**
 * Busca un control visible por texto, contentDescription o resource-id y lo pulsa.
 * La acción se acepta solo si AccessibilityNodeInfo.performAction devuelve true.
 * El AgentLoop debe releer la pantalla para verificar el efecto real.
 */
class UiFindAndClickTool : Tool {
    override val id = "ui_find_and_click"
    override val description = "Busca un elemento visible por texto, descripción o resource-id y lo pulsa"
    override val parametersJsonSchema = """{
        "type":"object",
        "properties":{
            "query":{"type":"string","minLength":1},
            "match":{"type":"string","enum":["text","description","resource_id","any"]},
            "exact":{"type":"boolean"}
        },
        "required":["query"]
    }""".trimIndent()
    override val risk = ActionRisk.MEDIUM
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        if (!ctx.accessibilityAvailable || !YinoAccessibilityService.isEnabled()) {
            return ToolResult(false, "Accesibilidad no está disponible.")
        }

        val query = arguments.optString("query").trim()
        if (query.isEmpty()) return ToolResult(false, "query no puede estar vacío.")
        val match = arguments.optString("match", "any")
        val exact = arguments.optBoolean("exact", false)
        val root = YinoAccessibilityService.instance()?.root()
            ?: return ToolResult(false, "No hay árbol de accesibilidad disponible.")

        var found = 0
        var matchedNode: AccessibilityNodeInfo? = null

        fun matches(value: CharSequence?): Boolean {
            val candidate = value?.toString()?.trim().orEmpty()
            if (candidate.isEmpty()) return false
            return if (exact) candidate.equals(query, ignoreCase = true)
            else candidate.contains(query, ignoreCase = true)
        }

        fun walk(node: AccessibilityNodeInfo?) {
            if (node == null || matchedNode != null) return
            val textMatch = match == "text" || match == "any"
            val descMatch = match == "description" || match == "any"
            val idMatch = match == "resource_id" || match == "any"
            val matched = (textMatch && matches(node.text)) ||
                (descMatch && matches(node.contentDescription)) ||
                (idMatch && matches(node.viewIdResourceName))

            if (matched && node.isVisibleToUser && node.isEnabled) {
                found++
                matchedNode = AccessibilityNodeInfo.obtain(node)
                return
            }

            for (i in 0 until node.childCount) {
                if (matchedNode != null) break
                walk(node.getChild(i))
            }
        }

        try {
            walk(root)
        } finally {
            root.recycle()
        }

        val node = matchedNode ?: return ToolResult(false, "No encontré un elemento visible que coincida con '$query'.")
        return try {
            val clickable = node.isClickable
            val accepted = if (clickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                false
            }
            if (accepted) {
                ToolResult(true, "Elemento encontrado y click aceptado para '$query'. Relee la pantalla para verificar el resultado.")
            } else {
                ToolResult(false, "Encontré '$query', pero Android no aceptó ACTION_CLICK (clickable=$clickable).")
            }
        } finally {
            node.recycle()
        }
    }
}
