# Graph Report - Opencode-Yino-AI  (2026-09-01)

## Corpus Check
- 83 files · ~38,911 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 671 nodes · 1348 edges · 47 communities (43 shown, 4 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 57 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `fd8b9b1a`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- YinoViewModel
- SecurityGate
- MemoryRepositoryTest
- YinoVoiceService
- YinoAccessibilityService
- YinoGraph
- VoskSttProvider
- ToolResult
- VoskPassphraseAuth
- AndroidSttProvider.kt
- CloudLLMProvider
- YinoNotificationListener
- YinoGraph.kt
- BiometricPrompt
- LocalLLMProvider
- Intent
- ToolRegistryTest
- Build & Run — Yino AI
- ActionRisk
- SendMessageTool
- Configuración de Yino AI (pasos en el teléfono)
- ReadScreenTool.kt
- Yino AI — Seguridad
- AuditLog
- AudioRecorder
- OpenAppTool.kt
- Yino AI
- NotificationReplyTool.kt
- BackTool.kt
- CallTool.kt
- GoHomeTool.kt
- PlayMusicTool.kt
- ReadNotificationsTool.kt
- ScrollTool.kt
- SendEmailTool.kt
- TapTool.kt
- WebSearchTool.kt
- SettingsActivity.kt
- Yino AI — Autenticación del dueño (Identidad)
- ContactsHelper.kt
- CalendarIntegration.kt
- YinoApplication
- gradlew
- Migrations.kt

## God Nodes (most connected - your core abstractions)
1. `ToolResult` - 57 edges
2. `ToolContext` - 56 edges
3. `YinoAccessibilityService` - 30 edges
4. `SecurityGate` - 30 edges
5. `YinoGraph` - 26 edges
6. `ActionRisk` - 25 edges
7. `ToolRegistry` - 23 edges
8. `CloudLLMProvider` - 19 edges
9. `YinoViewModel` - 19 edges
10. `YinoVoiceService` - 19 edges

## Surprising Connections (you probably didn't know these)
- `ApprovalDialog()` --calls--> `Text`  [INFERRED]
  app/src/main/java/com/yino/ai/ui/ApprovalDialog.kt → app/src/main/java/com/yino/ai/core/llm/LLMProvider.kt
- `VoiceScreen()` --calls--> `Text`  [INFERRED]
  app/src/main/java/com/yino/ai/ui/VoiceScreen.kt → app/src/main/java/com/yino/ai/core/llm/LLMProvider.kt
- `ChatScreen()` --calls--> `ApprovalDialog()`  [INFERRED]
  app/src/main/java/com/yino/ai/ui/ChatScreen.kt → app/src/main/java/com/yino/ai/ui/ApprovalDialog.kt
- `VoiceScreen()` --calls--> `ApprovalDialog()`  [INFERRED]
  app/src/main/java/com/yino/ai/ui/VoiceScreen.kt → app/src/main/java/com/yino/ai/ui/ApprovalDialog.kt
- `YinoApp()` --calls--> `VoiceScreen()`  [INFERRED]
  app/src/main/java/com/yino/ai/ui/YinoApp.kt → app/src/main/java/com/yino/ai/ui/VoiceScreen.kt

## Import Cycles
- None detected.

## Communities (47 total, 4 thin omitted)

### Community 0 - "YinoViewModel"
Cohesion: 0.06
Nodes (57): Text, AppItems, AppsScreen(), SuggestedApp, AutomationScreen(), ChatScreen(), doSend(), FragmentActivity (+49 more)

### Community 1 - "SecurityGate"
Cohesion: 0.06
Nodes (32): AgentLoop, ChatMessage, LLMProvider, LLMRequest, LLMResult, Role, ASSISTANT, SYSTEM (+24 more)

### Community 2 - "MemoryRepositoryTest"
Cohesion: 0.06
Nodes (10): ConversationEntity, MemoryDao, Context, MemoryDatabase, MemoryRepository, MemoryStore, MessageEntity, InMemoryMemoryDao (+2 more)

### Community 3 - "YinoVoiceService"
Cohesion: 0.07
Nodes (14): VoiceScreen(), AndroidTtsProvider, TTSProvider, RecognitionListener, SpeechService, WakeWordDetector, RecognitionListener, YinoVoiceService (+6 more)

### Community 4 - "YinoAccessibilityService"
Cohesion: 0.08
Nodes (15): AccessibilityEvent, ActionExecutor, AccessibilityService, Stroke, ClickNode, Global, Swipe, Tap (+7 more)

### Community 5 - "YinoGraph"
Cohesion: 0.12
Nodes (11): IdentityGate, android, FloatArray, FragmentActivity, SecureSettings, Context, LLMProvider, YinoGraph (+3 more)

### Community 6 - "VoskSttProvider"
Cohesion: 0.12
Nodes (8): Context, FloatArray, VoskPassphraseVoiceAuth, RecognitionListener, SpeechService, VoskSttProvider, RecognitionListener, Model

### Community 7 - "ToolResult"
Cohesion: 0.20
Nodes (6): JSONObject, ToolContext, ToolResult, org, org, Tool

### Community 8 - "VoskPassphraseAuth"
Cohesion: 0.19
Nodes (7): EmbeddingVoiceAuthProvider, Context, FloatArray, VoiceAuthProvider, Context, FloatArray, VoskPassphraseAuth

### Community 9 - "AndroidSttProvider.kt"
Cohesion: 0.12
Nodes (10): AndroidSttProvider, Bundle, onBufferReceived(), onEvent(), onPartialResults(), onReadyForSpeech(), onResults(), STTProvider (+2 more)

### Community 10 - "CloudLLMProvider"
Cohesion: 0.14
Nodes (16): Choice, CloudLLMProvider, Fun, LLMProvider, Msg, Req, Resp, RespMsg (+8 more)

### Community 11 - "YinoNotificationListener"
Cohesion: 0.22
Nodes (6): InstanceHolder, Notice, ReplyTarget, YinoNotificationListener, NotificationListenerService, StatusBarNotification

### Community 12 - "YinoGraph.kt"
Cohesion: 0.36
Nodes (11): AddCalendarEventTool, Tool, OpenUrlTool, SetAlarmTool, SetTimerTool, SetVolumeTool, TakePhotoTool, Tool (+3 more)

### Community 13 - "BiometricPrompt"
Cohesion: 0.24
Nodes (5): FaceAuthProvider, Context, FragmentActivity, SystemBiometricFaceAuth, BiometricPrompt

### Community 14 - "LocalLLMProvider"
Cohesion: 0.22
Nodes (11): Choice, Fun, LLMProvider, LocalLLMProvider, Msg, Req, Resp, RespMsg (+3 more)

### Community 15 - "Intent"
Cohesion: 0.26
Nodes (4): JSONObject, Context, WhatsAppIntegration, Intent

### Community 16 - "ToolRegistryTest"
Cohesion: 0.23
Nodes (7): Tool, ToolRegistryTest, Tool, Tool, Tool, Tool, Tool

### Community 17 - "Build & Run — Yino AI"
Cohesion: 0.17
Nodes (12): 1. Requisitos del entorno de build, 2. Compilar, 3. Instalar y permisos en el dispositivo, 4. Dejar la app funcional (runtime), 5. Estructura, 6. Limitaciones conocidas, A. LLM en la nube (ruta por defecto), B. LLM local (privado, sin API key) (+4 more)

### Community 18 - "ActionRisk"
Cohesion: 0.22
Nodes (6): ActionRisk, HIGH, LOW, MEDIUM, JSONObject, Tool

### Community 19 - "SendMessageTool"
Cohesion: 0.35
Nodes (3): JSONObject, Tool, SendMessageTool

### Community 20 - "Configuración de Yino AI (pasos en el teléfono)"
Cohesion: 0.18
Nodes (10): 1. Descargar e instalar el APK, 2. Configurar el LLM (OBLIGATORIO, si no lo haces el asistente no responde), 3. Modelo de voz (solo para el modo Voz / manos libres), 4. Permisos (para automatización real), 5. Usarlo, Configuración de Yino AI (pasos en el teléfono), Ejemplo concreto: Groq (gratis, sin PC), Notas (+2 more)

### Community 21 - "ReadScreenTool.kt"
Cohesion: 0.31
Nodes (5): JSONObject, Tool, ReadScreenTool, AccessibilityNodeInfo, ScreenUnderstandingEngine

### Community 22 - "Yino AI — Seguridad"
Cohesion: 0.22
Nodes (8): 1. Identidad del dueño (anti-suplantación), 2. Secretos cifrados, 3. Autorización por riesgo, 4. Auditoría, 5. Privacidad / on-device, 6. Permisos mínimos, 7. Integridad, Yino AI — Seguridad

### Community 23 - "AuditLog"
Cohesion: 0.39
Nodes (3): AuditLog, Entry, android

### Community 24 - "AudioRecorder"
Cohesion: 0.46
Nodes (4): AudioRecorder, FloatArray, AudioRecord, Channel

### Community 25 - "OpenAppTool.kt"
Cohesion: 0.48
Nodes (4): Context, JSONObject, Tool, OpenAppTool

### Community 26 - "Yino AI"
Cohesion: 0.29
Nodes (6): Cómo compilar, Identidad del dueño (lo último añadido), Limitaciones conocidas (honestas), Módulos, Stack, Yino AI

### Community 27 - "NotificationReplyTool.kt"
Cohesion: 0.53
Nodes (3): JSONObject, Tool, NotificationReplyTool

### Community 28 - "BackTool.kt"
Cohesion: 0.60
Nodes (3): BackTool, JSONObject, Tool

### Community 29 - "CallTool.kt"
Cohesion: 0.60
Nodes (3): CallTool, JSONObject, Tool

### Community 30 - "GoHomeTool.kt"
Cohesion: 0.60
Nodes (3): GoHomeTool, JSONObject, Tool

### Community 31 - "PlayMusicTool.kt"
Cohesion: 0.60
Nodes (3): JSONObject, Tool, PlayMusicTool

### Community 32 - "ReadNotificationsTool.kt"
Cohesion: 0.60
Nodes (3): JSONObject, Tool, ReadNotificationsTool

### Community 33 - "ScrollTool.kt"
Cohesion: 0.60
Nodes (3): JSONObject, Tool, ScrollTool

### Community 34 - "SendEmailTool.kt"
Cohesion: 0.60
Nodes (3): JSONObject, Tool, SendEmailTool

### Community 35 - "TapTool.kt"
Cohesion: 0.60
Nodes (3): JSONObject, Tool, TapTool

### Community 36 - "WebSearchTool.kt"
Cohesion: 0.60
Nodes (3): JSONObject, Tool, WebSearchTool

### Community 37 - "SettingsActivity.kt"
Cohesion: 0.60
Nodes (3): Bundle, SettingsActivity, AppCompatActivity

### Community 38 - "Yino AI — Autenticación del dueño (Identidad)"
Cohesion: 0.40
Nodes (4): Enrollment, Flujo, Limitaciones (honestas), Yino AI — Autenticación del dueño (Identidad)

### Community 42 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **68 isolated node(s):** `Tap`, `Swipe`, `Global`, `ClickNode`, `TypeText` (+63 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `YinoGraph` connect `YinoGraph` to `YinoViewModel`, `SecurityGate`, `MemoryRepositoryTest`, `YinoVoiceService`, `YinoGraph.kt`?**
  _High betweenness centrality (0.161) - this node is a cross-community bridge._
- **Why does `YinoAccessibilityService` connect `YinoAccessibilityService` to `YinoViewModel`, `ScrollTool.kt`, `TapTool.kt`, `YinoVoiceService`, `YinoGraph.kt`, `SendMessageTool`, `ReadScreenTool.kt`, `BackTool.kt`, `GoHomeTool.kt`?**
  _High betweenness centrality (0.123) - this node is a cross-community bridge._
- **Why does `MemoryRepository` connect `MemoryRepositoryTest` to `YinoGraph.kt`, `YinoGraph`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Are the 8 inferred relationships involving `SecurityGate` (e.g. with `.`approval resolves pending deferred`()` and `.`deny resolves pending deferred with false`()`) actually correct?**
  _`SecurityGate` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Tap`, `Swipe`, `Global` to the rest of the system?**
  _68 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `YinoViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.0625694187338023 - nodes in this community are weakly interconnected._
- **Should `SecurityGate` be split into smaller, more focused modules?**
  _Cohesion score 0.060455486542443065 - nodes in this community are weakly interconnected._