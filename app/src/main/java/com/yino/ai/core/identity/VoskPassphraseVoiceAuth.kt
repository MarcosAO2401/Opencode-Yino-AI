package com.yino.ai.core.identity

import android.content.Context
import com.yino.ai.core.settings.SecureSettings
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer

/**
 * Implementación real de [VoiceAuthProvider] basada en reconocimiento del habla
 * (Vosk, 100% on-device, sin red) en lugar de embeddings de hablante.
 *
 * Limitación de seguridad: Vosk small-es es un modelo de transcripción, no de
 * locutor. Por tanto esto verifica el CONOCIMIENTO de la frase (lo que se dijo),
 * no la identidad biológica del hablante. Se añade una comprobación de "vivacidad"
 * acústica (energía RMS) para descartar muestras silenciosas o reproducciones planas.
 *
 * Asume que [sample]/[samples] son FloatArray PCM de 16 kHz (convención del proyecto:
 * ver VoskSttProvider SpeechService(recognizer, 16000.0f)).
 */
class VoskPassphraseVoiceAuth(
    private val secure: SecureSettings,
) : VoiceAuthProvider {

    override val isEnrolled: Boolean
        get() = secure.enrolledPassphrase.isNotBlank()

    override suspend fun enroll(context: Context, samples: List<FloatArray>): Boolean =
        withContext(Dispatchers.IO) {
            if (samples.isEmpty()) return@withContext false
            val phrases = samples.mapNotNull { transcribe(it) }.filter { it.isNotBlank() }
            if (phrases.isEmpty()) return@withContext false
            // Frase más frecuente entre las muestras como passphrase enrolada.
            val chosen = phrases.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
                ?: phrases.first()
            secure.enrolledPassphrase = chosen
            true
        }

    override suspend fun verify(context: Context, sample: FloatArray): Boolean =
        withContext(Dispatchers.IO) {
            if (!isEnrolled) return@withContext false
            if (!hasAcousticLiveness(sample)) return@withContext false
            val spoken = transcribe(sample) ?: return@withContext false
            if (spoken.isBlank()) return@withContext false
            val enrolled = secure.enrolledPassphrase
            if (spoken.equals(enrolled, ignoreCase = true)) return@withContext true
            levenshteinRatio(normalize(spoken), normalize(enrolled)) <= SIMILARITY_TOLERANCE
        }

    /** Transcribe una muestra PCM a texto normalizado, o null si no hay modelo/ruta. */
    private fun transcribe(samples: FloatArray): String? {
        val modelPath = secure.voskModelPath
        if (modelPath.isBlank()) return null
        val model = try {
            Model(modelPath)
        } catch (e: Exception) {
            return null
        }
        try {
            val recognizer = Recognizer(model, 16000.0f)
            try {
                recognizer.acceptWaveForm(samples, samples.size)
                val json = recognizer.getFinalResult()
                return normalize(JSONObject(json).optString("text", ""))
            } finally {
                recognizer.close()
            }
        } finally {
            model.close()
        }
    }

    private fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    /** Vivacidad acústica: la muestra debe tener energía RMS mínima. */
    private fun hasAcousticLiveness(samples: FloatArray): Boolean {
        if (samples.isEmpty()) return false
        var sumSq = 0.0
        for (s in samples) sumSq += s * s.toDouble()
        val rms = sqrt(sumSq / samples.size)
        return rms >= LIVENESS_RMS_THRESHOLD
    }

    private fun levenshteinRatio(a: String, b: String): Double {
        if (a == b) return 0.0
        val (s, t) = if (a.length <= b.length) a to b else b to a
        val costs = IntArray(s.length + 1) { it }
        for (j in 1..t.length) {
            var prev = costs[0]
            costs[0] = j
            for (i in 1..s.length) {
                val tmp = costs[i]
                costs[i] = min3(
                    costs[i] + 1,
                    costs[i - 1] + 1,
                    prev + if (s[i - 1] == t[j - 1]) 0 else 1,
                )
                prev = tmp
            }
        }
        val dist = costs[s.length]
        val denom = if (a.length >= b.length) a.length else b.length
        return dist.toDouble() / denom
    }

    private fun min3(a: Int, b: Int, c: Int): Int {
        var m = a
        if (b < m) m = b
        if (c < m) m = c
        return m
    }

    companion object {
        private const val LIVENESS_RMS_THRESHOLD = 0.001
        private const val SIMILARITY_TOLERANCE = 0.25
    }
}
