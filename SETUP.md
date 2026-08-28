# Configuración de Yino AI (pasos en el teléfono)

La app ya compila y el APK está listo en GitHub Actions. Estos son los pasos
**obligatorios en tu teléfono** para que Yino funcione. No hace falta tocar código.

## 1. Descargar e instalar el APK
- En GitHub, pestaña **Actions** → workflow **Build Debug APK** → run más reciente (verde).
- Sección **Artifacts** → descarga **`yino-debug-apk`** (es un zip).
- Descomprime y pasa el `.apk` al teléfono. Ábrelo y pulsa **Instalar**.
  (Es "fuente desconocida"; aptívado "Orígenes desconocidos" si el sistema lo pide.)

## 2. Configurar el LLM (OBLIGATORIO, si no lo haces el asistente no responde)
Yino necesita un modelo de lenguaje. Tienes dos opciones:

### Opción A — API en la nube (la más simple)
- Abre Yino → pestaña **Ajustes**.
- Rellena **API Key**, **URL base del LLM** y **Modelo**:
  - Por defecto: OpenAI (`https://api.openai.com/v1/chat/completions`, `gpt-4o-mini`).
  - Proveedores **gratuitos OpenAI-compatible** (sin cambiar código): pon su URL y
    modelo, p. ej. DeepSeek (`https://api.deepseek.com/v1/chat/completions`,
    `deepseek-chat`), Groq, Together, OpenRouter, etc.
- Pulsa **Guardar config del LLM (cifrada)**.
- Listo: ya puedes hablar/escribir con Yino.

#### Ejemplo concreto: Groq (gratis, sin PC)
- **API Key:** la que generas en groq.com (empieza por `gsk_`).
- **URL base del LLM:** `https://api.groq.com/openai/v1/chat/completions`
- **Modelo:** `llama-3.3-70b-versatile` (o `llama-3.1-8b-instant` para menor latencia).
Groq es OpenAI-compatible, así que funciona tal cual. Solo necesitas internet.

### Opción B — LLM local en el propio teléfono (privado, sin nube, sin PC)
Funciona si tienes un servidor de inferencia **en el celular**, p. ej. Ollama en
Termux. La app habla con `127.0.0.1`, así que el servidor debe correr en el
mismo teléfono (no en otro equipe).

Pasos en el teléfono:
1. En Termux deja Ollama corriendo y descarga un modelo, p. ej.:
   `ollama pull llama3` (el servidor queda en `127.0.0.1:11434`).
2. En Yino → **Ajustes** → activa **Usar LLM local** y pon:
   - **URL base del LLM local:** `http://127.0.0.1:11434/v1/chat/completions`
   - **Nombre del modelo local:** `llama3` (el que hayas bajado).
3. **Guardar servidor local**.
4. Ya funciona **sin internet ni API key**. El texto nunca sale del teléfono.

Nota: la app es OpenAI-compatible, así que también sirve llama.cpp en modo
servidor OpenAI (`http://127.0.0.1:8080/v1/chat/completions`).

## 3. Modelo de voz (solo para el modo Voz / manos libres)
Descarga **`vosk-model-small-es-0.42`** (~40 MB, NO el de 1.4 GB) desde
https://alphacephei.com/vosk/models y descomprímelo en el teléfono.

Forma más fácil (la app lo copia sola):
- En Yino → **Ajustes** → **Seleccionar carpeta del modelo Vosk**.
- Eliges la carpeta `vosk-model-small-es-0.42` que descomprimiste.
- La app la copia a su propio almacenamiento. Listo, no muevas archivos a mano.

Forma manual (alternativa): mueve la carpeta a
`Android/data/com.yino.ai/files/vosk-model-small-es-0.42`.
- Si no pones el modelo, la pantalla **Voz** avisará "Modelo no encontrado"
  (el resto de la app sí funciona).

## 4. Permisos (para automatización real)
- **Accesibilidad**: Ajustes del sistema → Accesibilidad → Yino AI → Activar.
  (Esto permite leer la pantalla y tocar por ti.)
- **Notificaciones** (opcional): Ajustes → Apps → Yino AI → Notificaciones.
- **Micrófono / Notificaciones**: concédelos al instalar o en Ajustes.
- **Biometría del sistema**: ten huella o rostro enrolado (Yino lo usa para
  saber que eres tú antes de obedecer).

## 5. Usarlo
- **Chat**: escribe o pulsa micrófono. Yino pedirá tu rostro/huella y luego
  aprobará acciones sensibles en pantalla.
- **Voz**: activa el servicio en Ajustes ("Iniciar servicio de voz") y di
  **"Yino"** para dar órdenes manos libres.

## Notas
- Las acciones de riesgo MEDIO/ALTO (ej. tocar la pantalla, enviar mensaje)
  requieren tu aprobación en pantalla. En modo manos libres sin pantalla se
  deniegan solas por seguridad.
- Todo el audio y la verificación de identidad se procesan en el dispositivo.
