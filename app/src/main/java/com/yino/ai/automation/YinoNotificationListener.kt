package com.yino.ai.automation

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Escucha notificaciones entrantes (nivel C). El agente puede reaccionar
 * a mensajes/eventos, pero solo con el permiso NOTIFICATION_LISTENER
 * concedido manualmente por el usuario.
 */
class YinoNotificationListener : NotificationListenerService() {

    data class Notice(val packageName: String, val title: String?, val text: String?)

    private val _notices = MutableSharedFlow<Notice>(extraBufferCapacity = 64)
    val notices = _notices.asSharedFlow()

    private val buffer = mutableListOf<Notice>()

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
