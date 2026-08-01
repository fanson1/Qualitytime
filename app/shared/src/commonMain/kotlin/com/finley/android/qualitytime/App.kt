package com.finley.android.qualitytime

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finley.android.qualitytime.service.SettingsService
import com.finley.android.qualitytime.service.TextToSpeechService
import com.finley.android.qualitytime.ui.poem.PoemScreen
import com.finley.android.qualitytime.ui.poem.PoemViewModel

private val TraditionalPaper = Color(0xFFFCFBF4)
private val JadeGreen = Color(0xFF006D5B)
private val DeepRed = Color(0xFF8B0000)

@Composable
@Preview
fun App(
    ttsService: TextToSpeechService? = null,
    settingsService: SettingsService? = null
) {
    val lightColors = lightColorScheme(
        primary = JadeGreen,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0F2F1),
        secondary = DeepRed,
        background = TraditionalPaper,
        surface = Color.White,
        surfaceVariant = Color(0xFFF0F0F0)
    )

    MaterialTheme(
        colorScheme = lightColors,
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp)
        )
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            val viewModel: PoemViewModel = viewModel { PoemViewModel(ttsService, settingsService) }
            PoemScreen(viewModel)
        }
    }
}
