package com.yino.ai.core.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistencia de configuración de Yino AI.
 *
 * Nota: las claves y otros secretos no deberían permanecer en texto plano
 * en SharedPreferences en una versión de producción. Esta clase mantiene
 * por ahora la compatibilidad con el proyecto existente; el endurecimiento
 * de almacenamiento seguro se hará en una fase separada.
 */
class SecureSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("yino_prefs", Context.MODE_PRIVATE)

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

    /** Nombre del modelo servido por el motor local OpenAI-compatible. */
    var localModelName: String
        get() = prefs.getString(KEY_LOCAL_MODEL_NAME, DEFAULT_LOCAL_MODEL) ?: DEFAULT_LOCAL_MODEL
        set(value) = prefs.edit().putString(KEY_LOCAL_MODEL_NAME, value).apply()

    /** Ruta opcional para un modelo local administrado directamente por Yino. */
    var localModelPath: String
        get() = prefs.getString(KEY_LOCAL_MODEL_PATH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LOCAL_MODEL_PATH, value).apply()

    companion object {
        private const val KEY_API = "llm_api_key"
        private const val KEY_URL = "llm_base_url"
        private const val KEY_MODEL_NAME = "llm_model"
        private const val KEY_LOCAL = "use_local_llm"
        private const val KEY_LOCAL_LLM_URL = "local_llm_base_url"
        private const val KEY_LOCAL_MODEL_NAME = "local_model_name"
        private const val KEY_LOCAL_MODEL_PATH = "local_model_path"

        const val DEFAULT_URL = "https://api.groq.com/openai/v1/chat/completions"
        const val DEFAULT_MODEL = "llama-3.3-70b-versatile"
        const val DEFAULT_LOCAL_URL = "http://127.0.0.1:11434/v1/chat/completions"
        const val DEFAULT_LOCAL_MODEL = "llama3.2:3b"
    }
}
