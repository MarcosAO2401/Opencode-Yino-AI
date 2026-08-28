package com.yino.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import com.yino.ai.core.YinoGraph
import com.yino.ai.voice.AndroidTtsProvider
import com.yino.ai.voice.VoskSttProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VoiceScreen(viewModel: YinoViewModel) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()
    val tts = remember { AndroidTtsProvider(context.applicationContext) }
    val modelPath = remember {
        "${context.getExternalFilesDir(null)?.absolutePath}/vosk-model-small-es-0.42"
    }
    val vosk = remember { VoskSttProvider(context) }
    var modelReady by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Cargando modelo de voz...") }
    LaunchedEffect(Unit) {
        modelReady = withContext(Dispatchers.IO) { vosk.loadModel(modelPath) }
        status = if (modelReady) "Pulsa para hablar" else "Modelo de voz no encontrado"
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
        Button(
            onClick = {
                if (!modelReady) { status = "Modelo no disponible"; return@Button }
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
                    val text = vosk.listen()
                    vosk.stop()
                    if (text.isBlank()) {
                        status = "No entendí"
                        listening = false
                        return@launch
                    }
                    viewModel.append("user", text)
                    status = "Pensando..."
                    val result = viewModel.runAgent(text)
                    viewModel.append("assistant", result)
                    tts.speak(result)
                    status = "Pulsa para hablar"
                    listening = false
                }
            },
            modifier = Modifier.size(120.dp, 120.dp),
            enabled = !listening && modelReady,
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "Hablar", modifier = Modifier.size(48.dp))
        }
        Text(text = status, modifier = Modifier.padding(top = 16.dp))
    }
}
