package com.yino.ai.core.llm

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Proveedor on-device que habla con un SERVIDOR DE INFERENCIA LOCAL
 * (llama.cpp `/completion`, Ollama `/api/generate` u compatible).
 *
 * Ventajas:
 *  - Privado: el texto nunca sale del teléfono.
 *  - No requiere API key ni cuota.
 *
 * Requisito: un motor GGUF corriendo en el dispositivo. Opciones reales:
 *  - llama.cpp-server (binario nativo) vía Termux, apuntando a 127.0.0.1:8080.
 *  - Servidor embebido en la app (futuro: AiKit / llama.cpp JNI).
 *
 * La "ruta del modelo local" en Ajustes se interpreta como la URL base del
 * servidor (por defecto http://127.0.0.1:8080).
 */
class LocalLLMProvider(
    private val baseUrl: String = "http://127.0.0.1:8080",
) : LLMProvider {

    override val id: String = "local:$baseUrl"
    override val supportsTools: Boolean = false

    private val client = HttpClient(OkHttp) { expectSuccess = true }
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun complete(request: LLMRequest): LLMResult {
        return try {
            val prompt = request.messages.last().content
            val body = buildJsonObject {
                put("prompt", prompt)
                put("temperature", request.temperature.toDouble())
                put("n_predict", 256)
                put("stream", false)
            }.toString()

            val raw = client.post("$baseUrl/completion") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.bodyAsText()

            LLMResult.Text(parseContent(raw))
        } catch (e: Exception) {
            LLMResult.Text("(Motor local no disponible en $baseUrl: ${e.message})")
        }
    }

    override fun stream(request: LLMRequest): Flow<LLMResult> = flow {
        emit(complete(request))
    }

    private fun parseContent(raw: String): String = try {
        val obj = json.parseToJsonElement(raw).jsonObject
        obj["content"]?.jsonPrimitive?.content
            ?: obj["response"]?.jsonPrimitive?.content
            ?: raw
    } catch (_: Exception) {
        raw
    }
}
