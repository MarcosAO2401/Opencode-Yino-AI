package com.yino.ai.automation

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Escucha notificaciones entrantes (nivel C). El agente puede reaccionar
 * a mensajes/eventos, pero solo con el permiso NOTIFICATION_LISTENER
 * concedido manualmente por el usuario.
 *
 * Además captura las acciones de "Responder" (RemoteInput) que apps como
 * Instagram y Facebook Messenger adjuntan a sus notificaciones de DM. Eso
 * permite enviar/responder un DM de forma fiable (sin scraping de UI) usando
 * la propia acción de la notificación.
 */
class YinoNotificationListener : NotificationListenerService() {

    data class Notice(val packageName: String, val title: String?, val text: String?)
    data class ReplyTarget(
        val pendingIntent: PendingIntent,
        val remoteInput: RemoteInput,
        val label: String?,
    )

    private val _notices = MutableSharedFlow<Notice>(extraBufferCapacity = 64)
    val notices = _notices.asSharedFlow()

    private val buffer = mutableListOf<Notice>()
    private val replyTargets = mutableMapOf<String, ReplyTarget>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val notice = Notice(sbn.packageName, title, text)
        _notices.tryEmit(notice)
        synchronized(buffer) {
            buffer += notice
            if (buffer.size > 50) buffer.removeAt(0)
        }
        captureReply(sbn)
    }

    private fun captureReply(sbn: StatusBarNotification) {
        val actions: Array<out Notification.Action>? = sbn.notification.actions
        actions?.forEach { action ->
            action.remoteInputs?.firstOrNull()?.let { ri ->
                synchronized(replyTargets) {
                    replyTargets[sbn.packageName] =
                        ReplyTarget(action.actionIntent, ri, action.title?.toString())
                }
            }
        }
    }

    /** Devuelve la acción de respuesta capturada para el paquete, si existe. */
    fun getReply(packageName: String): ReplyTarget? =
        synchronized(replyTargets) { replyTargets[packageName] }

    override fun onListenerConnected() {
        instance = this
        super.onListenerConnected()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    fun recent(): List<Notice> = synchronized(buffer) { buffer.toList() }

    companion object {
        fun instance(): YinoNotificationListener? = InstanceHolder.instance
        fun isEnabled(): Boolean = InstanceHolder.instance != null
        private var instance: YinoNotificationListener? = null
            set(value) { field = value; InstanceHolder.instance = value }
        private object InstanceHolder {
            var instance: YinoNotificationListener? = null
        }
    }
}

