package com.finley.android.qualitytime.service

class JvmTextToSpeechService : TextToSpeechService {
    private var onStartListener: ((String) -> Unit)? = null
    private var onDoneListener: ((String) -> Unit)? = null

    override fun speak(text: String, utteranceId: String, enqueue: Boolean) {
        println("JVM TTS: $text (ID: $utteranceId, Enqueue: $enqueue)")
        onStartListener?.invoke(utteranceId)
        // Simulate done after 1 second
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
        println("JVM TTS rate set to: $rate")
    }

    override fun getVoices(): List<TtsVoice> {
        return listOf(TtsVoice("jvm_default", "Default JVM Voice", "en-US"))
    }

    override fun setVoice(id: String) {
        println("JVM TTS voice set to: $id")
    }
}

actual fun createTextToSpeechService(platformContext: Any?): TextToSpeechService {
    return JvmTextToSpeechService()
}
