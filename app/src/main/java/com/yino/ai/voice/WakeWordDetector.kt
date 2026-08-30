package com.yino.ai.voice

import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService

/**
 * Wake-word (palabra de activación) 100% on-device con Vosk.
 *
 * En lugar de un modelo de keyword spotting aparte, restringe el reconocedor
 * a un gramatica de frases fijas ("yino", "hola yino"…). Cuando el modelo
 * devuelve una de ellas, se invoca [onWake]. Esto mantiene la privacidad:
 * el audio nunca sale del telefono y solo se transcribe completo TRAS el wake.
 */
class WakeWordDetector(
    private val model: Model,
    private val phrases: List<String> = listOf("yino", "hola yino", "ey yino"),
    private val onWake: () -> Unit,
) {
    private var service: SpeechService? = null

    fun start() {
        if (service != null) return
        val grammar = "[" + phrases.joinToString(",") { "\"$it\"" } + "]"
        val sampleRate = model.sampleRate.toFloat()
        val recognizer = Recognizer(model, sampleRate, grammar)
        service = SpeechService(recognizer, sampleRate)
        service?.startListening(object : RecognitionListener {
            override fun onResult(hypothesis: String?) {
                val text = hypothesis?.let { JSONObject(it).optString("text") } ?: ""
                if (text.isNotBlank()) onWake()
            }
            override fun onFinalResult(hypothesis: String?) {}
            override fun onPartialResult(hypothesis: String?) {}
            override fun onError(e: Exception?) {}
            override fun onTimeout() {}
        })
    }

    fun stop() {
        service?.stop()
        service?.shutdown()
        service = null
    }
}
