package com.finley.android.qualitytime.service

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.finley.android.qualitytime.util.AppLog
import java.util.Locale

/**
 * Android Text-to-Speech implementation backed by `android.speech.tts.TextToSpeech`.
 *
 * The engine is created asynchronously; callers must check [isReady] before
 * speaking, or the play flow will surface a friendly error.
 */
class AndroidTextToSpeechService(context: Context) : TextToSpeechService {

    companion object {
        private const val TAG = "TTS"
    }

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var onStartListener: ((String) -> Unit)? = null
    private var onDoneListener: ((String) -> Unit)? = null
    private var selectedVoice: android.speech.tts.Voice? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINESE) ?: TextToSpeech.LANG_MISSING_DATA
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    AppLog.e(TAG) { "Chinese language not supported or missing data (code=$result)" }
                } else {
                    isReady = true
                    setupListener()
                    AppLog.i(TAG) { "TTS ready" }
                }
            } else {
                AppLog.e(TAG) { "TTS initialization failed with status: $status" }
            }
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                utteranceId?.let { onStartListener?.invoke(it) }
            }

            override fun onDone(utteranceId: String?) {
                utteranceId?.let { onDoneListener?.invoke(it) }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                AppLog.w(TAG) { "TTS onError: $utteranceId" }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                AppLog.w(TAG) { "TTS onError: $utteranceId, code=$errorCode" }
            }
        })
    }

    override fun speak(text: String, utteranceId: String, enqueue: Boolean) {
        val engine = tts ?: return
        if (!isReady) {
            AppLog.w(TAG) { "speak called before engine ready (id=$utteranceId)" }
            return
        }

        // Re-apply the selected voice before each utterance to guarantee it takes effect.
        selectedVoice?.let { engine.setVoice(it) }

        val queueMode = if (enqueue) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            // KEY_PARAM_VOLUME is a float in [0, 1]; 1.0 = full stream volume.
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val result = engine.speak(text, queueMode, params, utteranceId)
        AppLog.d(TAG) { "speak(id=$utteranceId) result=$result" }
    }

    override fun stop() {
        tts?.stop()
        AppLog.d(TAG) { "stop()" }
    }

    override fun dispose() {
        tts?.shutdown()
        tts = null
        isReady = false
    }

    override fun setProgressListener(onStart: (String) -> Unit, onDone: (String) -> Unit) {
        this.onStartListener = onStart
        this.onDoneListener = onDone
    }

    override fun getVoices(): List<TtsVoice> {
        val engine = tts ?: return emptyList()
        val allVoices = engine.voices ?: return emptyList()

        // Restore the current selection after probing setVoice().
        val previousSelected = selectedVoice
        val usableVoices = allVoices.filter { engine.setVoice(it) == TextToSpeech.SUCCESS }
        if (previousSelected != null) {
            engine.setVoice(previousSelected)
        } else if (usableVoices.isNotEmpty()) {
            selectedVoice = usableVoices.first()
        }

        val voicesToShow = if (usableVoices.isNotEmpty()) usableVoices else allVoices
        return voicesToShow
            .sortedBy { voice ->
                when {
                    voice.locale.language.startsWith("zh") -> 0
                    voice.locale.language.startsWith("en") -> 1
                    else -> 2
                }
            }
            .map { TtsVoice(id = it.name, name = it.name, locale = it.locale.toString()) }
    }

    override fun setVoice(id: String) {
        val engine = tts ?: return
        selectedVoice = engine.voices?.find { it.name == id }
        val success = selectedVoice?.let { engine.setVoice(it) }
        AppLog.d(TAG) { "setVoice($id) -> found=${selectedVoice != null}, setVoiceResult=$success" }
    }

    override fun isReady(): Boolean = isReady

    override fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }
}

actual fun createTextToSpeechService(platformContext: Any?): TextToSpeechService {
    val context = platformContext as? Context ?: throw IllegalArgumentException("Android context required")
    return AndroidTextToSpeechService(context)
}