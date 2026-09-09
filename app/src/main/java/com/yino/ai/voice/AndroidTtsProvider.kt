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
                val locale = Locale.UK
                tts?.language = locale
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts?.voices?.forEach { voice ->
                        if (voice.locale == locale && voice.quality >= Voice.QUALITY_HIGH &&
                            voice.name.lowercase().contains("male")) {
                            jarvisVoice = voice
                        }
                    }
                    if (jarvisVoice == null) {
                        tts?.voices?.forEach { voice ->
                            if (voice.locale == locale && voice.name.lowercase().contains("male")) {
                                jarvisVoice = voice
                            }
                        }
                    }
                    tts?.setPitch(0.9f)
                    tts?.setSpeechRate(0.95f)
                    jarvisVoice?.let { tts?.voice = it }
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