# Build & Run — Yino AI

Yino AI es una app Android (Kotlin + Jetpack Compose, Clean Architecture).
Este documento explica cómo compilar el APK y dejar la app **funcional** en un
dispositivo real.

## 1. Requisitos del entorno de build

- **JDK 17** (ej. `apt install openjdk-17-jdk` o Adoptium Temurin 17).
- **Android SDK** con:
  - `compileSdk` / `targetSdk` = 34
  - `build-tools;34.0.0`
  - Plataforma `android-34`
- **Gradle 8.9+** (usa el wrapper `./gradlew`; se descarga solo).
- Sistema **x86-64, macOS o Windows**. El `aapt2` que trae el SDK es binario
  x86-64; en ARM64 sin emulación x86-64 no se puede empaquetar el recurso.

> Nota: el código Kotlin de este proyecto fue validado compilando los 52
> archivos con `kotlinc 2.0.21` + plugin Compose (0 errores). El empaquetado
> final (APK/AAB) debe hacerse en una máquina con arquitectura soportada por
> aapt2.

## 2. Compilar

```bash
cd yino-android
./gradlew assembleDebug      # APK de depuración
# o
./gradlew assembleRelease    # AAB/listo para firma
```

El AGP descargará el `aapt2` correcto para tu arquitectura automáticamente.

Salida:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/bundle/release/app-release.aab`

## 3. Instalar y permisos en el dispositivo

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Al abrir la app, concede en **Ajustes del sistema**:
1. **Accesibilidad** → Yino AI (para abrir apps, tap, scroll, leer pantalla).
2. **Notificaciones** → Yino AI (para leer/responder notificaciones).

## 4. Dejar la app funcional (runtime)

### A. LLM en la nube (ruta por defecto)
En *Ajustes* de la app, pega una API key compatible con OpenAI
(`CloudLLMProvider` usa Ktor contra un endpoint OpenAI-compatible). Se guarda
**cifrada** con `EncryptedSharedPreferences` + AndroidKeyStore.

### B. LLM local (privado, sin API key)
La app habla con un servidor de inferencia local (llama.cpp/Ollama) en
`127.0.0.1:8080`. Para activarlo:
1. En *Ajustes* marca "Usar LLM local".
2. (Opcional) en "Ruta del modelo local" pon la URL del servidor
   (por defecto `http://127.0.0.1:8080`).
3. En el teléfono ejecuta un servidor GGUF, p. ej. con Termux:

   ```bash
   pkg install llama-cpp
   llama-server -m qwen2.5-0.5b-instruct-q4_k_m.gguf -c 2048 --host 127.0.0.1 --port 8080
   ```

El tráfico a `127.0.0.1` usa texto claro (ver `network_security_config.xml`);
todo lo demás va por HTTPS obligatorio.

### C. Voz on-device (STT)
`VoskSttProvider` corre 100% en el dispositivo. Descarga un modelo en español:

```bash
# En el dispositivo, dentro de Android/data/com.yino.ai/files/
wget https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip
unzip vosk-model-small-es-0.42.zip
```

La app carga `vosk-model-small-es-0.42` desde ese directorio.

### C2. Wake-word y escucha manos libres
En *Ajustes* puedes activar el wake-word "Yino" y pulsar **Activar escucha**
para lanzar `YinoVoiceService` (primer plano). El flujo on-device es:

```
"Yino" (WakeWordDetector, gramatica Vosk) -> STT (Vosk) -> Agente (ReAct) -> TTS
```

El audio nunca sale del telefono salvo que el comando lo requiera (LLM cloud,
web). El modelo de voz debe estar presente (sección 4.C).

### D. Identidad (rostro / voz)
- **Rostro**: usa la biometría del sistema (BiometricPrompt). Configúralo en
  *Identidad* de la app.
- **Voz (speaker verification)**: `EmbeddingVoiceAuthProvider` es un stub de
  embeddings; sustituir por un modelo x-vector/ECAPA real para producción.

## 5. Estructura

```
core/llm/      -> LLMProvider (cloud/local), ReAct
core/tools/     -> 10 herramientas (apps, mensajes, pantalla, web…)
core/security/ -> SecurityGate (aprobación MEDIO/ALTO), AuditLog
core/identity/ -> FaceAuthProvider, VoiceAuthProvider, IdentityGate
core/vision/   -> ScreenUnderstandingEngine
automation/     -> AccessibilityService + ActionExecutor (anti-ANR)
voice/          -> VoskSttProvider (on-device), TTS
data/memory/    -> Room
ui/             -> 7 pantallas Compose
```

## 6. Limitaciones conocidas
- Emulador: requiere `aapt2` x86-64; no corre en ARM64 sin emulación.
- LLM local in-process (GGUF cargado en la app) no está conectado; se usa
  servidor local externo (ver sección 4.B).
- Voz speaker-verification es stub; requiere modelo real para producción.
