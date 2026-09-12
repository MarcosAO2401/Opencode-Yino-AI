package com.yino.ai.core.vision

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Convierte el árbol de accesibilidad en una representación compacta para el
 * agente. Solo expone estados que Android proporciona mediante AccessibilityNodeInfo.
 */
object ScreenUnderstandingEngine {

    fun summarize(root: AccessibilityNodeInfo?, maxNodes: Int = 80): String {
        if (root == null) return "(sin pantalla accesible; revisa permiso de Accesibilidad)"

        val sb = StringBuilder()
        var count = 0
        val packageName = root.packageName?.toString().orEmpty()
        if (packageName.isNotBlank()) sb.appendLine("package=$packageName")

        fun walk(node: AccessibilityNodeInfo?, depth: Int) {
            if (node == null || count >= maxNodes) return

            val text = node.text?.toString()?.trim().orEmpty()
            val desc = node.contentDescription?.toString()?.trim().orEmpty()
            val label = when {
                text.isNotBlank() -> text
                desc.isNotBlank() -> desc
                else -> ""
            }
            val cls = node.className?.toString()?.substringAfterLast(".").orEmpty()
            val resourceId = node.viewIdResourceName?.takeIf { it.isNotBlank() }
            val bounds = Rect().also { node.getBoundsInScreen(it) }
            val actionable = node.isClickable || node.isEditable || node.isScrollable ||
                node.isCheckable || node.isFocusable

            if (label.isNotBlank() || actionable) {
                val indent = "  ".repeat(depth.coerceAtMost(8))
                val flags = buildList {
                    if (node.isClickable) add("tap")
                    if (node.isEditable) add("editable")
                    if (node.isScrollable) add("scroll")
                    if (node.isCheckable) add("checkable")
                    if (node.isChecked) add("checked")
                    if (node.isSelected) add("selected")
                    if (node.isFocusable) add("focusable")
                    if (node.isEnabled) add("enabled") else add("disabled")
                }
                sb.append(indent).append("- ").append(cls.ifBlank { "Node" })
                if (label.isNotBlank()) {
                    sb.append(" \"").append(label.replace("\"", "'")).append("\"")
                }
                if (resourceId != null) sb.append(" id=").append(resourceId)
                if (flags.isNotEmpty()) sb.append(" [").append(flags.joinToString(",")).append("]")
                if (!bounds.isEmpty) {
                    sb.append(" bounds=")
                        .append(bounds.left).append(",")
                        .append(bounds.top).append(",")
                        .append(bounds.right).append(",")
                        .append(bounds.bottom)
                }
                sb.appendLine()
                count++
            }

            for (i in 0 until node.childCount) {
                if (count >= maxNodes) break
                val child = node.getChild(i)
                try {
                    walk(child, depth + 1)
                } finally {
                    child?.recycle()
                }
            }
        }

        walk(root, 0)
        return if (sb.isBlank()) "(pantalla sin contenido legible ni controles accesibles)" else sb.toString()
    }
}
