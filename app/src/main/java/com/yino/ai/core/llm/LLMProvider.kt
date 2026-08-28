package com.yino.ai.core.llm

import kotlinx.coroutines.flow.Flow

enum class Role { SYSTEM, USER, ASSISTANT, TOOL }

data class ChatMessage(val role: Role, val content: String)

data class ToolSpec(
    val name: String,
    val description: String,
    val parametersJsonSchema: String,
)

data class LLMRequest(
    val messages: List<ChatMessage>,
    val tools: List<ToolSpec> = emptyList(),
    val temperature: Float = 0.7f,
)

sealed interface LLMResult {
    data class Text(val content: String) : LLMResult
    data class ToolCall(val name: String, val argumentsJson: String) : LLMResult
}

/**
 * Abstracción del "cerebro". Permite intercambiar proveedor cloud
 * (OpenAI-compatible, Gemini, Claude, DeepSeek) por uno local (GGUF
 * vía llama.cpp / AiKit) sin tocar el resto del sistema.
 */
interface LLMProvider {
    val id: String
    val supportsTools: Boolean
    suspend fun complete(request: LLMRequest): LLMResult
    fun stream(request: LLMRequest): Flow<LLMResult> = throw UnsupportedOperationException("streaming no implementado")
}
