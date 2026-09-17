package com.finley.android.qualitytime.service

import com.finley.android.qualitytime.util.AppLog

/**
 * Stub implementation for JVM targets (used by the Ktor server and local runs).
 * There is no way to synthesize speech on a plain JVM, so utterances are
 * acknowledged immediately rather than played.
 */
class JvmTextToSpeechService : TextToSpeechService {
    companion object {
        private const val TAG = "JVM-TTS"
    }

    private var onStartListener: ((String) -> Unit)? = null
    private var onDoneListener: ((String) -> Unit)? = null

    override fun speak(text: String, utteranceId: String, enqueue: Boolean) {
        AppLog.i(TAG) { "speak(id=$utteranceId, enqueue=$enqueue): $text" }
        onStartListener?.invoke(utteranceId)
        onDoneListener?.invoke(utteranceId)
    }

    override fun stop() {}

    override fun dispose() {}

    override fun setProgressListener(onStart: (String) -> Unit, onDone: (String) -> Unit) {
        this.onStartListener = onStart
        this.onDoneListener = onDone
    }

    override fun isReady(): Boolean = true

    override fun setSpeechRate(rate: Float) {
        AppLog.i(TAG) { "speech rate set to $rate" }
    }

    override fun getVoices(): List<TtsVoice> {
        return listOf(TtsVoice("jvm_default", "Default JVM Voice", "en-US"))
    }

    override fun setVoice(id: String) {
        AppLog.i(TAG) { "voice set to $id" }
    }
}

actual fun createTextToSpeechService(platformContext: Any?): TextToSpeechService {
    return JvmTextToSpeechService()
}