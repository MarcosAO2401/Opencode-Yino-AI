package com.yino.ai.core

import android.content.Context
import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.agent.AgentLoop
import com.yino.ai.core.llm.CloudLLMProvider
import com.yino.ai.core.llm.LocalLLMProvider
import com.yino.ai.core.llm.LLMProvider
import com.yino.ai.core.identity.IdentityGate
import com.yino.ai.core.identity.SystemBiometricFaceAuth
import com.yino.ai.core.identity.EmbeddingVoiceAuthProvider
import com.yino.ai.core.security.SecurityGate
import com.yino.ai.core.settings.SecureSettings
import com.yino.ai.core.tools.ToolRegistry
import com.yino.ai.core.tools.impl.BackTool
import com.yino.ai.core.tools.impl.GoHomeTool
import com.yino.ai.core.tools.impl.OpenAppTool
import com.yino.ai.core.tools.impl.ReadNotificationsTool
import com.yino.ai.core.tools.impl.ReadScreenTool
import com.yino.ai.core.tools.impl.ScrollTool
import com.yino.ai.core.tools.impl.SendMessageTool
import com.yino.ai.core.tools.impl.TapTool
import com.yino.ai.core.tools.impl.WebSearchTool
import com.yino.ai.data.memory.MemoryRepository

object YinoGraph {
    lateinit var appContext: Context
        private set
    lateinit var secure: SecureSettings
        private set
    lateinit var llm: LLMProvider
        private set
    val registry: ToolRegistry = ToolRegistry()
    val security: SecurityGate = SecurityGate()
    val identity: IdentityGate = IdentityGate(
        face = SystemBiometricFaceAuth(),
        voice = EmbeddingVoiceAuthProvider(),
    )
    lateinit var memory: MemoryRepository
        private set
    lateinit var agent: AgentLoop
        private set

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        secure = SecureSettings(appContext)
        memory = MemoryRepository(appContext)
        registerTools()
        rebuildLlm()
    }

    private fun registerTools() {
        registry.register(OpenAppTool(appContext))
        registry.register(SendMessageTool(appContext))
        registry.register(WebSearchTool(appContext))
        registry.register(GoHomeTool())
        registry.register(BackTool())
        registry.register(ReadScreenTool())
        registry.register(ReadNotificationsTool())
        registry.register(ScrollTool())
        registry.register(TapTool())
    }

    private fun rebuildLlm() {
        llm = if (secure.useLocalLlm) {
            LocalLLMProvider(secure.localModelPath.ifBlank { "http://127.0.0.1:8080" })
        } else {
            CloudLLMProvider(apiKeyParam = secure.apiKey)
        }
        agent = AgentLoop(
            llm,
            registry,
            security,
            accessibilityAvailable = { YinoAccessibilityService.isEnabled() },
            grantedPermissions = { emptySet() },
        )
    }

    fun setApiKey(key: String) {
        secure.apiKey = key
        if (!secure.useLocalLlm) rebuildLlm()
    }

    fun setUseLocalLlm(use: Boolean) {
        secure.useLocalLlm = use
        rebuildLlm()
    }

    fun setLocalModelPath(path: String) {
        secure.localModelPath = path
        if (secure.useLocalLlm) rebuildLlm()
    }
}
