package com.yino.ai.ui.components

import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yino.ai.ui.theme.YinoBrand
import com.yino.ai.ui.theme.YinoColors
import com.yino.ai.ui.theme.YinoMotion
import com.yino.ai.ui.theme.YinoRadius
import com.yino.ai.ui.theme.YinoShapes
import com.yino.ai.ui.theme.YinoSpacing
import java.util.Date

fun formatTime(ts: Long): String = DateFormat.format("HH:mm", Date(ts)).toString()

@Composable
fun YinoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(YinoMotion.fast))
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interaction,
        modifier = modifier.scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = YinoColors.accentPrimary,
            contentColor = YinoColors.backgroundPrimary,
            disabledContainerColor = YinoColors.surfaceElevated,
            disabledContentColor = YinoColors.textTertiary,
        ),
        shape = YinoShapes.large,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = YinoColors.backgroundPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun YinoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    contentDescription: String? = null,
    icon: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, tween(YinoMotion.fast))
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        modifier = modifier
            .scale(scale)
            .size(48.dp),
        shape = YinoShapes.medium,
        color = if (selected) YinoColors.accentPrimary.copy(alpha = 0.15f) else YinoColors.surface,
        border = BorderStroke(
            1.dp,
            if (selected) YinoColors.accentPrimary else YinoColors.border,
        ),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalContentColor provides if (selected) YinoColors.accentSecondary else YinoColors.textSecondary,
            ) { icon() }
        }
    }
}

@Composable
fun YinoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = YinoShapes.large,
        color = YinoColors.surface,
        border = BorderStroke(1.dp, YinoColors.border),
    ) {
        Column(Modifier.padding(YinoSpacing.l)) { content() }
    }
}

@Composable
fun YinoAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    processing: Boolean = false,
) {
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "avatarGlow")
    val glow by t.animateFloat(0.35f, 0.7f, androidx.compose.animation.core.infiniteRepeatable(
        tween(3000), androidx.compose.animation.core.RepeatMode.Reverse,
    ))
    Box(
        modifier = modifier
            .size(size)
            .background(YinoColors.surfaceElevated, CircleShape)
            .border(1.dp, YinoColors.border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Y",
            fontFamily = YinoBrand,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.5).sp,
            color = YinoColors.accentSecondary.copy(alpha = glow),
        )
        if (processing) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(0.94f),
                color = YinoColors.accentSecondary,
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
fun YinoMessageBubble(
    text: String,
    isUser: Boolean,
    time: String = "",
    onCopy: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(Modifier.widthIn(0.dp, 320.dp)) {
            Surface(
                shape = YinoShapes.large,
                color = if (isUser) Color(0xFF102A42) else YinoColors.surface,
                border = BorderStroke(1.dp, YinoColors.border),
            ) {
                Column(Modifier.padding(YinoSpacing.m)) {
                    if (!isUser) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            YinoAvatar(size = 22.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Yino AI",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = YinoColors.accentSecondary,
                                ),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Text(
                        text,
                        color = YinoColors.textPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (time.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            time,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = YinoColors.textTertiary,
                            ),
                            modifier = Modifier.align(Alignment.End),
                        )
                    }
                }
            }
            if (!isUser && onCopy != null) {
                TextButton(onClick = onCopy, modifier = Modifier.align(Alignment.End)) {
                    Text("Copiar", color = YinoColors.textTertiary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun YinoErrorCard(
    title: String,
    message: String,
    onRetry: (() -> Unit)? = null,
    detail: String? = null,
) {
    var expanded by remember { androidx.compose.runtime.mutableStateOf(false) }
    Surface(
        shape = YinoShapes.large,
        color = YinoColors.surface,
        border = BorderStroke(1.dp, YinoColors.error),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(YinoSpacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, null, tint = YinoColors.error)
                Spacer(Modifier.width(8.dp))
                Text(title, color = YinoColors.textPrimary, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(6.dp))
            Text(message, color = YinoColors.textSecondary, style = MaterialTheme.typography.bodyLarge)
            if (detail != null) {
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Ocultar detalles" else "Ver detalles", color = YinoColors.accentSecondary)
                }
                if (expanded) {
                    Text(
                        detail,
                        color = YinoColors.textTertiary,
                        style = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp),
                    )
                }
            }
            if (onRetry != null) {
                Spacer(Modifier.height(6.dp))
                YinoButton("Reintentar", onClick = onRetry, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
fun YinoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val focused = remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        if (focused.value) YinoColors.accentPrimary else YinoColors.border,
        tween(YinoMotion.normal),
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(YinoColors.backgroundSecondary, RoundedCornerShape(YinoRadius.large))
            .border(1.dp, borderColor, RoundedCornerShape(YinoRadius.large))
            .onFocusChanged { focused.value = it.isFocused }
            .padding(YinoSpacing.m),
        enabled = enabled,
        textStyle = TextStyle(color = YinoColors.textPrimary, fontSize = 16.sp),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        cursorBrush = Brush.verticalGradient(listOf(YinoColors.accentSecondary, YinoColors.accentPrimary)),
        decorationBox = { inner ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = YinoColors.textTertiary, fontSize = 16.sp)
                    }
                    inner()
                }
                if (trailingIcon != null) trailingIcon()
            }
        },
    )
}
