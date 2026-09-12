package com.yino.ai.core.llm

import kotlinx.coroutines.flow.Flow

enum class Role { SYSTEM, USER, ASSISTANT, TOOL }

data class ChatMessage(
    val role: Role,
    val content: String,
    val toolCallId: String? = null,
    val toolCallName: String? = null,
    val toolCallArguments: String? = null,
)

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
    data class ToolCall(val name: String, val argumentsJson: String, val id: String? = null) : LLMResult
}

/**
 * Abstracción del cerebro. Permite intercambiar proveedores OpenAI-compatible
 * y proveedores locales sin tocar el resto del sistema.
 */
interface LLMProvider {
    val id: String
    val supportsTools: Boolean
    suspend fun complete(request: LLMRequest): LLMResult
    fun stream(request: LLMRequest): Flow<LLMResult> = throw UnsupportedOperationException("streaming no implementado")
}
