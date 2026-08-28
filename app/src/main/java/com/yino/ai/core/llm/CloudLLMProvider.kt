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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Proveedor cloud compatible con la API de OpenAI (funciona con Gemini,
 * DeepSeek, Together, Groq, etc. cambiando baseUrl + header).
 * Implementación real y compilable; la API key se gestiona en Settings.
 */
class CloudLLMProvider(
    private val baseUrl: String = "https://api.openai.com/v1/chat/completions",
    apiKeyParam: String,
    private val model: String = "gpt-4o-mini",
) : LLMProvider {

    override val id: String = "cloud:$model"
    override val supportsTools: Boolean = true

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
        val tools: List<Tool>? = null,
    )

    @Serializable private data class Msg(val role: String, val content: String)
    @Serializable private data class Tool(val type: String = "function", val function: Fun)
    @Serializable private data class Fun(val name: String, val description: String, val parameters: String)

    @Serializable private data class Resp(val choices: List<Choice>)
    @Serializable private data class Choice(val message: RespMsg)
    @Serializable private data class RespMsg(
        val content: String? = null,
        val tool_calls: List<ToolCall>? = null,
    )
    @Serializable private data class ToolCall(
        val function: ToolCallFun,
    )
    @Serializable private data class ToolCallFun(val name: String, val arguments: String)

    override suspend fun complete(request: LLMRequest): LLMResult {
        val tools = if (request.tools.isEmpty()) null else request.tools.map {
            Tool(function = Fun(it.name, it.description, it.parametersJsonSchema))
        }
        val body = Req(
            model = model,
            messages = request.messages.map { Msg(it.role.name.lowercase(), it.content) },
            temperature = request.temperature,
            tools = tools,
        )
        val resp: Resp = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            setBody(body)
        }.body()
        val choice = resp.choices.firstOrNull() ?: return LLMResult.Text("(sin respuesta)")
        val tc = choice.message.tool_calls?.firstOrNull()
        return if (tc != null) {
            LLMResult.ToolCall(tc.function.name, tc.function.arguments)
        } else {
            LLMResult.Text(choice.message.content ?: "")
        }
    }
}
