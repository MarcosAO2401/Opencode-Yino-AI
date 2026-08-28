# Yino AI — Seguridad

Medidas implementadas para que la app sea **segura por diseño**:

## 1. Identidad del dueño (anti-suplantación)
- `ChatScreen` y `VoiceScreen` exigen `IdentityGate.verifyFace()` (BiometricPrompt
  del sistema: face/huella en TEE) **antes** de ejecutar cualquier comando.
- Si no es el dueño → `denyUnknownSpeaker()`; el agente no corre.
- `requireVoice` (speaker verification on-device) disponible como segundo factor.

## 2. Secretos cifrados
- La API key del LLM se guarda en `EncryptedSharedPreferences` (AES-256-GCM vía
  `AndroidKeyStore` + Tink), nunca en claro ni en `SharedPreferences` normales.

## 3. Autorización por riesgo
- `SecurityGate`: toda herramienta de riesgo `MEDIO`/`ALTO` requiere
  confirmación explícita del usuario (`PendingApproval` → UI → `respond()`).
- `ActionRisk`: LOW (abrir app) / MEDIO (tap, scroll) / ALTO (enviar mensaje).

## 4. Auditoría
- `AuditLog` registra cada acción: herramienta, riesgo, aprobación y resultado.
- Visible en la pantalla **Memoria**.

## 5. Privacidad / on-device
- STT con **Vosk** (on-device): el audio de tu voz **nunca** sale del teléfono.
- TTS con Android TTS (on-device).
- LLM **local** (GGUF) opcional: sin enviar datos a ningún servidor.
- Solo el LLM cloud (si lo eliges) envía texto del prompt a tu proveedor.

## 6. Permisos mínimos
- `RECORD_AUDIO` solo para STT; `INTERNET` solo si usas LLM cloud.
- Accesibilidad y Notification Listener se habilitan **manualmente** por el
  usuario y solo se usan para leer/controllar la UI bajo su consentimiento.
- No se evaden las restricciones de Android (foreground services, permisos).

## 7. Integridad
- Clean Architecture: el agente no puede acceder a APIs del sistema sin pasar
  por `ToolRegistry` + `SecurityGate` + `IdentityGate`.
- Sin código ofuscado ni telemetría oculta.
