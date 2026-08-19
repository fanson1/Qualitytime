package com.finley.android.qualitytime

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finley.android.qualitytime.service.SettingsService
import com.finley.android.qualitytime.service.TextToSpeechService
import com.finley.android.qualitytime.ui.poem.PoemScreen
import com.finley.android.qualitytime.ui.poem.PoemViewModel
import com.finley.android.qualitytime.ui.theme.QtyTheme

@Composable
@Preview
fun App(
    ttsService: TextToSpeechService? = null,
    settingsService: SettingsService? = null
) {
    QtyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val viewModel: PoemViewModel = viewModel { PoemViewModel(ttsService, settingsService) }
            PoemScreen(viewModel)
        }
    }
}
