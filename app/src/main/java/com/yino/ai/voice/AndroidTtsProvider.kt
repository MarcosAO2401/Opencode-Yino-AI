package com.yino.ai.voice

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class AndroidTtsProvider(context: Context) : TTSProvider {

    private val tts: TextToSpeech?
    private var ready = false
    private var jarvisVoice: Voice? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configurar voz estilo Jarvis: Inglés Británico, masculino, tono bajo
                val locale = Locale.UK
                tts?.language = locale
                
                // Buscar voz masculina británica (estilo Jarvis)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val voices = tts?.voices
                    voices?.forEach { voice ->
                        if (voice.locale == locale && voice.quality == Voice.QUALITY_HIGH &&
                            voice.name.lowercase().contains("male")) {
                            jarvisVoice = voice
                        }
                    }
                    // Fallback: primera voz masculina británica disponible
                    if (jarvisVoice == null) {
                        voices?.forEach { voice ->
                            if (voice.locale == locale && voice.name.lowercase().contains("male")) {
                                jarvisVoice = voice
                            }
                        }
                    }
                }
                ready = true
            }
        }
    }

    override fun speak(text: String) {
        if (!ready) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val params = android.os.Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_PITCH, 0.9f)      // Tono ligeramente más bajo
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)    // Volumen normal
                putFloat(TextToSpeech.Engine.KEY_PARAM_RATE, 0.95f)     // Velocidad ligeramente más lenta
            }
            jarvisVoice?.let { voice ->
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "yino_utterance", voice)
            } ?: run {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "yino_utterance")
            }
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}