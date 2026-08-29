package com.yino.ai.ui.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yino.ai.R

// Branding font (solo para títulos/marca). El cuerpo usa Roboto (Default) para legibilidad.
val YinoBrand = FontFamily(Font(R.font.orbitron))
val YinoText = FontFamily.Default

object YinoColors {
    val backgroundPrimary = Color(0xFF05080D)
    val backgroundSecondary = Color(0xFF08111C)
    val surface = Color(0xFF0B1622)
    val surfaceElevated = Color(0xFF101D2B)
    val accentPrimary = Color(0xFF2196F3)
    val accentSecondary = Color(0xFF38A9FF)
    val border = Color(0xFF1B3A52)
    val textPrimary = Color(0xFFEAF2F8)
    val textSecondary = Color(0xFF9AA9B8)
    val textTertiary = Color(0xFF667788)
    val error = Color(0xFFE5484D)
    val success = Color(0xFF3DDC84)
    val warning = Color(0xFFFFB020)
}

object YinoSpacing {
    val xs: Dp = 4.dp
    val s: Dp = 8.dp
    val m: Dp = 12.dp
    val l: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
}

object YinoRadius {
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val xlarge: Dp = 24.dp
}

object YinoMotion {
    const val fast = 120
    const val normal = 250
    const val slow = 500
}

private val DarkColors = darkColorScheme(
    primary = YinoColors.accentPrimary,
    onPrimary = YinoColors.backgroundPrimary,
    secondary = YinoColors.accentSecondary,
    background = YinoColors.backgroundPrimary,
    surface = YinoColors.surface,
    surfaceVariant = YinoColors.surfaceElevated,
    onBackground = YinoColors.textPrimary,
    onSurface = YinoColors.textPrimary,
    onSurfaceVariant = YinoColors.textSecondary,
    outline = YinoColors.border,
    error = YinoColors.error,
    onError = YinoColors.backgroundPrimary,
)

private val YinoTypography = Typography(
    displaySmall = TextStyle(fontFamily = YinoBrand, fontWeight = FontWeight.Bold, fontSize = 30.sp),
    headlineSmall = TextStyle(fontFamily = YinoBrand, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    headlineMedium = TextStyle(fontFamily = YinoBrand, fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontFamily = YinoBrand, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = YinoText, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    labelLarge = TextStyle(fontFamily = YinoText, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = YinoText, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = YinoText, fontSize = 14.sp),
)

val YinoShapes = Shapes(
    small = RoundedCornerShape(YinoRadius.small),
    medium = RoundedCornerShape(YinoRadius.medium),
    large = RoundedCornerShape(YinoRadius.large),
)

@Composable
fun YinoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = YinoTypography,
        shapes = YinoShapes,
        content = content,
    )
}

@Composable
fun AnimatedYinoBackground(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "bgGlow")
    val a1 by t.animateFloat(0.08f, 0.16f, infiniteRepeatable(tween(5000), RepeatMode.Reverse))
    val a2 by t.animateFloat(0.06f, 0.12f, infiniteRepeatable(tween(6500), RepeatMode.Reverse))
    Box(
        modifier.fillMaxSize().background(YinoColors.backgroundPrimary),
    ) {
        Box(
            Modifier.wrapContentSize(Alignment.TopEnd).size(260.dp).offset(40.dp, (-40).dp)
                .background(
                    Brush.radialGradient(
                        listOf(YinoColors.accentSecondary.copy(alpha = a1), Color.Transparent),
                    ),
                ),
        )
        Box(
            Modifier.wrapContentSize(Alignment.BottomStart).size(300.dp).offset((-60).dp, 60.dp)
                .background(
                    Brush.radialGradient(
                        listOf(YinoColors.accentPrimary.copy(alpha = a2), Color.Transparent),
                    ),
                ),
        )
    }
}
