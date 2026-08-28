package com.yino.ai.core.identity

import android.content.Context
import kotlin.math.sqrt

/**
 * Verificación de hablante (speaker verification) ON-DEVICE.
 *
 * Android NO trae esta capacidad de forma nativa. El enfoque real es:
 *  - Enrollment: grabar frases del dueño y extraer un embedding de voz
 *    (vector) con un modelo (resemblyzer / WeNet / Nemo SpeakerVerif / o
 *    Picovoice SpeakerRec on-device).
 *  - Verify: extraer embedding de la muestra y comparar con los enrolados
 *    por similitud de coseno; si supera un umbral, es el dueño.
 *
 * Esta versión es un STUB compilable: define la interfaz y la lógica de
 * comparación; sustituye [extractEmbedding] por la inferencia del modelo
 * real (TFLite/ONNX). No se puede entrenar el modelo en este entorno.
 */
interface VoiceAuthProvider {
    val isEnrolled: Boolean
    suspend fun enroll(context: Context, samples: List<FloatArray>): Boolean
    suspend fun verify(context: Context, sample: FloatArray): Boolean
}

class EmbeddingVoiceAuthProvider(
    private val threshold: Float = 0.82f,
) : VoiceAuthProvider {

    private val enrolled = mutableListOf<FloatArray>()

    override val isEnrolled: Boolean get() = enrolled.isNotEmpty()

    override suspend fun enroll(context: Context, samples: List<FloatArray>): Boolean {
        enrolled.clear()
        enrolled += samples
        return enrolled.isNotEmpty()
    }

    override suspend fun verify(context: Context, sample: FloatArray): Boolean {
        if (enrolled.isEmpty()) return false
        val best = enrolled.maxOf { cosine(sample, it) }
        return best >= threshold
    }

    // TODO: reemplazar por el modelo real. Devuelve embedding simulado.
    private fun extractEmbedding(): FloatArray = FloatArray(256) { 0f }

    private fun cosine(a: FloatArray, b: FloatArray): Float {
        var dot = 0f; var na = 0f; var nb = 0f
        for (i in a.indices) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
        val d = sqrt(na) * sqrt(nb)
        return if (d == 0f) 0f else dot / d
    }
}
