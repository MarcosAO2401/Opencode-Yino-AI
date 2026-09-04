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
    var apiKey by remember { mutableStateOf(YinoGraph.secure.apiKey) }
    var baseUrl by remember { mutableStateOf(YinoGraph.secure.llmBaseUrl) }
    var cloudModel by remember { mutableStateOf(YinoGraph.secure.llmModel) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.headlineSmall, color = Color.Cyan)

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Usar LLM local", color = Color.Cyan)
            Switch(checked = localLlm, onCheckedChange = { localLlm = it; YinoGraph.setUseLocalLlm(it) })
        }

        if (!localLlm) {
            OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key", color = Color.Cyan) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, label = { Text("URL base", color = Color.Cyan) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cloudModel, onValueChange = { cloudModel = it }, label = { Text("Modelo", color = Color.Cyan) }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                YinoGraph.setApiKey(apiKey.trim())
                YinoGraph.setLlmBaseUrl(baseUrl.trim())
                YinoGraph.setLlmModel(cloudModel.trim())
                android.widget.Toast.makeText(context, "Configuración guardada", android.widget.Toast.LENGTH_SHORT).show()
            }) { Text("Guardar configuración", color = Color.Cyan) }
        }
    }
}
