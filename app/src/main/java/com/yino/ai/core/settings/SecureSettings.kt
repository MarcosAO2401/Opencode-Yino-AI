package com.yino.ai.core.settings

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Almacenamiento seguro de configuración sensible (p. ej. la API key del LLM).
 * Usa EncryptedSharedPreferences (AES256-GCM vía AndroidKeyStore + Tink): la
 * key nunca queda en claro en disco. Es la forma oficial de guardar secretos
 * en Android sin un backend.
 */
class SecureSettings(context: Context) {

    private val masterKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        "yino_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

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
        get() = prefs.getBoolean(KEY_LOCAL, false)
        set(value) = prefs.edit().putBoolean(KEY_LOCAL, value).apply()

    var localModelPath: String
        get() = prefs.getString(KEY_MODEL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var voskModelPath: String
        get() = prefs.getString(KEY_VOSK, DEFAULT_VOSK) ?: DEFAULT_VOSK
        set(value) = prefs.edit().putString(KEY_VOSK, value).apply()

    var wakeWordEnabled: Boolean
        get() = prefs.getBoolean(KEY_WAKE, true)
        set(value) = prefs.edit().putBoolean(KEY_WAKE, value).apply()

    companion object {
        private const val KEY_API = "llm_api_key"
        private const val KEY_URL = "llm_base_url"
        private const val KEY_MODEL_NAME = "llm_model"
        private const val KEY_LOCAL = "use_local_llm"
        private const val KEY_MODEL = "local_model_path"
        private const val KEY_VOSK = "vosk_model_path"
        private const val KEY_WAKE = "wake_word_enabled"
        const val DEFAULT_VOSK =
            "/storage/emulated/0/Android/data/com.yino.ai/files/vosk-model-small-es-0.42"
        const val DEFAULT_URL = "https://api.openai.com/v1/chat/completions"
        const val DEFAULT_MODEL = "gpt-4o-mini"
    }
}
