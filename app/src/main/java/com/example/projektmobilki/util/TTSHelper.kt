package com.example.projektmobilki.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TTSHelper(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isInitialized = true
            }
        }
    }

    fun speak(text: String) {
        if (!SettingsRepository.getInstance(context).isSoundEnabled.value) return
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
