package com.yino.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.yino.ai.core.YinoGraph
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: YinoViewModel) {
    val messages by viewModel.messages.collectAsState()
    val pending by viewModel.pending.collectAsState()
    val busy by viewModel.busy.collectAsState()
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()

    if (pending != null) {
        ApprovalDialog(
            pending!!,
            onApprove = { viewModel.approve(pending!!.requestId) },
            onDeny = { viewModel.deny(pending!!.requestId) },
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true,
        ) {
            items(messages.reversed()) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(0.85f),
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isUser) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") },
                enabled = !busy,
            )
            Button(
                onClick = {
                    scope.launch {
                        val owner = if (YinoGraph.identity.requireFace) {
                            activity?.let { YinoGraph.identity.verifyFace(it) } ?: false
                        } else true
                        if (owner) {
                            viewModel.send(input)
                            input = ""
                        } else {
                            viewModel.denyUnknownSpeaker()
                        }
                    }
                },
                enabled = !busy && input.isNotBlank(),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize(0.5f))
                } else {
                    Text("Enviar")
                }
            }
        }
    }
}
