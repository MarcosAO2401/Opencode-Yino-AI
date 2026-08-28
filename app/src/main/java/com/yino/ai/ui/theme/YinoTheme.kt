package com.yino.ai.ui.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yino.ai.R

val YinoFont = FontFamily(Font(R.font.orbitron))

private val DarkColors = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF00181C),
    secondary = Color(0xFFB388FF),
    tertiary = Color(0xFF00FFA3),
    background = Color(0xFF05080D),
    surface = Color(0xFF0F1620),
    surfaceVariant = Color(0xFF16202E),
    onBackground = Color(0xFFE6F1FF),
    onSurface = Color(0xFFE6F1FF),
    outline = Color(0xFF1E2A38),
)

private val YinoTypography = Typography(
    displaySmall = TextStyle(fontFamily = YinoFont, fontWeight = FontWeight.Bold, fontSize = 30.sp),
    headlineSmall = TextStyle(fontFamily = YinoFont, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    headlineMedium = TextStyle(fontFamily = YinoFont, fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontFamily = YinoFont, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = YinoFont, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    labelLarge = TextStyle(fontFamily = YinoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = YinoFont, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = YinoFont, fontSize = 14.sp),
)

val YinoShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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
    val t = rememberInfiniteTransition(label = "glow")
    val a1 by t.animateFloat(0.10f, 0.24f, infiniteRepeatable(tween(4200), RepeatMode.Reverse))
    val a2 by t.animateFloat(0.08f, 0.20f, infiniteRepeatable(tween(5600), RepeatMode.Reverse))
    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFF05080D), Color(0xFF0A1626), Color(0xFF120A22)),
            ),
        ),
    ) {
        Box(
            Modifier.wrapContentSize(Alignment.TopEnd).size(280.dp).offset(60.dp, (-50).dp)
                .background(Brush.radialGradient(listOf(Color(0xFF00E5FF).copy(alpha = a1), Color.Transparent))),
        )
        Box(
            Modifier.wrapContentSize(Alignment.BottomStart).size(320.dp).offset((-80).dp, 70.dp)
                .background(Brush.radialGradient(listOf(Color(0xFFB388FF).copy(alpha = a2), Color.Transparent))),
        )
    }
}
