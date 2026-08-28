package com.yino.ai.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.yino.ai.automation.YinoAccessibilityService

@Composable
fun AutomationScreen(viewModel: YinoViewModel) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(YinoAccessibilityService.isEnabled()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Automatización", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = if (enabled) {
                "Servicio de accesibilidad Yino: ACTIVADO"
            } else {
                "Servicio de accesibilidad Yino: DESACTIVADO"
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
        ) {
            Text("Abrir ajustes de accesibilidad")
        }
    }
}
