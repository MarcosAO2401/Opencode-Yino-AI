package com.yino.ai.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.yino.ai.core.security.SecurityGate

@Composable
fun ApprovalDialog(
    approval: SecurityGate.PendingApproval,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDeny,
        title = { Text("Confirmar acción de riesgo ${approval.risk}") },
        text = {
            Column {
                Text("Herramienta: ${approval.toolId}")
                Text("Riesgo: ${approval.risk}")
                Text("Motivo: ${approval.reason}")
            }
        },
        confirmButton = {
            TextButton(onClick = onApprove) { Text("Aprobar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDeny) { Text("Denegar") }
        },
    )
}
