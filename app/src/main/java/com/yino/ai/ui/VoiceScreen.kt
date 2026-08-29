package com.yino.ai.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import com.yino.ai.core.YinoGraph
import com.yino.ai.voice.AndroidTtsProvider
import com.yino.ai.voice.VoskSttProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@Composable
fun VoiceScreen(viewModel: YinoViewModel) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()
    val tts = remember { AndroidTtsProvider(context.applicationContext) }
    val modelPath = remember { YinoGraph.secure.voskModelPath }
    val vosk = remember { VoskSttProvider(context) }
    var modelReady by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Cargando modelo de voz...") }
    val pending by viewModel.pending.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
            vosk.shutdown()
        }
    }

    fun talk() {
        if (!modelReady) { status = "Modelo no disponible"; return }
        listening = true
        status = "Verificando identidad..."
        scope.launch {
            val owner = if (YinoGraph.identity.requireFace) {
                activity?.let { YinoGraph.identity.verifyFace(it) } ?: false
            } else true
            if (!owner) {
                viewModel.append("assistant", "🔒 No eres el dueño. Solo el dueño puede hablar con Yino.")
                status = "Acceso denegado"
                listening = false
                return@launch
            }
            status = "Escuchando..."
            val text = try {
                withTimeout(12_000) { vosk.listen() }
            } catch (e: Exception) {
                ""
            }
            vosk.stop()
            if (text.isBlank()) {
                status = "No entendí"
                listening = false
                return@launch
            }
            viewModel.append("user", text)
            status = "Pensando..."
            val result = try {
                viewModel.runAgent(text)
            } catch (e: Exception) {
                "Error: ${e.message ?: e.javaClass.simpleName}"
            }
            viewModel.append("assistant", result)
            tts.speak(result)
            status = "Pulsa para hablar"
            listening = false
        }
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) talk() else status = "Permiso de micrófono denegado"
    }

    fun ensurePermissionAndTalk() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) talk() else recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(Unit) {
        modelReady = withContext(Dispatchers.IO) { vosk.loadModel(modelPath) }
        status = if (modelReady) "Pulsa para hablar" else "Modelo de voz no encontrado"
    }

    if (pending != null) {
        ApprovalDialog(
            pending!!,
            onApprove = { viewModel.approve(pending!!.requestId) },
            onDeny = { viewModel.deny(pending!!.requestId) },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Entrada de voz (on-device)", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Yino escucha localmente, verifica que eres tú y responde por voz. Sin nube.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        if (!modelReady) {
            Text(
                text = "Descarga vosk-model-small-es-0.42 y colócalo en:\n$modelPath",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp),
            )
        }
        Box(contentAlignment = Alignment.Center) {
            val pulse = rememberInfiniteTransition(label = "mic")
            val scale by pulse.animateFloat(1f, 1.28f, infiniteRepeatable(tween(1400), RepeatMode.Reverse))
            val glow by pulse.animateFloat(0.55f, 0.12f, infiniteRepeatable(tween(1400), RepeatMode.Reverse))
            Box(
                Modifier.size(150.dp).scale(scale).alpha(glow)
                    .background(Brush.radialGradient(listOf(Color(0xFF00E5FF), Color.Transparent)), CircleShape),
            )
            Button(
                onClick = { ensurePermissionAndTalk() },
                modifier = Modifier.size(120.dp, 120.dp),
                enabled = !listening && modelReady,
            ) {
                Icon(Icons.Filled.Mic, contentDescription = "Hablar", modifier = Modifier.size(48.dp))
            }
        }
        Text(text = status, modifier = Modifier.padding(top = 16.dp))
    }
}
