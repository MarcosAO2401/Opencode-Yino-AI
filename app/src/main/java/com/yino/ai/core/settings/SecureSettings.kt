package com.yino.ai.core.settings

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureSettings(context: Context) {

    private val appContext = context.applicationContext

    private val prefs by lazy {
        try {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                appContext,
                "yino_secure",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (error: Exception) {
            Log.w("YinoAI", "EncryptedSharedPreferences falló: ${error.message}. Usando SharedPreferences sin cifrar.")
            appContext.getSharedPreferences("yino_secure_fallback", Context.MODE_PRIVATE)
        }
    }

    val isEncryptionCompromised: Boolean
        get() = prefs.getBoolean("encryption_failed", false)

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

    var localModelPath: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_LOCAL_MODEL_PATH) ?: DEFAULT_LOCAL_MODEL_PATH
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var localModelName: String
        get() = prefs.getString(KEY_MODEL_NAME, DEFAULT_LOCAL_MODEL) ?: DEFAULT_LOCAL_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL_NAME, value).apply()

    var localLlmBaseUrl: String
        get() = prefs.getString(KEY_LOCAL_LLM_URL, DEFAULT_LOCAL_URL) ?: DEFAULT_LOCAL_URL
        set(value) = prefs.edit().putString(KEY_LOCAL_LLM_URL, value).apply()

    var voskModelPath: String
        get() = prefs.getString(KEY_VOSK, DEFAULT_VOSK) ?: DEFAULT_VOSK
        set(value) = prefs.edit().putString(KEY_VOSK, value).apply()

    var wakeWordEnabled: Boolean
        get() = prefs.getBoolean(KEY_WAKE, true)
        set(value) = prefs.edit().putBoolean(KEY_WAKE, value).apply()

    var requireFace: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_FACE, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUIRE_FACE, value).apply()

    var requireVoice: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_VOICE, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUIRE_VOICE, value).apply()

    var enrolledPassphrase: String
        get() = prefs.getString(KEY_PASSPHRASE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PASSPHRASE, value).apply()

    companion object {
        private const val KEY_API = "llm_api_key"
        private const val KEY_URL = "llm_base_url"
        private const val KEY_MODEL_NAME = "llm_model"
        private const val KEY_LOCAL = "use_local_llm"
        private const val KEY_MODEL = "local_model_path"
        private const val KEY_VOSK = "vosk_model_path"
        private const val KEY_PASSPHRASE = "enrolled_passphrase"
        private const val KEY_LOCAL_LLM_URL = "local_llm_base_url"
        private const val KEY_WAKE = "wake_word_enabled"
        private const val KEY_REQUIRE_FACE = "require_face"
        private const val KEY_REQUIRE_VOICE = "require_voice"

        const val DEFAULT_VOSK = "/storage/emulated/0/Download/YinoAI/vosk-model"
        const val DEFAULT_URL = "https://api.openai.com/v1/chat/completions"
        const val DEFAULT_MODEL = "gpt-4o-mini"
        const val DEFAULT_LOCAL_MODEL = "llama3"
        const val DEFAULT_LOCAL_URL = "http://192.168.1.123:11434/v1/chat/completions"
        const val DEFAULT_LOCAL_MODEL_PATH = "/storage/emulated/0/Download/YinoAI/gguf-model.gguf"
    }
}
