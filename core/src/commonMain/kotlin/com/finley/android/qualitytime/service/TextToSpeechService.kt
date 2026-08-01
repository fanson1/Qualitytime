package com.finley.android.qualitytime.service

interface TextToSpeechService {
    fun speak(text: String, utteranceId: String = "default", enqueue: Boolean = false)
    fun stop()
    fun dispose()
    fun setProgressListener(onStart: (String) -> Unit, onDone: (String) -> Unit)
    fun isReady(): Boolean
    fun setSpeechRate(rate: Float)
    fun getVoices(): List<TtsVoice>
    fun setVoice(id: String)
}

expect fun createTextToSpeechService(platformContext: Any? = null): TextToSpeechService
