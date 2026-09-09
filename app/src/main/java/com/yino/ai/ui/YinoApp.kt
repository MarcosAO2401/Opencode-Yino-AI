package com.yino.ai.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.px
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.px
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yino.ai.ui.theme.JarvisGlowEffect
import com.yino.ai.ui.theme.JarvisPulseAnimation
import com.yino.ai.ui.theme.YinoTheme

@Composable
fun YinoApp(viewModel: YinoViewModel = viewModel()) {
    YinoTheme {
        val messages by viewModel.messages.collectAsStateWithLifecycle()
        var input by remember { mutableStateOf("") }
        val scope = remember { androidx.lifecycle.viewmodel.compose.viewModelScope }
        
        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            // Background con efecto sutil
            Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize()
                    .background(com.yino.ai.ui.theme.YinoTheme.colorScheme.background)
            )
            
            // Pulsating background glow
            JarvisPulseAnimation(
                color = com.yino.ai.ui.theme.YinoTheme.colorScheme.primary,
                size = 300.dp,
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .graphicsLayer { alpha = 0.3f }
            )
            
            Column(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                // Header estilo Jarvis
                JarvisHeader()
                
                // Visualizador central tipo Jarvis
                JarvisVisualizer()
                
                // Área de mensajes
                MessagesArea(messages = viewModel.messages.value)
                
                // Input area
                InputArea(
                    input = viewModel.input,
                    onInputChange = { viewModel.input = it },
                    onSend = { viewModel.sendMessage(input) },
                    busy = viewModel.busy.value,
                    pending = viewModel.pending.value
                )
            }
        }
    }
}

@Composable
fun JarvisHeader() {
    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        // Logo Jarvis
        Row(
            modifier = androidx.compose.ui.Modifier
                .size(48.dp)
                .graphicsLayer {
                    translationX = -4.dp
                }
            ) {
                androidx.compose.foundation.Canvas(modifier = androidx.compose.ui.Modifier.size(48.dp)) {
                    // Círculo exterior
                    drawCircle(
                        color = com.yino.ai.ui.theme.YinoTheme.colorScheme.primary.copy(alpha = 0.2f),
                        radius = 24.dp.toPx(),
                    )
                    // Círculo interior pulsante
                    drawCircle(
                        color = com.yino.ai.ui.theme.YinoTheme.colorScheme.primary,
                        radius = 16.dp.toPx(),
                    )
                    // Punto central
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                    )
                }
            }
        
        Column(modifier = androidx.compose.ui.Modifier.padding(start = 12.dp)) {
            Text(
                text = "YINO",
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "SISTEMA OPERATIVO",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = androidx.compose.ui.graphics.Color(0xFF00D4AA)
            )
        }
        
        // Status indicator
        Box(
            modifier = androidx.compose.ui.Modifier
                .size(12.dp)
                .background(Color(0xFF00D4AA), CircleShape)
                .padding(end = 8.dp)
        )
        
        Text(
            text = "EN LÍNEA",
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = androidx.compose.ui.graphics.Color(0xFF00D4AA),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun JarvisVisualizer() {
    Box(
        modifier = androidx.compose.ui.Modifier
            .size(200.dp)
            .padding(vertical = 24.dp)
    ) {
        // Anillos concéntricos estilo Jarvis
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            // Anillo exterior pulsante
            JarvisPulseRing(progress = 0f)
            // Anillo medio
            JarvisPulseRing(progress = 0.33f, color = androidx.compose.ui.graphics.Color(0xFF00D4AA).copy(alpha = 0.6f))
            // Anillo interior
            JarvisPulseRing(progress = 0.66f, color = androidx.compose.ui.graphics.Color(0xFF00D4AA).copy(alpha = 0.4f))
            // Núcleo central
            JarvisCore()
        }
    }
}

@Composable
fun JarvisPulseRing(
    progress: Float = 0f,
    color: Color = Color(0xFF00D4AA),
) {
    var progressAnim by androidx.compose.animation.core.animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.tween(3000, delayMillis = 0, easing = androidx.compose.animation.core.LinearEasing)
    )
    
    androidx.compose.foundation.Canvas(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = (size.width.min(size.height) / 2f) * (0.6f + progress * 0.4f)
        
        drawCircle(
            color = color.copy(alpha = 0.15f),
            radius = radius,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        
        // Arco animado
        val sweepAngle = 360f * (progressAnim * 0.5f % 1f)
        drawArc(
            color = color.copy(alpha = 0.8f),
            startAngle = 270f + progressAnim * 360f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun JarvisCore() {
    val pulse by androidx.compose.animation.core.animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, delayMillis = 0, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "corePulse"
    )
    
    Box(
        modifier = androidx.compose.ui.Modifier
            .size(60.dp)
            .graphicsLayer {
                scaleX = 0.8f + pulse * 0.3f
                scaleY = 0.8f + pulse * 0.3f
            }
    ) {
        androidx.compose.foundation.Canvas(modifier = androidx.compose.ui.Modifier.size(60.dp)) {
            // Núcleo central brillante
            drawCircle(
                color = Color(0xFF00D4AA).copy(alpha = 0.9f),
                radius = 30.dp.toPx()
            )
            
            // Anillo interno pulsante
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 20.dp.toPx(),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            
            // Partículas alrededor
            for (i in 0..5) {
                val angle = (i * 60f + System.currentTimeMillis() / 1000.0 % 360) * Math.PI / 180
                val radius = 35.dp.toPx()
                val x = 30.dp.toPx() + radius * cos(angle)
                val y = 30.dp.toPx() + radius * sin(angle)
                drawCircle(
                    color = Color(0xFF00D4AA).copy(alpha = 0.6f),
                    radius = 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun MessagesArea(messages: List<ChatMessageUi>) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .weight(1f)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        reverseLayout = true,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(messages.reversed()) { msg ->
            MessageBubble(msg)
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessageUi) {
    val isUser = msg.role == "user"
    
    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Avatar Yino
            Box(
                modifier = androidx.compose.ui.Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(32.dp)
                        .graphicsLayer { scaleX = 0.9f; scaleY = 0.9f }
                ) {
                    androidx.compose.foundation.Canvas(modifier = androidx.compose.ui.Modifier.size(32.dp)) {
                        drawCircle(
                            color = Color(0xFF00D4AA).copy(alpha = 0.3f),
                            radius = 16.dp.toPx()
                        )
                        drawCircle(
                            color = Color(0xFF00D4AA),
                            radius = 10.dp.toPx()
                        )
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
        }
        
        Card(
            modifier = androidx.compose.ui.Modifier
                .padding(vertical = 4.dp)
                .widthIn(min = 80.dp, max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF00D4AA) else Color(0xFF0F172A),
                contentColor = if (isUser) Color.White else Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = msg.text,
                color = if (isUser) Color.White else Color.White,
                fontSize = 15.sp,
                modifier = androidx.compose.ui.Modifier.padding(16.dp),
                style = androidx.compose.ui.text.TextStyle(
                    lineHeight = 22.sp,
                    letterSpacing = 0.2.sp
                )
            )
        }
        
        if (isUser) {
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
            // Avatar Usuario
            Box(
                modifier = androidx.compose.ui.Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = androidx.compose.ui.Modifier.size(32.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = 16.dp.toPx()
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = 10.dp.toPx()
                    )
                }
            }
        }
    }
}

@Composable
fun InputArea(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    busy: Boolean,
    pending: com.yino.ai.core.security.SecurityGate.PendingApproval?
) {
    Column(modifier = androidx.compose.ui.Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Pending approval banner
        pending?.let { approval ->
            Card(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFFFD54F).copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚠ ${approval.toolId.toUpperCase()} requiere confirmación: ${approval.reason}",
                        color = Color.Black,
                        fontSize = 13.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { YinoGraph.security.respond(approval.requestId, false) },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.8f)
                            )
                        ) {
                            Text("DENEGAR", color = Color.White, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { YinoGraph.security.respond(approval.requestId, true) },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00D4AA)
                            )
                        ) {
                            Text("APROBAR", color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        
        // Input row
        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.TextField(
                value = _input,
                onValueChange = onInputChange,
                modifier = androidx.compose.ui.Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                placeholder = { Text("Consulta a Yino...", color = Color.White.copy(alpha = 0.5f)) },
                singleLine = true,
                colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFF0F172A),
                    focusedContainerColor = Color(0xFF1E293B),
                    textColor = Color.White,
                    placeholderColor = Color.White.copy(alpha = 0.4f),
                    unfocusedStrokeColor = Color(0xFF00D4AA).copy(alpha = 0.3f),
                    focusedStrokeColor = Color(0xFF00D4AA),
                    disabledContainerColor = Color(0xFF0F172A).copy(alpha = 0.5f),
                    disabledTextColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color(0xFF00D4AA)
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = androidx.compose.ui.text.input.KeyboardActions(
                    onDone = { onSend() }
                )
            )
            
            // Botón enviar / micrófono
            Box(
                modifier = androidx.compose.ui.Modifier.size(56.dp)
            ) {
                if (_input.isNotBlank()) {
                    IconButton(
                        onClick = onSend,
                        modifier = androidx.compose.ui.Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                } else {
                    IconButton(
                        onClick = { /* Activar escucha de voz */ },
                        modifier = androidx.compose.ui.Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Mic,
                            contentDescription = "Voz",
                            tint = Color(0xFF00D4AA)
                        )
                    }
                }
            }
        }
    }
    
    var _input by remember { mutableStateOf("") }
}