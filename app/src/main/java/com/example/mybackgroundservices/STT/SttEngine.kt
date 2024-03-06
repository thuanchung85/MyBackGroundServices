package com.example.foregroundservice.STT

import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.speech.SpeechRecognizer
import androidx.lifecycle.MutableLiveData

abstract class SttEngine {

    protected abstract var speechRecognizer: SpeechRecognizer?

    protected abstract var speechIntent: Intent

    protected abstract var audioManager: AudioManager

    protected abstract var restartSpeechHandler: Handler

    protected abstract var partialResultSpeechHandler: Handler

    protected abstract var listeningTime: Long

    protected abstract var pauseAndSpeakTime: Long

    protected abstract var finalSpeechResultFound: Boolean

    protected abstract var onReadyForSpeech: Boolean

    protected abstract var partialRestartActive: Boolean

    protected abstract var showProgressView: Boolean

    protected abstract var speechResult: MutableLiveData<String>

    protected abstract var speechFrequency: MutableLiveData<Float>

    /**
     * Starts the speech recognition
     */
    abstract fun startSpeechRecognition()

    /**
     * Restarts the speech recognition
     * @param partialRestart The partial restart status
     */
    protected abstract fun restartSpeechRecognition(partialRestart: Boolean)

    /**
     * Closes the speech operations
     */
    abstract fun closeSpeechOperations()

    /**
     * Mutes the audio
     * @param mute Boolean The mute audio status
     */
    protected abstract fun mute(mute: Boolean)
}