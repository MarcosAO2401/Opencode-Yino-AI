package com.yino.ai.integrations

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppIntegration {
    fun openChat(context: Context, contact: String) {
        val uri = Uri.parse("https://wa.me/${Uri.encode(contact)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun send(context: Context, contact: String, msg: String) {
        val uri = Uri.parse("https://wa.me/${Uri.encode(contact)}?text=${Uri.encode(msg)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
