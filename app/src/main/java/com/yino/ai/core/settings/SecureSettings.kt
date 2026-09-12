package com.yino.ai.core.settings

import android.content.Context
import android.content.SharedPreferences

class SecureSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("yino_prefs", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString(KEY_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API, value).apply()
    var llmBaseUrl: String
        get() = prefs.getString(KEY_URL, DEFAULT_URL) ?: DEFAULT_URL
        set(value) = prefs.edit().putString(KEY_URL, value).apply()
    var llmModel: String
        get() = prefs.getString(KEY_MODEL_NAME, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL_NAME, value).apply()
    var useLocalLlm: Boolean
        get() = prefs.getBoolean(KEY_LOCAL, true)
        set(value) = prefs.edit().putBoolean(KEY_LOCAL, value).apply()

    /** NVIDIA NIM / Try NVIDIA Nemotron cloud provider. */
    var useNvidiaNemotron: Boolean
        get() = prefs.getBoolean(KEY_NVIDIA_NEMOTRON, false)
        set(value) = prefs.edit().putBoolean(KEY_NVIDIA_NEMOTRON, value).apply()
    var nvidiaApiKey: String
        get() = prefs.getString(KEY_NVIDIA_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NVIDIA_API, value).apply()
    var nvidiaBaseUrl: String
        get() = prefs.getString(KEY_NVIDIA_URL, DEFAULT_NVIDIA_URL) ?: DEFAULT_NVIDIA_URL
        set(value) = prefs.edit().putString(KEY_NVIDIA_URL, value).apply()
    var nvidiaModel: String
        get() = prefs.getString(KEY_NVIDIA_MODEL, DEFAULT_NVIDIA_MODEL) ?: DEFAULT_NVIDIA_MODEL
        set(value) = prefs.edit().putString(KEY_NVIDIA_MODEL, value).apply()

    var localLlmBaseUrl: String
        get() = prefs.getString(KEY_LOCAL_LLM_URL, DEFAULT_LOCAL_URL) ?: DEFAULT_LOCAL_URL
        set(value) = prefs.edit().putString(KEY_LOCAL_LLM_URL, value).apply()
    var localModelName: String
        get() = prefs.getString(KEY_LOCAL_MODEL_NAME, DEFAULT_LOCAL_MODEL) ?: DEFAULT_LOCAL_MODEL
        set(value) = prefs.edit().putString(KEY_LOCAL_MODEL_NAME, value).apply()
    var localModelPath: String
        get() = prefs.getString(KEY_LOCAL_MODEL_PATH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LOCAL_MODEL_PATH, value).apply()
    var requireFace: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_FACE, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUIRE_FACE, value).apply()
    var requireVoice: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_VOICE, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUIRE_VOICE, value).apply()
    var enrolledPassphrase: String
        get() = prefs.getString(KEY_ENROLLED_PASSPHRASE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ENROLLED_PASSPHRASE, value).apply()
    var voskModelPath: String
        get() = prefs.getString(KEY_VOSK_MODEL_PATH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_VOSK_MODEL_PATH, value).apply()
    var wakeWordEnabled: Boolean
        get() = prefs.getBoolean(KEY_WAKE_WORD_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, value).apply()

    companion object {
        private const val KEY_API = "llm_api_key"
        private const val KEY_URL = "llm_base_url"
        private const val KEY_MODEL_NAME = "llm_model"
        private const val KEY_LOCAL = "use_local_llm"
        private const val KEY_NVIDIA_NEMOTRON = "use_nvidia_nemotron"
        private const val KEY_NVIDIA_API = "nvidia_api_key"
        private const val KEY_NVIDIA_URL = "nvidia_base_url"
        private const val KEY_NVIDIA_MODEL = "nvidia_model"
        private const val KEY_LOCAL_LLM_URL = "local_llm_base_url"
        private const val KEY_LOCAL_MODEL_NAME = "local_model_name"
        private const val KEY_LOCAL_MODEL_PATH = "local_model_path"
        private const val KEY_REQUIRE_FACE = "require_face"
        private const val KEY_REQUIRE_VOICE = "require_voice"
        private const val KEY_ENROLLED_PASSPHRASE = "enrolled_passphrase"
        private const val KEY_VOSK_MODEL_PATH = "vosk_model_path"
        private const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"

        const val DEFAULT_URL = "https://api.groq.com/openai/v1/chat/completions"
        const val DEFAULT_MODEL = "llama-3.3-70b-versatile"
        const val DEFAULT_NVIDIA_URL = "https://integrate.api.nvidia.com/v1/chat/completions"
        const val DEFAULT_NVIDIA_MODEL = "nvidia/nemotron-3.5-lightning-30b-a3b"
        const val DEFAULT_LOCAL_URL = "http://127.0.0.1:11434/v1/chat/completions"
        const val DEFAULT_LOCAL_MODEL = "llama3.2:3b"
    }
}
