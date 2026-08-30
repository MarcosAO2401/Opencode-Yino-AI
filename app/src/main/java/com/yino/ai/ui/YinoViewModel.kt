package com.yino.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yino.ai.core.YinoGraph
import com.yino.ai.core.security.SecurityGate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

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
            if (com.yino.ai.core.YinoGraph::appContext.isInitialized) {
                YinoGraph.security.pendingApprovals.collect { approval ->
                    _pending.value = approval
                }
            }
        }
        viewModelScope.launch {
            runCatching {
                if (com.yino.ai.core.YinoGraph::appContext.isInitialized) {
                    YinoGraph.memory.loadLatest().forEach {
                        _messages.value = _messages.value + ChatMessageUi(it.first, it.second)
                    }
                }
            }
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _busy.value) return
        _messages.value = _messages.value + ChatMessageUi("user", trimmed)
        _busy.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = runAgent(trimmed)
                _messages.value = _messages.value + ChatMessageUi("assistant", result)
            } catch (e: Exception) {
                _messages.value = _messages.value +
                    ChatMessageUi(
                        "assistant",
                        "No pudimos completar la solicitud. Reintenta o revisa la conexión con el motor.",
                        isError = true,
                        detail = e.message ?: e.javaClass.simpleName,
                    )
            } finally {
                _busy.value = false
            }
        }
    }

    suspend fun runAgent(text: String): String {
        val result = YinoGraph.agent.run(text)
        YinoGraph.memory.append("user", text)
        YinoGraph.memory.append("assistant", result)
        return result
    }

    fun append(role: String, text: String) {
        _messages.value = _messages.value + ChatMessageUi(role, text)
    }

    fun approve(id: String) {
        YinoGraph.security.respond(id, true)
        _pending.value = null
    }

    fun deny(id: String) {
        YinoGraph.security.respond(id, false)
        _pending.value = null
    }

    fun denyUnknownSpeaker() {
        _messages.value = _messages.value + ChatMessageUi(
            "assistant",
            "🔒 No reconozco a quien habla. Solo el dueño puede darme órdenes. " +
                "Verifica tu identidad en la pantalla de Identidad.",
        )
    }
}
