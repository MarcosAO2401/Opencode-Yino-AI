package com.yino.ai.voice

import android.content.Context
import java.io.File
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService

/**
 * Speech-to-text 100% ON-DEVICE con Vosk (Apache 2.0). No envía audio a
 * ningún servidor: el modelo corre localmente. Requiere un modelo descargado
 * (p. ej. vosk-model-small-es-0.42) en el path indicado.
 */
class VoskSttProvider(private val context: Context) : STTProvider {

    private var model: Model? = null
    private var service: SpeechService? = null
    private var pending: ((String) -> Unit)? = null

    fun loadModel(modelPath: String): Boolean = try {
        require(File(modelPath).exists()) { "Modelo no existe: $modelPath" }
        model = Model(modelPath)
        true
    } catch (t: Throwable) {
        false
    }

    fun isModelLoaded(): Boolean = model != null

    fun getModel(): Model? = model

    override fun start() {
        val m = model ?: return
        try {
            val sampleRate = m.sampleRate.toFloat()
            val recognizer = Recognizer(m, sampleRate)
            service = SpeechService(recognizer, sampleRate)
            service?.startListening(object : RecognitionListener {
                override fun onResult(hypothesis: String?) {
                    val text = hypothesis?.let { JSONObject(it).optString("text") } ?: ""
                    if (text.isNotBlank()) pending?.invoke(text)
                }
                override fun onFinalResult(hypothesis: String?) {
                    val text = hypothesis?.let { JSONObject(it).optString("text") } ?: ""
                    if (text.isNotBlank()) pending?.invoke(text)
                }
                override fun onPartialResult(hypothesis: String?) {}
                override fun onError(e: Exception?) { pending?.invoke("") }
                override fun onTimeout() {}
            })
        } catch (e: Exception) {
            pending?.invoke("")
        }
    }

    override fun stop() {
        service?.stop()
    }

    override suspend fun listen(): String = suspendCancellableCoroutine { cont ->
        pending = { text -> if (cont.isActive) cont.resume(text) }
        start()
        cont.invokeOnCancellation { stop() }
    }

    fun shutdown() {
        service?.shutdown()
        service = null
    }
}
