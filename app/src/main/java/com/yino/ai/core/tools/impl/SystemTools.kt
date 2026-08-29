package com.yino.ai.core.tools.impl

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.CalendarContract
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Herramientas de control del sistema que funcionan con Intents estándar de
 * Android (sin permisos especiales). Esto es lo que convierte a Yino en un
 * asistente tipo Jarvis: puede poner alarmas, timers, eventos, subir/bajar
 * volumen y abrir la cámara o una URL.
 *
 * Se usan los literales de acción/extra documentados para máxima compatibilidad
 * de compilación.
 */
private const val ACTION_SET_ALARM = "android.intent.action.SET_ALARM"
private const val ACTION_SET_TIMER = "android.intent.action.SET_TIMER"
private const val EXTRA_HOUR = "android.intent.extra.alarm.HOUR"
private const val EXTRA_MINUTES = "android.intent.extra.alarm.MINUTES"
private const val EXTRA_MESSAGE = "android.intent.extra.alarm.MESSAGE"
private const val EXTRA_SKIP_UI = "android.intent.extra.alarm.SKIP_UI"
private const val EXTRA_LENGTH_SECONDS = "android.intent.extra.alarm.LENGTH_SECONDS"
private const val EXTRA_EVENT_BEGIN = "android.intent.extra.EVENT_BEGIN_TIME"
private const val EXTRA_EVENT_END = "android.intent.extra.EVENT_END_TIME"

class SetAlarmTool(private val context: Context) : Tool {
    override val id = "set_alarm"
    override val description = "Pone una alarma del sistema a una hora y minuto (0-23, 0-59). Opcional: message."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"hour":{"type":"integer"},"minute":{"type":"integer"},"message":{"type":"string"}},"required":["hour","minute"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val hour = arguments.optInt("hour", -1)
        val minute = arguments.optInt("minute", -1)
        if (hour !in 0..23 || minute !in 0..59) {
            return ToolResult(false, "hour (0-23) y minute (0-59) requeridos")
        }
        return try {
            val intent = Intent(ACTION_SET_ALARM).apply {
                putExtra(EXTRA_HOUR, hour)
                putExtra(EXTRA_MINUTES, minute)
                putExtra(EXTRA_MESSAGE, arguments.optString("message", "Yino"))
                putExtra(EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(true, "Alarma puesta a las $hour:$minute")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}

class SetTimerTool(private val context: Context) : Tool {
    override val id = "set_timer"
    override val description = "Pone un temporizador del sistema en segundos."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"seconds":{"type":"integer"}},"required":["seconds"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val seconds = arguments.optInt("seconds", -1)
        if (seconds <= 0) return ToolResult(false, "seconds debe ser > 0")
        return try {
            val intent = Intent(ACTION_SET_TIMER).apply {
                putExtra(EXTRA_LENGTH_SECONDS, seconds)
                putExtra(EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(true, "Temporizador de $seconds s puesto")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}

class AddCalendarEventTool(private val context: Context) : Tool {
    override val id = "add_calendar_event"
    override val description = "Crea un evento en el calendario del sistema con título y duración en minutos."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"title":{"type":"string"},"durationMinutes":{"type":"integer"},"description":{"type":"string"}},"required":["title"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val title = arguments.optString("title")
        if (title.isBlank()) return ToolResult(false, "title requerido")
        val duration = arguments.optLong("durationMinutes", 60)
        val begin = System.currentTimeMillis() + 60_000
        val end = begin + duration * 60_000
        return try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra("title", title)
                putExtra(EXTRA_EVENT_BEGIN, begin)
                putExtra(EXTRA_EVENT_END, end)
                putExtra("description", arguments.optString("description", ""))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(true, "Evento '$title' creado en el calendario")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}

class OpenUrlTool(private val context: Context) : Tool {
    override val id = "open_url"
    override val description = "Abre una URL en el navegador."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"url":{"type":"string"}},"required":["url"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val url = arguments.optString("url")
        if (url.isBlank()) return ToolResult(false, "url requerida")
        return try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            ToolResult(true, "Abriendo $url")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}

class SetVolumeTool(private val context: Context) : Tool {
    override val id = "set_volume"
    override val description = "Ajusta el volumen (0-100) de un stream: music, ring, alarm, call."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"level":{"type":"integer"},"stream":{"type":"string"}},"required":["level"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val level = arguments.optInt("level", -1)
        if (level !in 0..100) return ToolResult(false, "level debe ser 0-100")
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val stream = when (arguments.optString("stream", "music").lowercase()) {
            "ring", "ringer" -> AudioManager.STREAM_RING
            "alarm" -> AudioManager.STREAM_ALARM
            "call" -> AudioManager.STREAM_VOICE_CALL
            else -> AudioManager.STREAM_MUSIC
        }
        val max = am.getStreamMaxVolume(stream)
        val vol = (max * level / 100f).roundToInt()
        return try {
            am.setStreamVolume(stream, vol, 0)
            ToolResult(true, "Volumen de ${arguments.optString("stream", "music")} en $level%")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}

class TakePhotoTool(private val context: Context) : Tool {
    override val id = "take_photo"
    override val description = "Abre la cámara para tomar una foto (la app de cámara guarda en DCIM)."
    override val parametersJsonSchema = """{"type":"object","properties":{}}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        return try {
            context.startActivity(
                Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            ToolResult(true, "Cámara abierta para tomar foto")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}
