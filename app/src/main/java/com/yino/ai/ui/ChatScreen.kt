package com.yino.ai.ui

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yino.ai.core.YinoGraph
import com.yino.ai.ui.components.YinoAvatar
import com.yino.ai.ui.components.YinoErrorCard
import com.yino.ai.ui.components.YinoIconButton
import com.yino.ai.ui.components.YinoMessageBubble
import com.yino.ai.ui.components.YinoTextField
import com.yino.ai.ui.components.formatTime
import com.yino.ai.ui.theme.YinoColors
import com.yino.ai.ui.theme.YinoMotion
import com.yino.ai.ui.theme.YinoSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: YinoViewModel) {
    val messages by viewModel.messages.collectAsState()
    val pending by viewModel.pending.collectAsState()
    val busy by viewModel.busy.collectAsState()
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showAttach by remember { mutableStateOf(false) }

    if (pending != null) {
        ApprovalDialog(
            pending!!,
            onApprove = { viewModel.approve(pending!!.requestId) },
            onDeny = { viewModel.deny(pending!!.requestId) },
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(YinoSpacing.l)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = YinoSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YinoAvatar(size = 44.dp, processing = busy)
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            Column {
                Text(
                    "Yino AI",
                    color = YinoColors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Text(
                    "Asistente Inteligente",
                    color = YinoColors.textSecondary,
                    fontSize = 13.sp,
                )
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(YinoSpacing.s),
        ) {
            items(messages.reversed()) { msg ->
                if (msg.isError) {
                    val lastUser = messages.lastOrNull { it.role == "user" }?.text ?: ""
                    YinoErrorCard(
                        title = "No se pudo completar",
                        message = msg.text,
                        onRetry = { if (lastUser.isNotBlank()) doSend(lastUser, viewModel, activity, scope) },
                        detail = msg.detail,
                    )
                } else {
                    YinoMessageBubble(
                        text = msg.text,
                        isUser = msg.role == "user",
                        time = formatTime(msg.time),
                        onCopy = {
                            val clip = android.content.Context.CLIPBOARD_SERVICE
                            val cm = context.getSystemService(clip) as? android.content.ClipboardManager
                            cm?.setPrimaryClip(android.content.ClipData.newPlainText("yino", msg.text))
                        },
                    )
                }
            }
            if (busy) {
                item { ProcessingRow() }
            }
        }

        // Input
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = YinoSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YinoIconButton(
                onClick = { showAttach = true },
                icon = { Icon(androidx.compose.material.icons.Icons.Filled.AttachFile, null) },
                enabled = !busy,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(6.dp))
            YinoTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = "Escribe un mensaje...",
                enabled = !busy,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSend = { doSend(input, viewModel, activity, scope) { input = "" } },
                ),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(6.dp))
            YinoIconButton(
                onClick = { doSend(input, viewModel, activity, scope) { input = "" } },
                icon = {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(0.5f),
                            color = YinoColors.accentSecondary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Filled.Send, null)
                    }
                },
                enabled = !busy && input.isNotBlank(),
            )
        }
    }

    if (showAttach) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = { showAttach = false }, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth().padding(YinoSpacing.l)) {
                listOf("Cámara", "Galería", "Documento", "Audio").forEach { opt ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = YinoSpacing.s)
                            .clickable {
                                Toast.makeText(context, "$opt: aún no disponible", Toast.LENGTH_SHORT).show()
                                showAttach = false
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Filled.MoreVert, null, tint = YinoColors.textSecondary)
                        androidx.compose.foundation.layout.Spacer(Modifier.padding(10.dp))
                        Text(opt, color = YinoColors.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessingRow() {
    val t = rememberInfiniteTransition(label = "dots")
    val a by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(YinoMotion.fast * 3), RepeatMode.Reverse))
    Row(verticalAlignment = Alignment.CenterVertically) {
        YinoAvatar(size = 22.dp, processing = true)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
        Text("Yino está procesando", color = YinoColors.textSecondary)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
        repeat(3) {
            Text("•", color = YinoColors.accentSecondary.copy(alpha = a), fontSize = 18.sp)
        }
    }
}

private fun doSend(
    text: String,
    viewModel: YinoViewModel,
    activity: FragmentActivity?,
    scope: kotlinx.coroutines.CoroutineScope,
    clear: (() -> Unit)? = null,
) {
    val trimmed = text.trim()
    if (trimmed.isEmpty() || viewModel.busy.value) return
    scope.launch {
        val owner = if (YinoGraph.identity.requireFace) {
            activity?.let { YinoGraph.identity.verifyFace(it) } ?: false
        } else true
        if (owner) {
            viewModel.send(trimmed)
            clear?.invoke()
        } else {
            viewModel.denyUnknownSpeaker()
        }
    }
}
