package com.yino.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yino.ai.core.YinoGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

data class ChatMessageUi(
    val role: String,
    val text: String,
    val time: Long = System.currentTimeMillis(),
)

class YinoViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessageUi>>(emptyList())
    val messages: StateFlow<List<ChatMessageUi>> = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            if (YinoGraph.isInitialized) {
                // Saludo simple, sin clima, sin redirecciones
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val greeting = when (hour) {
                    in 5..11 -> "Buenos días, Señor."
                    in 12..19 -> "Buenas tardes, Señor."
                    else -> "Buenas noches, Señor."
                }
                
                _messages.value = _messages.value + ChatMessageUi("assistant", "$greeting ¿En qué puedo serle útil hoy?")
                YinoGraph.tts.speak("$greeting ¿En qué puedo serle útil hoy?")
            }
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        _messages.value = _messages.value + ChatMessageUi("user", trimmed)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = YinoGraph.agent.run(trimmed)
                _messages.value = _messages.value + ChatMessageUi("assistant", result)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessageUi("assistant", "Error: ${e.message}")
            }
        }
    }
}
