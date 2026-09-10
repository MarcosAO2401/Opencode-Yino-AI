package com.yino.ai.core

import android.content.Context
import com.yino.ai.automation.YinoAccessibilityService
import com.yino.ai.core.agent.AgentLoop
import com.yino.ai.core.llm.CloudLLMProvider
import com.yino.ai.core.llm.LocalLLMProvider
import com.yino.ai.core.llm.LLMProvider
import com.yino.ai.core.identity.IdentityGate
import com.yino.ai.core.identity.SystemBiometricFaceAuth
import com.yino.ai.core.identity.VoskPassphraseVoiceAuth
import com.yino.ai.core.security.SecurityGate
import com.yino.ai.core.security.AuditLog
import com.yino.ai.core.settings.SecureSettings
import com.yino.ai.core.tools.ToolRegistry
import com.yino.ai.core.tools.impl.BackTool
import com.yino.ai.core.tools.impl.GoHomeTool
import com.yino.ai.core.tools.impl.OpenAppTool
import com.yino.ai.core.tools.impl.ReadNotificationsTool
import com.yino.ai.core.tools.impl.ReadScreenTool
import com.yino.ai.core.tools.impl.ScrollTool
import com.yino.ai.core.tools.impl.SendMessageTool
import com.yino.ai.core.tools.impl.SetAlarmTool
import com.yino.ai.core.tools.impl.SetTimerTool
import com.yino.ai.core.tools.impl.AddCalendarEventTool
import com.yino.ai.core.tools.impl.CallTool
import com.yino.ai.core.tools.impl.OpenUrlTool
import com.yino.ai.core.tools.impl.PlayMusicTool
import com.yino.ai.core.tools.impl.SendEmailTool
import com.yino.ai.core.tools.impl.SetVolumeTool
import com.yino.ai.core.tools.impl.TakePhotoTool
import com.yino.ai.core.tools.impl.TapTool
import com.yino.ai.core.tools.impl.UiClickTool
import com.yino.ai.core.tools.impl.UiTypeTool
import com.yino.ai.core.tools.impl.UiWaitTool
import com.yino.ai.core.tools.impl.WeatherTool
import com.yino.ai.core.tools.impl.WebSearchTool
import com.yino.ai.core.tools.impl.NotificationReplyTool
import com.yino.ai.data.memory.MemoryRepository
import com.yino.ai.voice.PiperTtsProvider
import com.yino.ai.voice.TTSProvider

object YinoGraph {
    private var _appContext: Context? = null
    val appContext: Context
        get() = _appContext ?: throw IllegalStateException("YinoGraph not initialized. Call init() first.")
    val isInitialized: Boolean
        get() = _appContext != null

    lateinit var secure: SecureSettings
        private set
    lateinit var llm: LLMProvider
        private set
    val registry: ToolRegistry = ToolRegistry()
    val security: SecurityGate = SecurityGate()
    lateinit var identity: IdentityGate
        private set
    lateinit var memory: MemoryRepository
        private set
    lateinit var tts: TTSProvider
        private set
    lateinit var agent: AgentLoop
        private set

    fun init(context: Context) {
        if (isInitialized) return
        _appContext = context.applicationContext
        secure = SecureSettings(appContext)
        // Auto-configurar Vosk si está en Download pero no en destino
        try {
            val dest = java.io.File(appContext.getExternalFilesDir(null), "vosk-model-small-es-0.42")
            if (!dest.exists() || !java.io.File(dest, "am").exists()) {
                val src = java.io.File("/storage/emulated/0/Download/YinoAI/vosk-model-small-es-0.42")
                val srcAlt = java.io.File("/storage/emulated/0/Download/YinoAI/vosk-model")
                val srcToUse = when {
                    src.exists() && java.io.File(src, "am").exists() -> src
                    srcAlt.exists() && java.io.File(srcAlt, "am").exists() -> srcAlt
                    else -> null
                }
                srcToUse?.let { s ->
                    copyRecursive(s, dest)
                    secure.voskModelPath = dest.absolutePath
                }
            }
            // Auto-fijar URL local a 127.0.0.1 si está con IP antigua 192.168.1.123
            if (secure.localLlmBaseUrl.contains("192.168.1.123")) {
                secure.localLlmBaseUrl = SecureSettings.DEFAULT_LOCAL_URL
            }
            // Auto-fijar modelo a qwen2.5:3b si está con 7b y 3b existe
            if (secure.localModelName == "qwen2.5:7b" || secure.localModelName == "llama3") {
                secure.localModelName = SecureSettings.DEFAULT_LOCAL_MODEL
            }
        } catch (_: Exception) {}
        identity = IdentityGate(
            face = SystemBiometricFaceAuth(),
            voice = VoskPassphraseVoiceAuth(secure),
            secure = secure,
        )
        AuditLog.init(appContext)
        memory = MemoryRepository(appContext)
        tts = PiperTtsProvider(appContext)
        registerTools()
        rebuildLlm()
    }

    private fun copyRecursive(src: java.io.File, dest: java.io.File) {
        if (src.isDirectory) {
            dest.mkdirs()
            src.listFiles()?.forEach { copyRecursive(it, java.io.File(dest, it.name)) }
        } else {
            src.inputStream().use { input -> dest.outputStream().use { output -> input.copyTo(output) } }
        }
    }

    private fun registerTools() {
        registry.register(OpenAppTool(appContext))
        registry.register(SendMessageTool(appContext))
        registry.register(WebSearchTool(appContext))
        registry.register(WeatherTool(appContext))
        registry.register(SetAlarmTool(appContext))
        registry.register(SetTimerTool(appContext))
        registry.register(AddCalendarEventTool(appContext))
        registry.register(OpenUrlTool(appContext))
        registry.register(SetVolumeTool(appContext))
        registry.register(TakePhotoTool(appContext))
        registry.register(CallTool(appContext))
        registry.register(SendEmailTool(appContext))
        registry.register(PlayMusicTool(appContext))
        registry.register(NotificationReplyTool(appContext))
        registry.register(GoHomeTool())
        registry.register(BackTool())
        registry.register(ReadScreenTool())
        registry.register(ReadNotificationsTool())
        registry.register(ScrollTool())
        registry.register(TapTool())
        registry.register(UiClickTool())
        registry.register(UiTypeTool())
        registry.register(UiWaitTool())
    }

    private fun rebuildLlm() {
        llm = if (secure.useLocalLlm) {
            LocalLLMProvider(
                secure,
                secure.localModelName.ifBlank { SecureSettings.DEFAULT_LOCAL_MODEL }
            )
        } else {
            CloudLLMProvider(
                baseUrl = secure.llmBaseUrl.ifBlank { SecureSettings.DEFAULT_URL },
                apiKeyParam = secure.apiKey,
                model = secure.llmModel.ifBlank { SecureSettings.DEFAULT_MODEL },
            )
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

    fun setLlmBaseUrl(url: String) {
        secure.llmBaseUrl = url
        if (!secure.useLocalLlm) rebuildLlm()
    }

    fun setLlmModel(model: String) {
        secure.llmModel = model
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

    fun setLocalModelName(name: String) {
        secure.localModelName = name
        if (secure.useLocalLlm) rebuildLlm()
    }

    fun setLocalLlmBaseUrl(url: String) {
        secure.localLlmBaseUrl = url
        if (secure.useLocalLlm) rebuildLlm()
    }
}
