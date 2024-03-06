package com.example.foregroundservice.STT

import android.R.attr
import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.lang.Exception
import java.util.*
import android.R.attr.data




class Stt(
    private val app: Application,
    private val listener: SttListener
) : SttEngine() {

    override var speechRecognizer: SpeechRecognizer? = SpeechRecognizer.createSpeechRecognizer(app)
    override var speechIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    override var audioManager: AudioManager =
        app.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override var restartSpeechHandler: Handler = Handler(Looper.getMainLooper())
    override var partialResultSpeechHandler: Handler = Handler(Looper.getMainLooper())

    override var listeningTime: Long = 0
    override var pauseAndSpeakTime: Long = 0
    override var finalSpeechResultFound: Boolean = false
    override var onReadyForSpeech: Boolean = false
    override var partialRestartActive: Boolean = false
    override var showProgressView: Boolean = false

    override var speechResult: MutableLiveData<String> = MutableLiveData()
    override var speechFrequency: MutableLiveData<Float> = MutableLiveData()

    init {
        speechIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechIntent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        speechIntent.putExtra("android.speech.extra.GET_AUDIO", true);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, app.packageName)
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

    }


    override fun startSpeechRecognition() {
        onReadyForSpeech = false
        if (partialRestartActive) partialRestartActive = false else speechResult.value = ""

        listeningTime = System.currentTimeMillis()
        pauseAndSpeakTime = listeningTime
        finalSpeechResultFound = false
        speechRecognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                onReadyForSpeech = true
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("test-audio ", "hey audio")
                Log.d("test-audio ", buffer.contentToString())
            }

            override fun onEndOfSpeech() {}

            override fun onError(errorCode: Int) {
                Log.d("error", getErrorTextFromCode(errorCode))
                val errDuration = System.currentTimeMillis() - listeningTime
                if (errDuration < 5000L && errorCode == SpeechRecognizer.ERROR_NO_MATCH && !onReadyForSpeech) return

                // Disabling/Enabling audio based on "audio beep disabled timeout",
                mute(onReadyForSpeech && errDuration < 30000L)

                if (arrayOf(
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                        SpeechRecognizer.ERROR_AUDIO,
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                    ).any { it == errorCode }
                ) {
                    // Restarting speech recognition
                    restartSpeechRecognition(errorCode == SpeechRecognizer.ERROR_RECOGNIZER_BUSY)
                } else {
                    listener.onSttSpeechError(getErrorTextFromCode(errorCode))

                    // Resetting on ready for speech status
                    onReadyForSpeech = false

                    // Closing speech operations
                    closeSpeechOperations()
                }
            }

            override fun onResults(results: Bundle?) {
                val speechData = speechIntent.extras.parseSpeechResult()
                Log.d("test simple",speechIntent.dataString.toString())
                Log.d("test simple",speechIntent.data.toString())
                val result = results.parseSpeechResult()
                if (result.valid) {
                    listener.onSttFinalSpeechResult(result.speechResult)
                    restartSpeechRecognition(true)
                } else {
                    restartSpeechRecognition(false)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                if (finalSpeechResultFound) return

                val partialResult = partialResults.parseSpeechResult()
                if (partialResult.valid) {
                    listener.onSttLiveSpeechResult(partialResult.speechResult)
                    // Updating the speech observer with the live result
                    speechResult.value = partialResult.speechResult

                    if ((System.currentTimeMillis() - pauseAndSpeakTime) > 1000L) {
                        // Final Speech result found
                        finalSpeechResultFound = true

                        partialResultSpeechHandler.postDelayed({
                            closeSpeechOperations()

                            listener.onSttFinalSpeechResult(partialResult.speechResult)

                            startSpeechRecognition() //starting speech recognition, for continuous speech recog.

                        }, 500L)
                    } else {
                        pauseAndSpeakTime = System.currentTimeMillis()
                    }
                } else {
                    pauseAndSpeakTime = System.currentTimeMillis()
                }
            }

            override fun onEvent(p0: Int, p1: Bundle?) {}

        })

        speechRecognizer?.startListening(speechIntent)
    }

    override fun restartSpeechRecognition(partialRestart: Boolean) {
        restartSpeechHandler.postDelayed({
            partialRestartActive = partialRestart
            startSpeechRecognition() //Starting speech recognition after a delay
        }, 1000L)
    }

    override fun closeSpeechOperations() {
        // Destroying the speech recognizer
        speechRecognizer?.destroy()

        // Removing any running callbacks if applicable
        restartSpeechHandler.removeCallbacksAndMessages(null)
        partialResultSpeechHandler.removeCallbacksAndMessages(null)

        // If audio was muted, resetting it back to normal
        mute(false)
    }

    override fun mute(mute: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                    0
                )
            } else {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute)
            }
        } catch (e: Exception) {
            e.printStackTrace()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_UNMUTE,
                    0
                )
            } else {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
            }
        }
    }

    fun getErrorTextFromCode(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
    }

    /**
     * Speech recognition Parsers
     */
    data class SpeechResult(val valid: Boolean, val speechResult: String)

    fun Bundle?.parseSpeechResult(): SpeechResult {
        if (this == null) return SpeechResult(false, "")
        if (!this.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
            return SpeechResult(false, "")
        }
        if (this.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) == null) {
            return SpeechResult(false, "")
        }

        val result = this.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!!

        return if (result.size > 0 && result[0].trim().isNotEmpty()) {
            SpeechResult(true, result[0].trim())
        } else {
            SpeechResult(false, "")
        }
    }
}