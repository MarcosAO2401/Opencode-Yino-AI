package com.yino.ai.core.identity

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Verificación de identidad por rostro/huella usando el biometric del
 * SISTEMA (face-unlock ya registrado en el dispositivo, protegido por TEE).
 * Es la vía segura y oficial: no almacena imágenes, no reinventa cripto.
 */
interface FaceAuthProvider {
    val isHardwareAvailable: Boolean
    suspend fun verify(activity: FragmentActivity): Boolean
}

class SystemBiometricFaceAuth : FaceAuthProvider {

    override val isHardwareAvailable: Boolean
        get() = true

    override suspend fun verify(activity: FragmentActivity): Boolean =
        suspendCancellableCoroutine { cont ->
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (cont.isActive) cont.resume(true)
                    }
                    override fun onAuthenticationError(code: Int, err: CharSequence) {
                        if (cont.isActive) cont.resume(false)
                    }
                    override fun onAuthenticationFailed() {
                        if (cont.isActive) cont.resume(false)
                    }
                })
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Yino · Verifica que eres tú")
                .setSubtitle("Usa tu rostro o huella registrados en el dispositivo")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                            or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
            prompt.authenticate(info)
        }
}
