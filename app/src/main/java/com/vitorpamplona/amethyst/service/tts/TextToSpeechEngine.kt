/*
 * Created by Ayaan on 02/02/23, 10:30 pm
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 02/02/23, 9:56 pm
 */

package com.vitorpamplona.amethyst.service.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

const val DEF_SPEECH_AND_PITCH = 0.8f

fun getErrorText(errorCode: Int): String = when (errorCode) {
    TextToSpeech.ERROR -> "ERROR"
    TextToSpeech.ERROR_INVALID_REQUEST -> "ERROR_INVALID_REQUEST"
    TextToSpeech.ERROR_NETWORK -> "ERROR_NETWORK"
    TextToSpeech.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
    TextToSpeech.ERROR_SERVICE -> "ERROR_SERVICE"
    TextToSpeech.ERROR_SYNTHESIS -> "ERROR_SYNTHESIS"
    TextToSpeech.ERROR_NOT_INSTALLED_YET -> "ERROR_NOT_INSTALLED_YET"
    else -> "UNKNOWN"
}

class TextToSpeechEngine private constructor() {
    private var tts: TextToSpeech? = null

    private var defaultPitch = 0.8f
    private var defaultSpeed = 0.8f
    private var defLanguage = Locale.getDefault()
    private var onStartListener: (() -> Unit)? = null
    private var onDoneListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    private var onHighlightListener: ((Int, Int) -> Unit)? = null
    private var message: String? = null

    companion object {
        private var instance: TextToSpeechEngine? = null
        fun getInstance(): TextToSpeechEngine {
            if (instance == null) {
                instance = TextToSpeechEngine()
            }
            return instance!!
        }
    }

    fun initTTS(context: Context, message: String) {
        tts = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                tts!!.language = defLanguage
                tts!!.setPitch(defaultPitch)
                tts!!.setSpeechRate(defaultSpeed)
                tts!!.setListener(
                    onStart = {
                        onStartListener?.invoke()
                    },
                    onError = { e ->
                        e?.let { error ->
                            onErrorListener?.invoke(error)
                        }
                    },
                    onRange = { start, end ->
                        if (this@TextToSpeechEngine.message != null) {
                            onHighlightListener?.invoke(start, end)
                        }
                    },
                    onDone = {
                        onStartListener?.invoke()
                    }
                )
                speak(message)
            } else {
                onErrorListener?.invoke(getErrorText(it))
            }
        }
    }

    private fun speak(message: String): TextToSpeechEngine {
        tts!!.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
        )
        return this
    }

    fun setPitchAndSpeed(pitch: Float, speed: Float) {
        defaultPitch = pitch
        defaultSpeed = speed
    }

    fun resetPitchAndSpeed() {
        defaultPitch = DEF_SPEECH_AND_PITCH
        defaultSpeed = DEF_SPEECH_AND_PITCH
    }

    fun setLanguage(local: Locale): TextToSpeechEngine {
        this.defLanguage = local
        return this
    }

    fun setHighlightedMessage(message: String) {
        this.message = message
    }

    fun setOnStartListener(onStartListener: (() -> Unit)): TextToSpeechEngine {
        this.onStartListener = onStartListener
        return this
    }

    fun setOnCompletionListener(onDoneListener: () -> Unit): TextToSpeechEngine {
        this.onDoneListener = onDoneListener
        return this
    }

    fun setOnErrorListener(onErrorListener: (String) -> Unit): TextToSpeechEngine {
        this.onErrorListener = onErrorListener
        return this
    }

    fun setOnHighlightListener(onHighlightListener: (Int, Int) -> Unit): TextToSpeechEngine {
        this.onHighlightListener = onHighlightListener
        return this
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        instance = null
    }
}

inline fun TextToSpeech.setListener(
    crossinline onStart: (String?) -> Unit = {},
    crossinline onError: (String?) -> Unit = {},
    crossinline onRange: (Int, Int) -> Unit = { _, _ -> },
    crossinline onDone: (String?) -> Unit
) = this.apply {
    setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(p0: String?) {
            onStart.invoke(p0)
        }

        override fun onDone(p0: String?) {
            onDone.invoke(p0)
        }

        @Deprecated("Deprecated in Java", ReplaceWith("onError.invoke(p0)"))
        override fun onError(p0: String?) {
            onError.invoke(p0)
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            super.onRangeStart(utteranceId, start, end, frame)
            onRange.invoke(start, end)
        }
    })
}
