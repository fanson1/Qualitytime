package com.finley.android.qualitytime.service

import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.darwin.NSObject

class IosTextToSpeechService : TextToSpeechService {
    private val synthesizer = AVSpeechSynthesizer()
    private var onStartListener: ((String) -> Unit)? = null
    private var onDoneListener: ((String) -> Unit)? = null
    private var currentRate: Float = 1.0f
    private var selectedVoiceId: String? = null

    private val utteranceIdMap = mutableMapOf<String, String>()

    private val delegate = object : NSObject(), AVSpeechSynthesizerDelegateProtocol {
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didStartSpeechUtterance: AVSpeechUtterance) {
            val speechString = didStartSpeechUtterance.speechString ?: return
            val id = utteranceIdMap[speechString] ?: return
            onStartListener?.invoke(id)
        }

        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didFinishSpeechUtterance: AVSpeechUtterance) {
            val speechString = didFinishSpeechUtterance.speechString ?: return
            val id = utteranceIdMap[speechString] ?: return
            onDoneListener?.invoke(id)
        }
    }

    init {
        synthesizer.delegate = delegate
        // Route audio through loudspeaker for louder playback
        val session = AVAudioSession.sharedInstance()
        session.setCategoryWithOptionsError(
            AVAudioSessionCategoryPlayback,
            AVAudioSessionCategoryOptionDefaultToSpeaker
        )
        session.setActiveError(true)
    }

    override fun speak(text: String, utteranceId: String, enqueue: Boolean) {
        if (!enqueue) {
            synthesizer.stopSpeakingAtBoundary(0)
        }
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        
        val voice = if (selectedVoiceId != null) {
            AVSpeechSynthesisVoice.voiceWithIdentifier(selectedVoiceId!!)
        } else {
            AVSpeechSynthesisVoice.voiceWithLanguage("zh-CN")
        }
        utterance.voice = voice
        utterance.rate = 0.5f * currentRate

        utteranceIdMap[text] = utteranceId
        synthesizer.speakUtterance(utterance)
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(0)
    }

    override fun dispose() {}

    override fun setProgressListener(onStart: (String) -> Unit, onDone: (String) -> Unit) {
        this.onStartListener = onStart
        this.onDoneListener = onDone
    }

    override fun isReady(): Boolean = true

    override fun setSpeechRate(rate: Float) {
        currentRate = rate
    }

    override fun getVoices(): List<TtsVoice> {
        return emptyList() // iOS voices are currently not accessible due to KMP mapping issues, will fix in next iteration
    }

    override fun setVoice(id: String) {
        selectedVoiceId = id
    }
}

actual fun createTextToSpeechService(platformContext: Any?): TextToSpeechService {
    return IosTextToSpeechService()
}
