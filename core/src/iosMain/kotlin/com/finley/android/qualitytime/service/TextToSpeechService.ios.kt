package com.finley.android.qualitytime.service

import com.finley.android.qualitytime.util.AppLog
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.setActive
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IosTextToSpeechService : TextToSpeechService {

    companion object {
        private const val TAG = "TTS"
    }

    private val synthesizer = AVSpeechSynthesizer()
    private var onStartListener: ((String) -> Unit)? = null
    private var onDoneListener: ((String) -> Unit)? = null
    private var currentRate: Float = 1.0f
    private var selectedVoiceId: String? = null

    /**
     * Maps the concrete utterance object to its logical id. Keyed by object
     * identity (not by text) so that repeated lines no longer collide — the
     * previous text-keyed map broke auto-advance for poems with duplicate lines.
     */
    private val utteranceIds = mutableMapOf<AVSpeechUtterance, String>()

    private val delegate = object : NSObject(), AVSpeechSynthesizerDelegateProtocol {
        @ObjCSignatureOverride
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didStartSpeechUtterance: AVSpeechUtterance) {
            utteranceIds[didStartSpeechUtterance]?.let { onStartListener?.invoke(it) }
        }

        @ObjCSignatureOverride
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didFinishSpeechUtterance: AVSpeechUtterance) {
            val id = utteranceIds.remove(didFinishSpeechUtterance) ?: return
            onDoneListener?.invoke(id)
        }
    }

    init {
        synthesizer.delegate = delegate
        // Route audio through the loudspeaker for louder playback.
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryPlayback,
            withOptions = AVAudioSessionCategoryOptionDefaultToSpeaker,
            error = null
        )
        session.setActive(true, error = null)
    }

    override fun speak(text: String, utteranceId: String, enqueue: Boolean) {
        if (!enqueue) {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)

        val voice = selectedVoiceId?.let {
            AVSpeechSynthesisVoice.voiceWithIdentifier(it)
        } ?: AVSpeechSynthesisVoice.voiceWithLanguage("zh-CN")
        utterance.voice = voice
        // 0.5 is AVSpeechUtterance default rate; scale it by the user's speed setting.
        utterance.rate = 0.5f * currentRate

        utteranceIds[utterance] = utteranceId
        synthesizer.speakUtterance(utterance)
        AppLog.d(TAG) { "speak(id=$utteranceId, enqueue=$enqueue): $text" }
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
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
        // iOS voices enumeration is deferred; current K/N mapping may not expose
        // AVSpeechSynthesisVoice properties (identifier/name/language) directly.
        // Return an empty list with the option to extend later.
        return emptyList()
    }

    override fun setVoice(id: String) {
        selectedVoiceId = id
    }
}

actual fun createTextToSpeechService(platformContext: Any?): TextToSpeechService {
    return IosTextToSpeechService()
}