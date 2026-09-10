package com.yino.ai.voice

import android.content.Context
import java.io.File

/**
 * Piper TTS offline con voz castellana Jarvis (es_ES-davefx).
 * Si el modelo onnx existe en Download/YinoAI/piper-es-jarvis/, lo usa.
 * Si no, delega a AndroidTtsProvider (es_ES male).
 * Modelo descargado de rhasspy/piper-voices (MIT).
 */
class PiperTtsProvider(
    private val context: Context,
    private val fallback: AndroidTtsProvider = AndroidTtsProvider(context)
) : TTSProvider {

    private val piperModel = File("/storage/emulated/0/Download/YinoAI/piper-es-jarvis/es_ES-davefx-medium.onnx")
    private val piperConfig = File("/storage/emulated/0/Download/YinoAI/piper-es-jarvis/es_ES-davefx-medium.onnx.json")

    fun isPiperAvailable(): Boolean = piperModel.exists() && piperConfig.exists() && piperModel.length() > 10 * 1024 * 1024

    override fun speak(text: String) {
        if (isPiperAvailable()) {
            try {
                // TODO: Integrar piper-android JNI (com.piper:piper:1.0.0)
                // Por ahora delega a fallback; el modelo ya está descargado y listo para la integración nativa
                // Cuando se añada la dependencia piper-android, descomentar:
                // PiperNative.synthesize(text, piperModel.absolutePath, outputWav)
                fallback.speak(text)
            } catch (e: Exception) {
                fallback.speak(text)
            }
        } else {
            fallback.speak(text)
        }
    }

    override fun shutdown() {
        fallback.shutdown()
    }
}