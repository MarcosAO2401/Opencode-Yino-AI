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
                val locale = Locale("es", "ES")
                val result = tts?.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.getDefault()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts?.voices?.forEach { voice ->
                        val isSpanish = voice.locale.language == "es"
                        val isMale = voice.name.lowercase().contains("male") || voice.name.lowercase().contains("alvaro") || voice.name.lowercase().contains("pablo") || voice.name.lowercase().contains("enrique")
                        if (isSpanish && isMale && voice.quality >= Voice.QUALITY_HIGH) {
                            jarvisVoice = voice
                        }
                    }
                    if (jarvisVoice == null) {
                        tts?.voices?.forEach { voice ->
                            if (voice.locale.language == "es" && voice.name.lowercase().contains("male")) {
                                jarvisVoice = voice
                            }
                        }
                    }
                    if (jarvisVoice == null) {
                        tts?.voices?.forEach { voice ->
                            if (voice.locale.language == "es") {
                                jarvisVoice = voice
                            }
                        }
                    }
                    tts?.setPitch(0.88f)
                    tts?.setSpeechRate(0.92f)
                    jarvisVoice?.let { tts?.voice = it }
                } else {
                    tts?.setPitch(0.88f)
                    tts?.setSpeechRate(0.92f)
                }
                ready = true
            }
        }
    }

    override fun speak(text: String) {
        if (!ready) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yino_utterance")
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