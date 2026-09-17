package com.finley.android.qualitytime

import android.app.Application
import com.finley.android.qualitytime.service.DataStoreSettingsService
import com.finley.android.qualitytime.service.SettingsService
import com.finley.android.qualitytime.service.TextToSpeechService
import com.finley.android.qualitytime.service.createDataStore
import com.finley.android.qualitytime.service.createTextToSpeechService

/**
 * Application-scoped dependency holder.
 *
 * The TTS engine and DataStore live for the whole process instead of being
 * re-created on every configuration change. Rebuilding TTS in `Activity.onCreate`
 * used to orphan the previous engine while the retained ViewModel kept speaking
 * through it, wasting resources and leaking listeners.
 */
class QualityTimeApp : Application() {

    lateinit var ttsService: TextToSpeechService
        private set
    lateinit var settingsService: SettingsService
        private set

    override fun onCreate() {
        super.onCreate()
        ttsService = createTextToSpeechService(applicationContext)
        settingsService = DataStoreSettingsService(createDataStore(applicationContext))
    }
}