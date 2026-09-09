package com.yino.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4AA),      // Verde neón estilo Jarvis
    primaryContainer = Color(0xFF004D40),
    onPrimary = Color(0xFF000000),
    onPrimaryContainer = Color(0xFF00D4AA),
    secondary = Color(0xFF00D4AA),
    secondaryContainer = Color(0xFF002B2B),
    onSecondary = Color(0xFF000000),
    onSecondaryContainer = Color(0xFF00D4AA),
    tertiary = Color(0xFF00D4AA),
    tertiaryContainer = Color(0xFF002B2B),
    onTertiary = Color(0xFF000000),
    onTertiaryContainer = Color(0xFF00D4AA),
    surface = Color(0xFF0A0E17),       // Negro profundo estilo Jarvis
    onSurface = Color(0xFF00D4AA),
    surfaceContainerHighest = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF00D4AA),
    outline = Color(0xFF00D4AA),
    outlineVariant = Color(0xFF00D4AA99),
    background = Color(0xFF0A0E17),
    onBackground = Color(0xFF00D4AA),
    surfaceTint = Color(0xFF00D4AA),
    inverseSurface = Color(0xFFE0E7FF),
    onInverseSurface = Color(0xFF0A0E17),
    inversePrimary = Color(0xFF004D40),
    shadow = Color(0xFF000000),
    scrim = Color(0xFF000000),
    surfaceContainer = Color(0xFF0F172A),
    surfaceContainerLow = Color(0xFF0A0E17),
    surfaceContainerLowest = Color(0xFF050A14),
    surfaceContainerHigh = Color(0xFF1E293B),
    surfaceDim = Color(0xFF050A14),
    surfaceBright = Color(0xFF1E293B),
    surfaceContainerHighest = Color(0xFF1E293B),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006D77),
    primaryContainer = Color(0xFFB2EBF2),
    onPrimary = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF001F1F),
    secondary = Color(0xFF006D77),
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondary = Color(0xFFFFFFFF),
    onSecondaryContainer = Color(0xFF001F1F),
    tertiary = Color(0xFF006D77),
    tertiaryContainer = Color(0xFFB2EBF2),
    onTertiary = Color(0xFFFFFFFF),
    onTertiaryContainer = Color(0xFF001F1F),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1A1A2E),
    surfaceContainerHighest = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF4A4A68),
    outline = Color(0xFF006D77),
    outlineVariant = Color(0xFF006D7799),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1A2E),
    surfaceTint = Color(0xFF006D77),
    inverseSurface = Color(0xFF1A1A2E),
    onInverseSurface = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFF00E5FF),
    shadow = Color(0xFF000000),
    scrim = Color(0xFF000000),
    surfaceContainer = Color(0xFFF0F0F0),
    surfaceContainerLow = Color(0xFFF5F5F5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE0E0E0),
    surfaceDim = Color(0xFFE8E8E8),
    surfaceBright = Color(0xFFF5F5F5),
    surfaceContainerHighest = Color(0xFFD0E0E0),
)

@Composable
fun YinoTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}

@Composable
fun JarvisGlowEffect(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    color: Color = Color(0xFF00D4AA),
    radius: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .graphicsLayer {
                shadowElevation = 0f
            }
            .padding(4.dp)
            .graphicsLayer {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    // Shadow effect via graphicsLayer
                }
            }
            .background(
                color = androidx.compose.ui.graphics.Color.Transparent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Efecto glow usando shadow elevation
                    shadowElevation = 20.dp.toPx()
                }
                .background(
                    color = color.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = 0.5f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun JarvisPulseAnimation(
    color: Color = Color(0xFF00D4AA),
    size: androidx.compose.ui.unit.Dp = 60.dp,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    var infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "alpha"
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = alpha
            }
    ) {
        androidx.compose.foundation.Canvas(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF00D4AA).copy(alpha = 0.15f),
                radius = (size.toPx() / 2) * 0.9f,
            )
            drawCircle(
                color = Color(0xFF00D4AA).copy(alpha = 0.08f),
                radius = (size.toPx() / 2) * 0.7f,
            )
            drawCircle(
                color = Color(0xFF00D4AA).copy(alpha = 0.05f),
                radius = (size.toPx() / 2) * 0.5f,
            )
        }
    }
}