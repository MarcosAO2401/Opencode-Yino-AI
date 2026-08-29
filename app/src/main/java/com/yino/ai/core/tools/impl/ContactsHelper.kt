package com.yino.ai.core.tools.impl

import android.content.Context
import android.provider.ContactsContract

/**
 * Resuelve un nombre de contacto guardado en el teléfono a su número.
 * Permite decir "envía un SMS a Juan" o "llama a María" sin dar el número.
 */
object ContactsHelper {
    fun resolvePhoneNumber(context: Context, name: String): String? {
        if (name.filter { it.isDigit() }.length >= 7) {
            return name.filter { it.isDigit() }
        }
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val sel = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val args = arrayOf("%$name%")
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                sel,
                args,
                null,
            )?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (_: Exception) {
            null
        }
    }
}
