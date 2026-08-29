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

    private lateinit var executor: ActionExecutor

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
        super.onDestroy()
    }

    fun tap(x: Float, y: Float) = executor.submit(UiAction.Tap(x, y))
    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float) =
        executor.submit(UiAction.Swipe(x1, y1, x2, y2))
    fun global(action: Int) = executor.submit(UiAction.Global(action))
    fun click(node: AccessibilityNodeInfo) = executor.submit(UiAction.ClickNode(node))
    fun type(node: AccessibilityNodeInfo, text: String) =
        executor.submit(UiAction.TypeText(node, text))

    fun root(): AccessibilityNodeInfo? = rootInActiveWindow

    /**
     * Busca el primer nodo cuya etiqueta (texto o contentDescription) contenga
     * [text] y hace clic en él (o en su ancestro clickable). Devuelve true si
     * encontró y disparó el clic. Se usa para manejar cualquier app por nombre.
     */
    fun findAndClick(text: String): Boolean {
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
