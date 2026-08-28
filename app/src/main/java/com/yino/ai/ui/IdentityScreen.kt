package com.yino.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yino.ai.core.YinoGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun IdentityScreen(viewModel: YinoViewModel) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var status by remember { mutableStateOf("") }
    var requireFace by remember { mutableStateOf(YinoGraph.identity.requireFace) }
    var requireVoice by remember { mutableStateOf(YinoGraph.identity.requireVoice) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text("Identidad del dueño", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Yino solo obedecerá a ti. Configura los factores biométricos " +
                "que se exigen antes de ejecutar cualquier comando.",
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Exigir rostro (biometric del sistema)", modifier = Modifier.weight(1f))
            Switch(checked = requireFace, onCheckedChange = {
                requireFace = it
                YinoGraph.identity.requireFace = it
            })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Exigir voz (speaker verification)", modifier = Modifier.weight(1f))
            Switch(checked = requireVoice, onCheckedChange = {
                requireVoice = it
                YinoGraph.identity.requireVoice = it
            })
        }

        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                status = if (activity != null) {
                    val ok = YinoGraph.identity.verifyFace(activity)
                    if (ok) "✅ Rostro verificado" else "❌ No verificado"
                } else "Activa esta pantalla desde la app"
            }
        }) { Text("Probar rostro ahora") }

        Text("Estado voz: ${if (YinoGraph.identity.voiceEnrolled) "enrolada" else "no enrolada"}")
        Text(
            "El enrollment de voz requiere grabar frases y un modelo de embeddings " +
                "on-device (resemblyzer/WeNet/Nemo o Picovoice). Stub en build de dev.",
        )

        Text(status)
    }
}
