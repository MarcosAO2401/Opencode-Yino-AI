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

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // El parseo de la UI se hace bajo demanda (ver ScreenUnderstandingEngine),
        // no aquí, para no bloquear el callback.
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        InstanceHolder.instance = null
        executor = null
        super.onDestroy()
    }

    private fun ensureExecutor(): Boolean = executor != null

    /** Encola el gesto y devuelve false si el servicio no está disponible o el buffer lo rechazó. */
    fun tap(x: Float, y: Float): Boolean = executor?.submit(UiAction.Tap(x, y)) == true

    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float): Boolean =
        executor?.submit(UiAction.Swipe(x1, y1, x2, y2)) == true

    fun global(action: Int): Boolean = executor?.submit(UiAction.Global(action)) == true

    fun click(node: AccessibilityNodeInfo): Boolean =
        executor?.submit(UiAction.ClickNode(node)) == true

    fun type(node: AccessibilityNodeInfo, text: String): Boolean =
        executor?.submit(UiAction.TypeText(node, text)) == true

    fun root(): AccessibilityNodeInfo? = rootInActiveWindow

    /**
     * Busca el primer nodo cuya etiqueta (texto o contentDescription) contenga
     * [text] y hace clic en él (o en su ancestro clickable). Devuelve true si
     * encontró y disparó el clic. Se usa para manejar cualquier app por nombre.
     */
    fun findAndClick(text: String): Boolean {
        if (!ensureExecutor()) return false
        var clicked = false
        withRoot { root ->
            if (root == null) return@withRoot
            fun dfs(node: AccessibilityNodeInfo?): Boolean {
                if (node == null) return false
                val label =
                    (node.text?.toString().orEmpty()) + " " + (node.contentDescription?.toString().orEmpty())
                if (label.contains(text, ignoreCase = true)) {
                    var t: AccessibilityNodeInfo? = node
                    while (t != null && !t.isClickable) t = t.parent
                    (t ?: node).performAction(ACTION_CLICK)
                    return true
                }
                for (i in 0 until node.childCount) {
                    if (dfs(node.getChild(i))) return true
                }
                return false
            }
            clicked = dfs(root)
        }
        return clicked
    }

    /**
     * Escribe [text] en el primer campo editable visible (buscador, caja de
     * mensaje, etc.). Devuelve true si encontró un campo y envió el texto.
     */
    fun findEditableAndType(text: String): Boolean {
        if (!ensureExecutor()) return false
        var ok = false
        withRoot { root ->
            if (root == null) return@withRoot
            fun dfs(node: AccessibilityNodeInfo?): Boolean {
                if (node == null) return false
                if (node.isEditable) {
                    val b = Bundle().apply {
                        putCharSequence(ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                    }
                    return node.performAction(ACTION_SET_TEXT, b)
                }
                for (i in 0 until node.childCount) {
                    if (dfs(node.getChild(i))) return true
                }
                return false
            }
            ok = dfs(root)
        }
        return ok
    }

    private inline fun withRoot(block: (AccessibilityNodeInfo?) -> Unit) {
        val root = rootInActiveWindow
        try {
            block(root)
        } finally {
            root?.recycle()
        }
    }

    companion object {
        fun instance(): YinoAccessibilityService? = InstanceHolder.instance
        fun isEnabled(): Boolean = InstanceHolder.instance != null
    }

    private object InstanceHolder {
        var instance: YinoAccessibilityService? = null
    }
}