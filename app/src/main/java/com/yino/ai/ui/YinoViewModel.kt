package com.yino.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yino.ai.core.YinoGraph
import com.yino.ai.core.security.SecurityGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estado visual de un mensaje del chat. */
data class ChatMessageUi(
    val role: String,
    val text: String,
    val time: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val detail: String? = null,
)

class YinoViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessageUi>>(emptyList())
    val messages: StateFlow<List<ChatMessageUi>> = _messages.asStateFlow()

    private val _pending = MutableStateFlow<SecurityGate.PendingApproval?>(null)
    val pending: StateFlow<SecurityGate.PendingApproval?> = _pending.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    init {
        viewModelScope.launch {
            YinoGraph.security.pendingApprovals.collect { approval ->
                _pending.value = approval
            }
        }
        if (YinoGraph.isInitialized) {
            viewModelScope.launch {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val greeting = when (hour) {
                    in 5..11 -> "Buenos días, Señor."
                    in 12..19 -> "Buenas tardes, Señor."
                    else -> "Buenas noches, Señor."
                }
                append("assistant", "$greeting ¿En qué puedo serle útil hoy?")
                YinoGraph.tts.speak("$greeting ¿En qué puedo serle útil hoy?")
            }
        }
    }

    fun append(role: String, text: String, isError: Boolean = false, detail: String? = null) {
        _messages.value = _messages.value + ChatMessageUi(role, text, isError = isError, detail = detail)
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        append("user", trimmed)
        viewModelScope.launch(Dispatchers.IO) {
            val result = runAgent(trimmed)
            append("assistant", result, result.startsWith("Error del LLM:") || result.startsWith("Error:"))
        }
    }

    suspend fun runAgent(text: String): String {
        _busy.value = true
        return try {
            YinoGraph.agent.run(text)
        } catch (e: Exception) {
            "Error: ${e.message ?: e.javaClass.simpleName}"
        } finally {
            _busy.value = false
        }
    }

    fun approve(requestId: String) {
        YinoGraph.security.respond(requestId, true)
        if (_pending.value?.requestId == requestId) _pending.value = null
    }

    fun deny(requestId: String) {
        YinoGraph.security.respond(requestId, false)
        if (_pending.value?.requestId == requestId) _pending.value = null
    }
}
