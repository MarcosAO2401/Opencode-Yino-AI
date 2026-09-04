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

    var localLlmBaseUrl: String
        get() = prefs.getString(KEY_LOCAL_LLM_URL, DEFAULT_LOCAL_URL) ?: DEFAULT_LOCAL_URL
        set(value) = prefs.edit().putString(KEY_LOCAL_LLM_URL, value).apply()

    companion object {
        private const val KEY_API = "llm_api_key"
        private const val KEY_URL = "llm_base_url"
        private const val KEY_MODEL_NAME = "llm_model"
        private const val KEY_LOCAL = "use_local_llm"
        private const val KEY_LOCAL_LLM_URL = "local_llm_base_url"

        const val DEFAULT_URL = "https://api.groq.com/openai/v1/chat/completions"
        const val DEFAULT_MODEL = "llama-3.3-70b-versatile"
        const val DEFAULT_LOCAL_URL = "http://127.0.0.1:11434/v1/chat/completions"
    }
}
