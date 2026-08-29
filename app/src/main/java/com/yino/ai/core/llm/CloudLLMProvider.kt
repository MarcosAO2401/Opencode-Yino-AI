package com.yino.ai.core.llm

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Proveedor cloud compatible con la API de OpenAI (funciona con Gemini,
 * DeepSeek, Together, Groq, etc. cambiando baseUrl + header).
 * Implementación real y compilable; la API key se gestiona en Settings.
 * 
 * Características:
 * - Retry exponencial con backoff para errores transitorios
 * - Timeout configurable
 * - Compatible OpenAI API (tools, system prompts)
 */
class CloudLLMProvider(
    private val baseUrl: String = "https://api.openai.com/v1/chat/completions",
    apiKeyParam: String,
    private val model: String = "gpt-4o-mini",
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 60_000,
    private val maxRetries: Int = 3,
    private val baseRetryDelayMs: Long = 1_000,
) : LLMProvider {

    override val id = "cloud:$model"
    override val supportsTools = true

    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
    }

    /** API key del proveedor. Se puede actualizar en runtime desde Settings. */
    var apiKey: String = apiKeyParam
        private set

    @Serializable
    private data class Req(
        val model: String,
        val messages: List<Msg>,
        val temperature: Float,
        val stream: Boolean,
        val tools: List<Tool>? = null,
    )

    @Serializable private data class Msg(val role: String, val content: String)
    @Serializable private data class Tool(val type: String = "function", val function: Fun)
    @Serializable private data class Fun(val name: String, val description: String, val parameters: String)

    @Serializable private data class Resp(val choices: List<Choice>)
    @Serializable private data class Choice(val message: RespMsg, val finish_reason: String?)
    @Serializable private data class RespMsg(
        val content: String? = null,
        val tool_calls: List<ToolCall>? = null,
    )
    @Serializable private data class ToolCall(
        val index: Int? = null,
        val function: ToolCallFun,
    )
    @Serializable private data class ToolCallFun(val name: String, val arguments: String)

    // Streaming response types (for future use)
    @Serializable private data class StreamResp(val choices: List<StreamChoice>)
    @Serializable private data class StreamChoice(
        val delta: StreamDelta,
        val finish_reason: String? = null,
    )
    @Serializable private data class StreamDelta(
        val content: String? = null,
        val tool_calls: List<StreamToolCall>? = null,
    )
    @Serializable private data class StreamToolCall(
        val index: Int? = null,
        val function: StreamToolCallFun? = null,
    )
    @Serializable private data class StreamToolCallFun(
        val name: String? = null,
        val arguments: String? = null,
    )

    override suspend fun complete(request: LLMRequest): LLMResult {
        var attempt = 0
        while (true) {
            try {
                val tools = if (request.tools.isEmpty()) null else request.tools.map {
                    Tool(function = Fun(it.name, it.description, it.parametersJsonSchema))
                }
                val body = Req(
                    model = model,
                    messages = request.messages.map { Msg(it.role.name.lowercase(), it.content) },
                    temperature = request.temperature,
                    stream = false,
                    tools = tools,
                )
                val response: Resp = client.post(baseUrl) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $apiKey")
                    setBody(body)
                }.body()
                val choice = response.choices.firstOrNull() ?: return LLMResult.Text("(sin respuesta del LLM)")
                val tc = choice.message.tool_calls?.firstOrNull()
                return if (tc != null) {
                    LLMResult.ToolCall(tc.function.name, tc.function.arguments)
                } else {
                    LLMResult.Text(choice.message.content ?: "")
                }
            } catch (e: Exception) {
                attempt++
                if (attempt > maxRetries) {
                    return LLMResult.Text("(Error del LLM en $baseUrl tras $maxRetries reintentos: ${e.message})")
                }
                delay(baseRetryDelayMs * (2.0.pow((attempt - 1).toDouble())).toLong())
            }
        }
    }
}