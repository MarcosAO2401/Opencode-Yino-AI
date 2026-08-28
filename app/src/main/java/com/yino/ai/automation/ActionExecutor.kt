package com.yino.ai.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Ejecuta acciones de UI en una coroutine fuera del hilo de callback del
 * AccessibilityService. Esto previene el ANR (Application Not Responding)
 * que ocurre si se hace trabajo pesado dentro de onAccessibilityEvent.
 */
class ActionExecutor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val actions = MutableSharedFlow<UiAction>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private var service: AccessibilityService? = null
    private val handler = Handler(Looper.getMainLooper())

    fun attach(service: AccessibilityService) {
        this.service = service
        actions.onEach { execute(it) }.launchIn(scope)
    }

    fun submit(action: UiAction) {
        actions.tryEmit(action)
    }

    private fun execute(action: UiAction) {
        val svc = service ?: return
        when (action) {
            is UiAction.Tap -> svc.dispatchGesture(gesture(listOf(
                Stroke(action.x, action.y, action.x, action.y, 1L, 60L)
            )), null, null)
            is UiAction.Swipe -> svc.dispatchGesture(gesture(listOf(
                Stroke(action.x1, action.y1, action.x2, action.y2, 0L, 300L)
            )), null, null)
            is UiAction.Global -> handler.post { svc.performGlobalAction(action.action) }
            is UiAction.ClickNode -> action.node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            is UiAction.TypeText -> {
                val b = android.os.Bundle().apply { putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, action.text) }
                action.node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, b)
            }
        }
    }

    private fun gesture(strokes: List<Stroke>): GestureDescription {
        val builder = GestureDescription.Builder()
        strokes.forEach { st ->
            val path = Path().apply { moveTo(st.x1, st.y1); lineTo(st.x2, st.y2) }
            builder.addStroke(GestureDescription.StrokeDescription(path, st.start, st.dur))
        }
        return builder.build()
    }

    private data class Stroke(
        val x1: Float, val y1: Float, val x2: Float, val y2: Float,
        val start: Long, val dur: Long,
    )
}
