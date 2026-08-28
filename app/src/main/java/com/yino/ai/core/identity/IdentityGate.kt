package com.yino.ai.core.identity

import androidx.fragment.app.FragmentActivity

/**
 * Puerta de identidad del dueño. Combina rostro (biometric del sistema)
 * y voz (embeddings on-device). Un comando solo se ejecuta si pasa el
 * factor(es) exigido(s). Esto cumple el requisito: "solo la persona
 * autorizada puede pedirle algo a Yino".
 */
class IdentityGate(
    private val face: FaceAuthProvider,
    private val voice: VoiceAuthProvider,
) {
    var requireFace: Boolean = true
    var requireVoice: Boolean = false

    val faceEnrolled: Boolean get() = face.isHardwareAvailable
    val voiceEnrolled: Boolean get() = voice.isEnrolled

    suspend fun verifyFace(activity: FragmentActivity): Boolean =
        if (requireFace) face.verify(activity) else true

    suspend fun verifyVoice(context: android.content.Context, sample: FloatArray): Boolean =
        if (requireVoice) voice.verify(context, sample) else true

    suspend fun enrollVoice(context: android.content.Context, samples: List<FloatArray>): Boolean =
        voice.enroll(context, samples)

    /**
     * Verificación completa antes de procesar un comando.
     * Devuelve true solo si el solicitante es el dueño.
     */
    suspend fun verifyOwner(
        activity: FragmentActivity,
        context: android.content.Context,
        voiceSample: FloatArray? = null,
    ): Boolean {
        if (requireFace && !verifyFace(activity)) return false
        if (requireVoice) {
            if (!voice.isEnrolled) return false
            if (voiceSample == null || !verifyVoice(context, voiceSample)) return false
        }
        return true
    }
}
