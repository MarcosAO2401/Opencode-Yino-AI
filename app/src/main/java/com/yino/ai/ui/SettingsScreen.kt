package com.yino.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import android.content.Intent
import com.yino.ai.core.YinoGraph
import com.yino.ai.voice.YinoVoiceService

@Composable
fun SettingsScreen(viewModel: YinoViewModel) {
    val context = LocalContext.current
    var localLlm by remember { mutableStateOf(YinoGraph.secure.useLocalLlm) }
    var apiKey by remember { mutableStateOf(YinoGraph.secure.apiKey) }
    var baseUrl by remember { mutableStateOf(YinoGraph.secure.llmBaseUrl) }
    var cloudModel by remember { mutableStateOf(YinoGraph.secure.llmModel) }
    var modelPath by remember { mutableStateOf(YinoGraph.secure.localModelPath) }
    var wakeWord by remember { mutableStateOf(YinoGraph.secure.wakeWordEnabled) }
    var listening by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineSmall)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Usar LLM local (sin API key, privado)")
            Switch(checked = localLlm, onCheckedChange = {
                localLlm = it
                YinoGraph.setUseLocalLlm(it)
                saved = true
            })
        }

        if (!localLlm) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key (nube)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("URL base del LLM (OpenAI-compatible)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = cloudModel,
                onValueChange = { cloudModel = it },
                label = { Text("Modelo (p. ej. gpt-4o-mini)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = {
                YinoGraph.setApiKey(apiKey)
                YinoGraph.setLlmBaseUrl(baseUrl)
                YinoGraph.setLlmModel(cloudModel)
                saved = true
            }) { Text("Guardar config del LLM (cifrada)") }
            Text(
                "Compatible con OpenAI y proveedores OpenAI-compatible " +
                    "(DeepSeek, Groq, Together, OpenRouter, etc.). Cambia la URL y el modelo.",
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            OutlinedTextField(
                value = modelPath,
                onValueChange = { modelPath = it },
                label = { Text("URL del servidor LLM local") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = {
                YinoGraph.setLocalModelPath(modelPath)
                saved = true
            }) { Text("Guardar servidor local") }
            Text(
                "El LLM local habla con un servidor de inferencia en el dispositivo " +
                    "(llama.cpp/Ollama en 127.0.0.1:8080). Ver BUILD.md.",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Wake-word \"Yino\" (manos libres)")
            Switch(checked = wakeWord, onCheckedChange = {
                wakeWord = it
                YinoGraph.secure.wakeWordEnabled = it
            })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = {
                context.startForegroundService(Intent(context, YinoVoiceService::class.java))
                listening = true
            }) { Text("Activar escucha") }
            Button(onClick = {
                context.stopService(Intent(context, YinoVoiceService::class.java))
                listening = false
            }) { Text("Detener escucha") }
        }
        Text(
            if (listening) "Yino está escuchando en segundo plano. Di \"Yino\" para hablarle."
            else "Inicia la escucha para usar Yino sin tocar la pantalla.",
            style = MaterialTheme.typography.bodySmall,
        )

        if (saved) {
            Text("Guardado ✓ (cifrado en disco vía AndroidKeyStore)", style = MaterialTheme.typography.bodySmall)
        }
    }
}
