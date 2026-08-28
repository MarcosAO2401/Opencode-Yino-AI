package com.yino.ai.automation

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Acciones de UI que el agente puede solicitar. Se procesan fuera del
 * callback onAccessibilityEvent para evitar ANR (patrón recomendado).
 */
sealed interface UiAction {
    data class Tap(val x: Float, val y: Float) : UiAction
    data class Swipe(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : UiAction
    data class Global(val action: Int) : UiAction
    data class ClickNode(val node: AccessibilityNodeInfo) : UiAction
    data class TypeText(val node: AccessibilityNodeInfo, val text: String) : UiAction
}
