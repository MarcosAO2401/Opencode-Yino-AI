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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Servicio en primer plano que mantiene a Yino ESCUCHANDO manos libres:
 *
 *   wake-word (on-device) -> STT (Vosk on-device) -> Agente (ReAct) -> TTS
 *
 * Todo el audio se procesa localmente; solo sale del telefono si el usuario
 * pide explicitamente algo que requiere la nube (p. ej. un LLM cloud o web).
 */
class YinoVoiceService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
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
        vosk.loadModel(YinoGraph.secure.voskModelPath)
        requestAudioFocus()
        startForeground(NOTIF_ID, buildNotification())
        if (YinoGraph.secure.wakeWordEnabled) startWake()
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
            val text = vosk.listen()
            vosk.stop()
            if (text.isNotBlank()) {
                val reply = YinoGraph.agent.run(text)
                tts.speak(reply)
            }
            if (YinoGraph.secure.wakeWordEnabled) startWake()
        }
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
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Yino Voz", NotificationManager.IMPORTANCE_LOW),
            )
        }
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
