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
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.compose.runtime.rememberCoroutineScope
import com.yino.ai.core.YinoGraph
import com.yino.ai.voice.YinoVoiceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(viewModel: YinoViewModel) {
    val context = LocalContext.current
    var localLlm by remember { mutableStateOf(YinoGraph.secure.useLocalLlm) }
    var apiKey by remember { mutableStateOf(YinoGraph.secure.apiKey) }
    var baseUrl by remember { mutableStateOf(YinoGraph.secure.llmBaseUrl) }
    var cloudModel by remember { mutableStateOf(YinoGraph.secure.llmModel) }
    var modelPath by remember { mutableStateOf(YinoGraph.secure.localModelPath) }
    var localModel by remember { mutableStateOf(YinoGraph.secure.localModelName) }
    var wakeWord by remember { mutableStateOf(YinoGraph.secure.wakeWordEnabled) }
    var listening by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var voskStatus by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val voskPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        scope.launch(Dispatchers.IO) {
            try {
                val dest = File(context.getExternalFilesDir(null), "vosk-model-small-es-0.42")
                copyUriTree(context, uri, dest)
                val ok = File(dest, "am").isDirectory || File(dest, "conf").isDirectory
                if (!ok) {
                    voskStatus = "La carpeta elegida no parece un modelo Vosk " +
                        "(debe contener 'am' y 'conf'). Elige la carpeta vosk-model-small-es-0.42."
                } else {
                    YinoGraph.secure.voskModelPath = dest.absolutePath
                    voskStatus = "Modelo de voz copiado a ${dest.absolutePath}"
                }
            } catch (e: Exception) {
                voskStatus = "Error al copiar el modelo: ${e.message}. " +
                    "Intenta mover la carpeta manualmente a Android/data/com.yino.ai/files/."
            }
        }
    }

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
                label = { Text("URL base del LLM local (OpenAI-compatible)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = localModel,
                onValueChange = { localModel = it },
                label = { Text("Nombre del modelo local (p. ej. llama3)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = {
                YinoGraph.setLocalModelPath(modelPath)
                YinoGraph.setLocalModelName(localModel)
                saved = true
            }) { Text("Guardar servidor local") }
            Text(
                "El LLM local habla con un servidor en el propio telefono (Ollama en " +
                    "Termux: http://127.0.0.1:11434/v1/chat/completions). No usa internet ni API key.",
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

        Button(onClick = { voskPicker.launch(null) }) {
            Text("Seleccionar carpeta del modelo Vosk")
        }
        Text(
            "Elige la carpeta 'vosk-model-small-es-0.42' (p. ej. donde la descomprimiste). " +
                "La app la copia sola a su almacenamiento. No necesitas mover archivos manualmente.",
            style = MaterialTheme.typography.bodySmall,
        )
        if (voskStatus.isNotBlank()) {
            Text(voskStatus, style = MaterialTheme.typography.bodySmall)
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

private fun copyUriTree(context: Context, treeUri: android.net.Uri, dest: File) {
    dest.mkdirs()
    val tree = DocumentFile.fromTreeUri(context, treeUri) ?: return
    copyDoc(context, tree, dest)
}

private fun copyDoc(context: Context, doc: DocumentFile, dest: File) {
    if (doc.isDirectory) {
        dest.mkdirs()
        doc.listFiles().forEach { child ->
            val name = child.name ?: return@forEach
            copyDoc(context, child, File(dest, name))
        }
    } else {
        val name = doc.name ?: return
        context.contentResolver.openInputStream(doc.uri)?.use { input ->
            File(dest, name).outputStream().use { out -> input.copyTo(out) }
        }
    }
}
