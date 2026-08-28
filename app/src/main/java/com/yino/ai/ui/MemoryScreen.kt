package com.yino.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yino.ai.core.security.AuditLog

@Composable
fun MemoryScreen(viewModel: YinoViewModel) {
    val entries = remember { AuditLog.all() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Registro de auditoría", style = MaterialTheme.typography.headlineSmall)
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(entries) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(entry.toolId, style = MaterialTheme.typography.titleMedium)
                        Text("Riesgo: ${entry.risk} · ${if (entry.approved) "Aprobado" else "Denegado"}")
                        Text(entry.result)
                        Text(entry.ts.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
