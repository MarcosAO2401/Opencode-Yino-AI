# Yino AI

Asistente personal de Android tipo Jarvis: modular, seguro, extensible.
Kotlin + Jetpack Compose + Clean Architecture + ReAct agent loop.

## Stack
- Kotlin 2.0.21 · Jetpack Compose (BOM 2024.10.00) · Material 3
- Hilt · Room · DataStore · WorkManager · Ktor (LLM cloud)
- Accesibilidad (AccessibilityService) para control de UI
- Biometría del sistema (face/huella) + speaker verification on-device

## Módulos
- `core/llm` — `LLMProvider` (cloud OpenAI-compatible + local GGUF stub)
- `core/tools` — `Tool`, `ToolRegistry`, herramientas (`impl/`)
- `core/agent` — `AgentLoop` (ReAct: Observation→Plan→Action→Verify)
- `core/security` — `SecurityGate` (aprobación MEDIO/ALTO) + `AuditLog`
- `core/identity` — `IdentityGate` (face + voice), SOLO el dueño manda
- `automation` — `YinoAccessibilityService`, `ActionExecutor` (anti-ANR)
- `voice` — STT/TTS on-device + `YinoVoiceService`
- `integrations` — WhatsApp, Calendar
- `data/memory` — Room (conversaciones/mensajes)
- `ui` — pantallas Compose (Chat, Voz, Automatización, Apps, Memoria, Ajustes, Identidad)

## Identidad del dueño (lo último añadido)
`ChatScreen` exige `IdentityGate.verifyFace()` antes de `agent.run`.
Si no es el dueño → `denyUnknownSpeaker()`. Ver `IDENTITY.md`.

## Cómo compilar
Requiere **Linux x86-64 o macOS** (aapt2 es x86-64; en Apple Silicon el
SDK trae binario arm64 nativo). En arm64 sin emulación x86 NO compila.

```
export ANDROID_HOME=/ruta/a/android-sdk
./gradlew assembleDebug      # APK de debug
./gradlew bundleRelease       # AAB (Play Store)
```

## Limitaciones conocidas (honestas)
- Reconocimiento facial *propio* no implementado; se delega en el biometric
  del sistema (más seguro). Speaker verification necesita modelo on-device
  (stub `EmbeddingVoiceAuthProvider`; conecta resemblyzer/WeNet/Nemo/Picovoice).
- LLM local es stub; conecta AiKit/llama.cpp con un modelo GGUF.
- Sin emulador en el entorno de desarrollo; probar en dispositivo físico.
- Play Store: una app de automatización vía Accessibility debe justificar
  propósito de accesibilidad o será rechazada (distribuir vía sideload).
