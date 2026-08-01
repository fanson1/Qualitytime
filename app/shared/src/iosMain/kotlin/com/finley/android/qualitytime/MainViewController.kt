package com.finley.android.qualitytime

import androidx.compose.ui.window.ComposeUIViewController
import com.finley.android.qualitytime.service.DataStoreSettingsService
import com.finley.android.qualitytime.service.createDataStore
import com.finley.android.qualitytime.service.createTextToSpeechService

fun MainViewController() = ComposeUIViewController { 
    val ttsService = createTextToSpeechService()
    val settingsService = DataStoreSettingsService(createDataStore())
    App(ttsService, settingsService) 
}
