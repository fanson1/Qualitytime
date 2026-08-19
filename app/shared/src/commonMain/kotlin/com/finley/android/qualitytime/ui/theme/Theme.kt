package com.finley.android.qualitytime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Brand palette ───────────────────────────────────────────────────────
// 设计语言: 中国古典「纸墨」意象 + 现代 Material 3
object QtyColors {
    val Jade = Color(0xFF00695C)
    val JadeDark = Color(0xFF004D40)
    val JadeLight = Color(0xFF9FD6CE)
    val JadePale = Color(0xFFD7EFEA)
    val Cinnabar = Color(0xFF9A4632)
    val CinnabarLight = Color(0xFFFFDBD0)
    val Gold = Color(0xFFB0872A)
    val GoldLight = Color(0xFFF4DEB0)
    val Ink = Color(0xFF201B17)
    val InkSoft = Color(0xFF4A4740)
    val Paper = Color(0xFFFAF7EF)
    val PaperLight = Color(0xFFFFFCF6)
    val PaperDark = Color(0xFFF1EBDE)
    val StarYellow = Color(0xFFF5B301)
}

private val QtyLightColors = lightColorScheme(
    primary = QtyColors.Jade,
    onPrimary = Color.White,
    primaryContainer = QtyColors.JadePale,
    onPrimaryContainer = Color(0xFF00332D),
    secondary = QtyColors.Cinnabar,
    onSecondary = Color.White,
    secondaryContainer = QtyColors.CinnabarLight,
    onSecondaryContainer = Color(0xFF3B0A02),
    tertiary = QtyColors.Gold,
    onTertiary = Color.White,
    tertiaryContainer = QtyColors.GoldLight,
    onTertiaryContainer = Color(0xFF2A1E00),
    background = QtyColors.Paper,
    onBackground = QtyColors.Ink,
    surface = QtyColors.PaperLight,
    onSurface = QtyColors.Ink,
    surfaceVariant = Color(0xFFECE7DC),
    onSurfaceVariant = QtyColors.InkSoft,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F1E7),
    surfaceContainer = Color(0xFFF0EBE0),
    surfaceContainerHigh = Color(0xFFEAE5DA),
    surfaceContainerHighest = Color(0xFFE4DFD4),
    outline = Color(0xFF7C756C),
    outlineVariant = Color(0xFFD2CCC0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val QtyDarkColors = darkColorScheme(
    primary = Color(0xFF81CBC4),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF9DF2E8),
    secondary = Color(0xFFFFB5A0),
    onSecondary = Color(0xFF581F10),
    secondaryContainer = Color(0xFF7A3524),
    onSecondaryContainer = Color(0xFFFFDBD0),
    tertiary = Color(0xFFE4C887),
    onTertiary = Color(0xFF3F2F00),
    tertiaryContainer = Color(0xFF5B4400),
    onTertiaryContainer = Color(0xFFFFE4A1),
    background = Color(0xFF17140F),
    onBackground = Color(0xFFEDE0D4),
    surface = Color(0xFF1D1A15),
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF4A4640),
    onSurfaceVariant = Color(0xFFCCC5BA),
    surfaceContainerLowest = Color(0xFF120F0B),
    surfaceContainerLow = Color(0xFF1F1C17),
    surfaceContainer = Color(0xFF24201B),
    surfaceContainerHigh = Color(0xFF2E2A24),
    surfaceContainerHighest = Color(0xFF393530),
    outline = Color(0xFF958F84),
    outlineVariant = Color(0xFF4A4640),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val QtyShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

// 古诗展示使用衬线字体，营造「古籍」质感
val PoemFont = FontFamily.Serif
val UiFont = FontFamily.SansSerif

private val QtyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PoemFont, fontWeight = FontWeight.Bold,
        fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PoemFont, fontWeight = FontWeight.Bold,
        fontSize = 45.sp, lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PoemFont, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PoemFont, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoemFont, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PoemFont, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PoemFont, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = UiFont, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = UiFont, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = UiFont, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = UiFont, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = UiFont, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = UiFont, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = UiFont, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = UiFont, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
)

@Composable
fun QtyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) QtyDarkColors else QtyLightColors,
        typography = QtyTypography,
        shapes = QtyShapes,
        content = content
    )
}
