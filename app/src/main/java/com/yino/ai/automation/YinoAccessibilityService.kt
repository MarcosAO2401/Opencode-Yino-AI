package com.yino.ai.automation

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE
import android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK
import android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT

/**
 * Servicio de accesibilidad de Yino. Requiere consentimiento explícito del
 * usuario (Ajustes > Accesibilidad). Solo se usa para leer la pantalla y
 * ejecutar acciones por indicación del agente tras aprobación.
 */
class YinoAccessibilityService : AccessibilityService() {

    private var executor: ActionExecutor? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        executor = ActionExecutor().also { it.attach(this) }
        InstanceHolder.instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        InstanceHolder.instance = null
        executor = null
        super.onDestroy()
    }

    private fun ensureExecutor(): Boolean = executor != null

    fun tap(x: Float, y: Float): Boolean = executor?.submit(UiAction.Tap(x, y)) == true
    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float): Boolean = executor?.submit(UiAction.Swipe(x1, y1, x2, y2)) == true
    fun global(action: Int): Boolean = executor?.submit(UiAction.Global(action)) == true
    fun click(node: AccessibilityNodeInfo): Boolean = executor?.submit(UiAction.ClickNode(node)) == true
    fun type(node: AccessibilityNodeInfo, text: String): Boolean = executor?.submit(UiAction.TypeText(node, text)) == true
    fun root(): AccessibilityNodeInfo? = rootInActiveWindow

    fun findAndClick(text: String): Boolean {
        if (!ensureExecutor()) return false
        var clicked = false
        withRoot { root ->
            if (root == null) return@withRoot
            fun dfs(node: AccessibilityNodeInfo?): Boolean {
                if (node == null) return false
                val label = (node.text?.toString().orEmpty() + " " + node.contentDescription?.toString().orEmpty()).trim()
                if (label.contains(text, ignoreCase = true) && node.isVisibleToUser && node.isEnabled) {
                    var target: AccessibilityNodeInfo? = node
                    while (target != null && !target.isClickable) target = target.parent
                    clicked = target?.performAction(ACTION_CLICK) == true
                    if (!clicked && target != node) clicked = node.performAction(ACTION_CLICK)
                    return clicked
                }
                for (i in 0 until node.childCount) if (dfs(node.getChild(i))) return true
                return false
            }
            clicked = dfs(root)
        }
        return clicked
    }

    /**
     * Busca un campo editable visible. Si [hint] existe, prioriza nodos cuyo
     * texto, descripción o resource-id coincidan con el hint; luego recurre al
     * primer campo editable visible como fallback.
     */
    fun findEditableAndType(text: String, hint: String? = null): Boolean {
        if (!ensureExecutor()) return false
        var ok = false
        withRoot { root ->
            if (root == null) return@withRoot
            val candidates = mutableListOf<AccessibilityNodeInfo>()
            fun collect(node: AccessibilityNodeInfo?) {
                if (node == null) return
                if (node.isEditable && node.isVisibleToUser && node.isEnabled) candidates += node
                for (i in 0 until node.childCount) collect(node.getChild(i))
            }
            collect(root)
            if (candidates.isEmpty()) return@withRoot

            fun score(node: AccessibilityNodeInfo): Int {
                if (hint.isNullOrBlank()) return 0
                val h = hint.trim()
                val values = listOf(
                    node.text?.toString().orEmpty(),
                    node.hintText?.toString().orEmpty(),
                    node.contentDescription?.toString().orEmpty(),
                    node.viewIdResourceName.orEmpty()
                )
                return values.sumOf { value ->
                    when {
                        value.equals(h, ignoreCase = true) -> 100
                        value.contains(h, ignoreCase = true) -> 50
                        else -> 0
                    }
                }
            }

            val target = candidates.maxByOrNull { score(it) } ?: return@withRoot
            val b = Bundle().apply { putCharSequence(ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
            ok = target.performAction(ACTION_SET_TEXT, b)
            if (!ok) {
                // Fallback: focus the field and retry SET_TEXT.
                target.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                ok = target.performAction(ACTION_SET_TEXT, b)
            }
        }
        return ok
    }

    private inline fun withRoot(block: (AccessibilityNodeInfo?) -> Unit) {
        val root = rootInActiveWindow
        try { block(root) } finally { root?.recycle() }
    }

    companion object {
        fun instance(): YinoAccessibilityService? = InstanceHolder.instance
        fun isEnabled(): Boolean = InstanceHolder.instance != null
    }

    private object InstanceHolder {
        var instance: YinoAccessibilityService? = null
    }
}
