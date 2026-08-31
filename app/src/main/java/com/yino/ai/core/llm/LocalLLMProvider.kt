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
 * Proveedor on-device OpenAI-compatible.
 * Habla con un SERVIDOR DE INFERENCIA
 * LOCAL que exponga la API de OpenAI, p. ej.:
 * - Ollama en Termux: http://127.0.0.1:11434/v1/chat/completions
 * - llama.cpp en modo servidor OpenAI: http://127.0.0.1:8080/v1/chat/completions
 *
 * Al ser OpenAI-compatible, SOPORTA tool_calls: el agente ReAct puede ejecutar
 * acciones (tocar la pantalla, enviar mensajes) 100% en el dispositivo.
 *
 * Ventajas: privado (el texto nunca sale del telefono) y sin API key ni cuota.
 */
class LocalLLMProvider(
    private val baseUrl: String = "http://127.0.0.1:11434/v1/chat/completions",
    private val model: String = "llama3",
) : LLMProvider {

    override val id: String = "local:$model"
    override val supportsTools: Boolean = true

    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
    }

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
    @Serializable private data class ToolCall(val function: ToolCallFun)
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
        return try {
            val resp: Resp = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body()
            val choice = resp.choices.firstOrNull()
                ?: return LLMResult.Text("(sin respuesta del motor local)")
            val tc = choice.message.tool_calls?.firstOrNull()
            if (tc != null) {
                LLMResult.ToolCall(tc.function.name, tc.function.arguments)
            } else {
                LLMResult.Text(choice.message.content ?: "")
            }
        } catch (e: Exception) {
            LLMResult.Text("(Motor local no disponible en $baseUrl: ${e.message})")
        }
    }
}
