package com.yino.ai.core.llm

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/** OpenAI-compatible on-device inference server provider. */
class LocalLLMProvider(
    private val secureSettings: com.yino.ai.core.settings.SecureSettings,
    private val model: String,
) : LLMProvider {
    override val id: String = "local:$model"
    override val supportsTools: Boolean = true

    private val baseUrl: String
        get() = secureSettings.localLlmBaseUrl.ifBlank {
            com.yino.ai.core.settings.SecureSettings.DEFAULT_LOCAL_URL
        }
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
    }

    @Serializable private data class Req(
        val model: String,
        val messages: List<Msg>,
        val temperature: Float,
        val tools: List<Tool>? = null,
    )
    @Serializable private data class Msg(val role: String, val content: String)
    @Serializable private data class Tool(val type: String = "function", val function: Fun)
    @Serializable private data class Fun(val name: String, val description: String, val parameters: JsonElement)
    @Serializable private data class Resp(val choices: List<Choice>? = null, val error: RespError? = null)
    @Serializable private data class RespError(val message: String)
    @Serializable private data class Choice(val message: RespMsg)
    @Serializable private data class RespMsg(val content: String? = null, val tool_calls: List<ToolCall>? = null)
    @Serializable private data class ToolCall(val function: ToolCallFun)
    @Serializable private data class ToolCallFun(val name: String, val arguments: String)

    private fun roleName(role: Role): String = when (role) {
        Role.SYSTEM -> "system"
        Role.USER -> "user"
        Role.ASSISTANT -> "assistant"
        Role.TOOL -> "tool"
    }

    override suspend fun complete(request: LLMRequest): LLMResult {
        val tools = if (request.tools.isEmpty()) null else request.tools.map { toolSpec ->
            val parameters = runCatching { json.parseToJsonElement(toolSpec.parametersJsonSchema) }
                .getOrElse { failure ->
                    return LLMResult.Text("(Esquema JSON inválido para la herramienta ${toolSpec.name}: ${failure.message})")
                }
            Tool(function = Fun(toolSpec.name, toolSpec.description, parameters))
        }
        val body = Req(
            model = model,
            messages = request.messages.map { message -> Msg(roleName(message.role), message.content) },
            temperature = request.temperature,
            tools = tools,
        )
        return try {
            val response = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            val resp: Resp = response.body()
            if (resp.error != null) return LLMResult.Text("(Error del servidor local: ${resp.error.message})")
            val choice = resp.choices?.firstOrNull() ?: return LLMResult.Text("(Respuesta vacía del motor local)")
            val tc = choice.message.tool_calls?.firstOrNull()
            if (tc != null) LLMResult.ToolCall(tc.function.name, tc.function.arguments)
            else LLMResult.Text(choice.message.content ?: "")
        } catch (e: Exception) {
            LLMResult.Text("(Error de conexión en $baseUrl: ${e.message})")
        }
    }
}