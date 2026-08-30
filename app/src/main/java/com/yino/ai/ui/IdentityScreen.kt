package com.yino.ai.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yino.ai.core.YinoGraph
import com.yino.ai.voice.AudioRecorder
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
    var isRecording by remember { mutableStateOf(false) }
    var recordingProgress by remember { mutableStateOf("") }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) status = "Permiso de micrófono denegado"
    }

    fun ensurePermissionAndRecord(action: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) action() else recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val startEnrollment = remember {
        { 
            isRecording = true
            status = "Iniciando enrollment..."
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val recorder = AudioRecorder()
                    val samples = mutableListOf<FloatArray>()
                    val phrases = listOf("Yino desbloquear", "Hola Yino", "Yino escucha")
                    
                    for ((i, phrase) in phrases.withIndex()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            recordingProgress = "Di: \"$phrase\" ($${i+1}/3)"
                        }
                        val chunks = recorder.recordFixedDuration(3000)
                        samples.addAll(chunks)
                        Thread.sleep(500)
                    }
                    
                    recorder.stop()
                    CoroutineScope(Dispatchers.Main).launch {
                        status = "Procesando enrollment..."
                    }
                    
                    val ok = YinoGraph.identity.enrollVoice(context, samples)
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        isRecording = false
                        recordingProgress = ""
                        if (ok) {
                            status = "✅ Voz enrolada correctamente"
                            requireVoice = true
                            YinoGraph.identity.requireVoice = true
                        } else {
                            status = "❌ Falló el enrollment. Intenta de nuevo."
                        }
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        isRecording = false
                        recordingProgress = ""
                        status = "❌ Error: ${e.message ?: e.javaClass.simpleName}"
                    }
                }
            }
        }
    }

    val startVerification = remember {
        { 
            isRecording = true
            status = "Grabando para verificación..."
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val recorder = AudioRecorder()
                    val chunks = recorder.recordFixedDuration(3000)
                    recorder.stop()
                    
                    val ok = YinoGraph.identity.verifyVoice(context, chunks.flattenToList())
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        isRecording = false
                        recordingProgress = ""
                        status = if (ok) "✅ Voz verificada" else "❌ Voz no coincide"
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        isRecording = false
                        recordingProgress = ""
                        status = "❌ Error: ${e.message ?: e.javaClass.simpleName}"
                    }
                }
            }
        }
    }

    val startReEnrollment = remember {
        { 
            isRecording = true
            status = "Regrabando voz..."
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val recorder = AudioRecorder()
                    val samples = mutableListOf<FloatArray>()
                    val phrases = listOf("Yino desbloquear", "Hola Yino", "Yino escucha")
                    
                    for ((i, phrase) in phrases.withIndex()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            recordingProgress = "Di: \"$phrase\" ($${i+1}/3)"
                        }
                        val chunks = recorder.recordFixedDuration(3000)
                        samples.addAll(chunks)
                        Thread.sleep(500)
                    }
                    
                    recorder.stop()
                    CoroutineScope(Dispatchers.Main).launch {
                        status = "Procesando re-enrollment..."
                    }
                    
                    val ok = YinoGraph.identity.enrollVoice(context, samples)
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        isRecording = false
                        recordingProgress = ""
                        if (ok) {
                            status = "✅ Voz re-enrolada correctamente"
                        } else {
                            status = "❌ Falló el re-enrollment. Intenta de nuevo."
                        }
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        isRecording = false
                        recordingProgress = ""
                        status = "❌ Error: ${e.message ?: e.javaClass.simpleName}"
                    }
                }
            }
        }
    }

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
            Switch(
                checked = requireVoice,
                enabled = YinoGraph.identity.voiceEnrolled,
                onCheckedChange = {
                    requireVoice = it
                    YinoGraph.identity.requireVoice = it
                },
            )
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

        if (!YinoGraph.identity.voiceEnrolled) {
            Button(
                onClick = {
                    ensurePermissionAndRecord { startEnrollment() }
                },
                enabled = !isRecording,
            ) {
                Text(if (isRecording) "Grabando frase 1/3... $recordingProgress" else "Enrolar voz (grabar frase)")
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        ensurePermissionAndRecord { startVerification() }
                    },
                    enabled = !isRecording,
                ) {
                    Text(if (isRecording) "Verificando... $recordingProgress" else "Verificar voz ahora")
                }
                Button(
                    onClick = {
                        ensurePermissionAndRecord { startReEnrollment() }
                    },
                    enabled = !isRecording,
                ) {
                    Text(if (isRecording) "Regrabando... $recordingProgress" else "Regrabar voz")
                }
            }
        }

        Text(
            "El enrollment de voz graba una frase y la verifica on-device con Vosk " +
                "(reconocimiento de habla + frases objetivo). No usa embeddings de speaker.",
        )

        Text(status, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun List<FloatArray>.flattenToList(): FloatArray {
    val totalSize = sumOf { it.size }
    val result = FloatArray(totalSize)
    var index = 0
    for (arr in this) {
        System.arraycopy(arr, 0, result, index, arr.size)
        index += arr.size
    }
    return result
}