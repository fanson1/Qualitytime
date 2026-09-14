package com.finley.android.qualitytime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import com.finley.android.qualitytime.service.DataStoreSettingsService
import com.finley.android.qualitytime.service.createDataStore
import com.finley.android.qualitytime.service.createTextToSpeechService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Prevent the system from adding a translucent scrim to the navigation
        // bar so the app's edge-to-edge background shows through it.
        window.isNavigationBarContrastEnforced = false

        val ttsService = createTextToSpeechService(applicationContext)
        val settingsService = DataStoreSettingsService(createDataStore(applicationContext))

        setContent {
            App(ttsService, settingsService)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
