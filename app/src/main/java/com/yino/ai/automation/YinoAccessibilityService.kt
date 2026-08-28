package com.yino.ai.automation

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

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

    fun root(): AccessibilityNodeInfo? = rootInActiveWindow

    companion object {
        fun instance(): YinoAccessibilityService? = InstanceHolder.instance
        fun isEnabled(): Boolean = InstanceHolder.instance != null
    }

    private object InstanceHolder {
        var instance: YinoAccessibilityService? = null
    }
}
