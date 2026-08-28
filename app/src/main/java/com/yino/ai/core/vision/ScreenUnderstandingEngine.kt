package com.yino.ai.core.vision

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Compacta el árbol de accesibilidad de la pantalla en texto legible para
 * el LLM (patrón de OpenRing / DroidLM: solo labels visibles, sin ruido).
 * Esto es lo que permite al agente "ver" la UI y decidir acciones.
 */
object ScreenUnderstandingEngine {

    fun summarize(root: AccessibilityNodeInfo?, maxNodes: Int = 60): String {
        if (root == null) return "(sin pantalla accesible; revisa permiso de Accesibilidad)"
        val sb = StringBuilder()
        var count = 0
        fun walk(node: AccessibilityNodeInfo?) {
            if (node == null || count >= maxNodes) return
            val text = node.text?.toString()
            val desc = node.contentDescription?.toString()
            val cls = node.className?.toString()?.substringAfterLast(".")
            val label = text ?: desc
            if (!label.isNullOrBlank() && cls != null) {
                val clickable = if (node.isClickable) " [tap]" else ""
                sb.appendLine("- $cls \"$label\"$clickable")
                count++
            }
            for (i in 0 until node.childCount) walk(node.getChild(i))
        }
        walk(root)
        return if (sb.isBlank()) "(pantalla sin texto legible)" else sb.toString()
    }
}
