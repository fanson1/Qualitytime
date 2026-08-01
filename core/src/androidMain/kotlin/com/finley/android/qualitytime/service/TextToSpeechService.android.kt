package com.finley.android.qualitytime.service

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class AndroidTextToSpeechService(context: Context) : TextToSpeechService {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var onStartListener: ((String) -> Unit)? = null
    private var onDoneListener: ((String) -> Unit)? = null
    private var selectedVoice: android.speech.tts.Voice? = null

    init {
        Log.i("TTS", "Initializing TTS with context: $context")
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINESE)
                Log.i("TTS", "Language set to CHINESE, result code: $result")
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Chinese language not supported or missing data")
                } else {
                    Log.i("TTS", "TTS Ready and Chinese supported")
                    isReady = true
                    setupListener()
                }
            } else {
                Log.e("TTS", "Initialization failed with status: $status")
            }
        }
    }

    private fun setupListener() {
        Log.i("TTS", "Setting up UtteranceProgressListener")
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.i("TTS", "onStart: $utteranceId")
                utteranceId?.let { onStartListener?.invoke(it) }
            }

            override fun onDone(utteranceId: String?) {
                Log.i("TTS", "onDone: $utteranceId")
                utteranceId?.let { onDoneListener?.invoke(it) }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e("TTS", "onError: $utteranceId")
            }
            
            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e("TTS", "onError: $utteranceId, code: $errorCode")
            }
        })
    }

    override fun speak(text: String, utteranceId: String, enqueue: Boolean) {
        Log.i("TTS", "speak request: $text (ID: $utteranceId, ready: $isReady)")
        if (isReady) {
            // Re-apply voice before each utterance to ensure it takes effect
            selectedVoice?.let { tts?.setVoice(it) }

            val queueMode = if (enqueue) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
            val params = Bundle().apply {
                putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
                // Boost TTS volume: 1.0f = 100% of system volume
                // TTS speech has lower RMS energy than music, so boost to compensate
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.8f)
            }
            val result = tts?.speak(text, queueMode, params, utteranceId)
            if (result == TextToSpeech.ERROR) {
                Log.e("TTS", "tts.speak returned ERROR for $utteranceId")
            } else {
                Log.i("TTS", "tts.speak successfully queued $utteranceId")
            }
        } else {
            Log.w("TTS", "speak called but TTS not ready")
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun dispose() {
        tts?.shutdown()
        tts = null
    }

    override fun setProgressListener(onStart: (String) -> Unit, onDone: (String) -> Unit) {
        this.onStartListener = onStart
        this.onDoneListener = onDone
    }

    override fun getVoices(): List<TtsVoice> {
        val allVoices = tts?.voices
        Log.i("TTS", "getVoices requested. Total system voices: ${allVoices?.size ?: 0}")

        if (allVoices.isNullOrEmpty()) return emptyList()

        // Save current voice to restore after testing
        val previousSelected = selectedVoice

        // Test which voices are actually installed (setVoice returns SUCCESS)
        val usableVoices = allVoices.filter { voice ->
            tts?.setVoice(voice) == TextToSpeech.SUCCESS
        }
        Log.i("TTS", "Installed (usable) voices: ${usableVoices.size}")

        // Restore the previously selected voice
        if (previousSelected != null) {
            tts?.setVoice(previousSelected)
        } else if (usableVoices.isNotEmpty()) {
            // No voice was selected before; keep the first usable one as default
            selectedVoice = usableVoices.first()
        }

        // Show only installed voices; fallback to all if somehow none tested usable
        val voicesToShow = if (usableVoices.isNotEmpty()) usableVoices else allVoices

        return voicesToShow.sortedBy { voice ->
            when {
                voice.locale.language.startsWith("zh") -> 0
                voice.locale.language.startsWith("en") -> 1
                else -> 2
            }
        }.map {
            TtsVoice(it.name, it.name, it.locale.toString())
        }
    }

    override fun setVoice(id: String) {
        selectedVoice = tts?.voices?.find { it.name == id }
        val success = selectedVoice?.let { tts?.setVoice(it) }
        Log.i("TTS", "setVoice($id) -> found=${selectedVoice != null}, setVoiceResult=$success")
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
