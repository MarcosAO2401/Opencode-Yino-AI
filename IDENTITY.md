# Yino AI — Autenticación del dueño (Identidad)

Yino solo obedece a su dueño. Esto se implementa en `core/identity/`:

- **`FaceAuthProvider`** → `SystemBiometricFaceAuth`: usa `BiometricPrompt` del
  sistema (face-unlock/huella ya registrados en el dispositivo, protegidos por
  TEE). No almacena imágenes. Es el método seguro y oficial.
- **`VoiceAuthProvider`** → `EmbeddingVoiceAuthProvider`: speaker verification
  **on-device** por embeddings de voz + similitud de coseno. Android no trae
  esta API; el stub define la interfaz y la lógica. Conecta un modelo real
  (resemblyzer / WeNet / Nemo SpeakerVerif / Picovoice SpeakerRec).
- **`IdentityGate`**: combina ambos factores. `verifyOwner()` debe pasar antes
  de ejecutar cualquier comando. Flags `requireFace` / `requireVoice`.

## Flujo
1. En `ChatScreen`, al pulsar Enviar se llama `YinoGraph.identity.verifyFace(activity)`.
2. Si no es el dueño → `YinoViewModel.denyUnknownSpeaker()` informa y no ejecuta.
3. Si pasa → `agent.run(text)`.
4. Herramientas de riesgo MEDIO/ALTO pasan además por `SecurityGate` (aprobación).

## Enrollment
- Rostro: ya enrolado en el sistema (ajustes del teléfono).
- Voz: pantalla **Identidad** → grabar frases → `EmbeddingVoiceAuthProvider.enroll()`.
  Requiere modelo de embeddings on-device (stub en build de dev).

## Limitaciones (honestas)
- Reconocimiento facial *propio* de Yino no se implementa; se delega en el
  biometric del sistema (más seguro, resistente a spoofing).
- Speaker verification necesita un modelo; sin él, el factor voz está desactivado
  por defecto (`requireVoice = false`).
- En emulador sin hardware biometric, `verifyFace` pedirá credencial del dispositivo.
