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
import kotlinx.serialization.json.JsonElement
import kotlinx.coroutines.delay
import kotlin.math.pow

/** OpenAI-compatible cloud provider. */
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
    private val apiKey = apiKeyParam
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
    }

    @Serializable private data class Req(
        val model: String,
        val messages: List<Msg>,
        val temperature: Float,
        val stream: Boolean,
        val tools: List<Tool>? = null,
    )
    @Serializable private data class Msg(
        val role: String,
        val content: String? = null,
        val tool_call_id: String? = null,
        val tool_calls: List<AssistantToolCall>? = null,
    )
    @Serializable private data class AssistantToolCall(
        val id: String,
        val type: String = "function",
        val function: AssistantFunction,
    )
    @Serializable private data class AssistantFunction(val name: String, val arguments: String)
    @Serializable private data class Tool(val type: String = "function", val function: Fun)
    @Serializable private data class Fun(val name: String, val description: String, val parameters: JsonElement)
    @Serializable private data class Resp(val choices: List<Choice>)
    @Serializable private data class Choice(val message: RespMsg, val finish_reason: String?)
    @Serializable private data class RespMsg(val content: String? = null, val tool_calls: List<ToolCall>? = null)
    @Serializable private data class ToolCall(val id: String? = null, val index: Int? = null, val function: ToolCallFun)
    @Serializable private data class ToolCallFun(val name: String, val arguments: String)

    private fun toMessage(message: ChatMessage): Msg {
        return when (message.role) {
            Role.ASSISTANT -> Msg(
                role = "assistant",
                content = message.content.ifBlank { null },
                tool_calls = if (message.toolCallName != null && message.toolCallArguments != null && message.toolCallId != null) {
                    listOf(AssistantToolCall(message.toolCallId, function = AssistantFunction(message.toolCallName, message.toolCallArguments)))
                } else null,
            )
            Role.TOOL -> Msg(role = "tool", content = message.content, tool_call_id = message.toolCallId)
            Role.SYSTEM -> Msg(role = "system", content = message.content)
            Role.USER -> Msg(role = "user", content = message.content)
        }
    }

    override suspend fun complete(request: LLMRequest): LLMResult {
        var attempt = 0
        while (true) {
            try {
                val tools = if (request.tools.isEmpty()) null else request.tools.map { toolSpec ->
                    val parameters = runCatching { json.parseToJsonElement(toolSpec.parametersJsonSchema) }
                        .getOrElse { failure ->
                            return LLMResult.Text("(Esquema JSON inválido para la herramienta ${toolSpec.name}: ${failure.message})")
                        }
                    Tool(function = Fun(toolSpec.name, toolSpec.description, parameters))
                }
                val body = Req(
                    model = model,
                    messages = request.messages.map(::toMessage),
                    temperature = request.temperature,
                    stream = false,
                    tools = tools,
                )
                val response: Resp = client.post(baseUrl) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer ${apiKey.trim()}")
                    setBody(body)
                }.body()
                val choice = response.choices.firstOrNull() ?: return LLMResult.Text("(sin respuesta del LLM)")
                val tc = choice.message.tool_calls?.firstOrNull()
                return if (tc != null) LLMResult.ToolCall(tc.function.name, tc.function.arguments, tc.id)
                else LLMResult.Text(choice.message.content ?: "")
            } catch (e: Exception) {
                attempt++
                if (attempt > maxRetries) return LLMResult.Text("(Error del LLM en $baseUrl tras $maxRetries reintentos: ${e.message})")
                delay(baseRetryDelayMs * (2.0.pow((attempt - 1).toDouble())).toLong())
            }
        }
    }
}
