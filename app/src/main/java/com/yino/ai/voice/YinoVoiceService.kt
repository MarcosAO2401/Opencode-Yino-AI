package com.yino.ai.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.yino.ai.MainActivity
import com.yino.ai.core.YinoGraph
import com.yino.ai.core.agent.AgentLoop
import com.yino.ai.core.security.SecurityGate
import com.yino.ai.automation.YinoAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Servicio en primer plano que mantiene a Yino ESCUCHANDO manos libres:
 *
 *   wake-word (on-device) -> STT (Vosk on-device) -> Agente (ReAct) -> TTS
 *
 * Todo el audio se procesa localmente; solo sale del telefono si el usuario
 * pide explicitamente algo que requiere la nube (p. ej. un LLM cloud o web).
 */
class YinoVoiceService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var vosk: VoskSttProvider
    private lateinit var tts: AndroidTtsProvider
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var wake: WakeWordDetector? = null

    override fun onCreate() {
        super.onCreate()
        YinoGraph.init(applicationContext)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        tts = AndroidTtsProvider(applicationContext)
        vosk = VoskSttProvider(this)
        requestAudioFocus()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
        scope.launch {
            runCatching { vosk.loadModel(YinoGraph.secure.voskModelPath) }
                .onFailure { /* modelo no disponible: el servicio sigue vivo pero sin voz */ }
            if (YinoGraph.secure.wakeWordEnabled && vosk.isModelLoaded()) startWake()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel("yino_voice", "Yino Voz", NotificationManager.IMPORTANCE_LOW),
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        wake?.stop()
        wake = null
        vosk.shutdown()
        tts.shutdown()
        abandonAudioFocus()
        scope.cancel()
        super.onDestroy()
    }

    private fun startWake() {
        val model = vosk.getModel() ?: return
        wake?.stop()
        wake = WakeWordDetector(model, onWake = { onWake() })
        wake?.start()
    }

    private fun onWake() {
        scope.launch {
            wake?.stop()
            wake = null
            val text = runCatching { withTimeoutOrNull(12.seconds) { vosk.listen() } ?: "" }
                .getOrDefault("")
            vosk.stop()
            if (text.isNotBlank()) {
                val reply = runBackgroundAgent(text)
                tts.speak(reply)
            }
            if (YinoGraph.secure.wakeWordEnabled) startWake()
        }
    }

    /**
     * En modo manos libres no hay UI para pedir confirmacion, asi que usa una
     * puerta fail-closed: las acciones MEDIO/ALTO se niegan (sin colgar 120s).
     */
    private suspend fun runBackgroundAgent(text: String): String {
        val gate = SecurityGate().apply { interactive = false }
        val agent = AgentLoop(
            YinoGraph.llm,
            YinoGraph.registry,
            gate,
            accessibilityAvailable = { YinoAccessibilityService.isEnabled() },
            grantedPermissions = { emptySet() },
        )
        return runCatching { agent.run(text) }
            .getOrDefault("(no pude procesar el comando en modo manos libres)")
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).build()
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "yino_voice"
        val pi = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Yino escuchando")
            .setContentText("Di \"Yino\" para activarlo")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pi)
            .build()
    }

    companion object {
        const val NOTIF_ID = 1001
    }
}
