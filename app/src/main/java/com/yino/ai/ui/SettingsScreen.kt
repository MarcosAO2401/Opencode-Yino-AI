package com.yino.ai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.yino.ai.core.YinoGraph

@Composable
fun SettingsScreen(viewModel: YinoViewModel) {
    val context = LocalContext.current
    var localLlm by remember { mutableStateOf(YinoGraph.secure.useLocalLlm) }
    var nemotron by remember { mutableStateOf(YinoGraph.secure.useNvidiaNemotron) }
    var apiKey by remember { mutableStateOf(YinoGraph.secure.apiKey) }
    var baseUrl by remember { mutableStateOf(YinoGraph.secure.llmBaseUrl) }
    var cloudModel by remember { mutableStateOf(YinoGraph.secure.llmModel) }
    var nvidiaApiKey by remember { mutableStateOf(YinoGraph.secure.nvidiaApiKey) }
    var nvidiaBaseUrl by remember { mutableStateOf(YinoGraph.secure.nvidiaBaseUrl) }
    var nvidiaModel by remember { mutableStateOf(YinoGraph.secure.nvidiaModel) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineSmall, color = Color.Cyan)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Usar LLM local", color = Color.Cyan)
            Switch(checked = localLlm, onCheckedChange = {
                localLlm = it
                if (it) {
                    nemotron = false
                    YinoGraph.setNvidiaNemotronEnabled(false)
                }
                YinoGraph.setUseLocalLlm(it)
            })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text("NVIDIA Nemotron", color = Color.Cyan)
                Text("Cerebro cloud para tareas agenticas", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = nemotron, onCheckedChange = {
                nemotron = it
                if (it) localLlm = false
                YinoGraph.setNvidiaNemotronEnabled(it)
            })
        }

        if (nemotron) {
            OutlinedTextField(
                value = nvidiaApiKey,
                onValueChange = { nvidiaApiKey = it },
                label = { Text("NVIDIA API Key", color = Color.Cyan) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = nvidiaBaseUrl,
                onValueChange = { nvidiaBaseUrl = it },
                label = { Text("URL NVIDIA NIM", color = Color.Cyan) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = nvidiaModel,
                onValueChange = { nvidiaModel = it },
                label = { Text("Modelo Nemotron", color = Color.Cyan) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = {
                YinoGraph.setNvidiaApiKey(nvidiaApiKey.trim())
                YinoGraph.setNvidiaBaseUrl(nvidiaBaseUrl.trim())
                YinoGraph.setNvidiaModel(nvidiaModel.trim())
                android.widget.Toast.makeText(context, "Nemotron configurado", android.widget.Toast.LENGTH_SHORT).show()
            }) { Text("Guardar Nemotron", color = Color.Cyan) }
        } else if (!localLlm) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key", color = Color.Cyan) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("URL base", color = Color.Cyan) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = cloudModel,
                onValueChange = { cloudModel = it },
                label = { Text("Modelo", color = Color.Cyan) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = {
                YinoGraph.setApiKey(apiKey.trim())
                YinoGraph.setLlmBaseUrl(baseUrl.trim())
                YinoGraph.setLlmModel(cloudModel.trim())
                android.widget.Toast.makeText(context, "Configuración guardada", android.widget.Toast.LENGTH_SHORT).show()
            }) { Text("Guardar configuración", color = Color.Cyan) }
        }

        Text(
            "Nemotron: ${if (nemotron) "ACTIVO" else "inactivo"} · LLM local: ${if (localLlm) "ACTIVO" else "inactivo"}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Cyan,
        )
        Text("Ruta Vosk: ${YinoGraph.secure.voskModelPath}", style = MaterialTheme.typography.bodySmall, color = Color.Cyan)
    }
}
