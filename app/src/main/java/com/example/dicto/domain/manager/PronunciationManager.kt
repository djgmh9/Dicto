package com.example.dicto.domain.manager

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope

/**
 * PronunciationManager - Handles all TTS/pronunciation logic
 *
 * Single Responsibility: Manage text-to-speech operations
 * Features:
 * - Speak text in Arabic
 * - Speak text in English
 * - Stop pronunciation
 * - Lifecycle management
 *
 * Separated from ViewModel for:
 * - Easier testing
 * - Reusability
 * - Clean resource management
 */
class PronunciationManager(
    application: Application,
    viewModelScope: CoroutineScope
) {
    private val ttsManager = TTSManager(application, viewModelScope).apply {
        initialize(
            onSuccess = {
                Log.d("PronunciationManager", "TTS Manager initialized successfully")
            },
            onError = { error ->
                Log.e("PronunciationManager", "TTS initialization error: $error")
            }
        )
    }

    /**
     * Pronounce word/text in Arabic (source language)
     */
    fun speakArabic(text: String) {
        if (text.isNotBlank()) {
            ttsManager.speak(text, java.util.Locale("ar"), onComplete = {
                Log.d("PronunciationManager", "Finished pronouncing Arabic: $text")
            })
        } else {
            Log.w("PronunciationManager", "Cannot pronounce empty Arabic text")
        }
    }

    /**
     * Pronounce word/text in English (target language)
     */
    fun speakEnglish(text: String) {
        if (text.isNotBlank()) {
            ttsManager.speak(text, java.util.Locale.ENGLISH, onComplete = {
                Log.d("PronunciationManager", "Finished pronouncing English: $text")
            })
        } else {
            Log.w("PronunciationManager", "Cannot pronounce empty English text")
        }
    }

    /**
     * Stop current pronunciation
     */
    fun stop() {
        ttsManager.stop()
        Log.d("PronunciationManager", "Stopped pronunciation")
    }

    /**
     * Shutdown TTS Manager and clean up resources
     */
    fun shutdown() {
        ttsManager.shutdown()
        Log.d("PronunciationManager", "TTS Manager shutdown")
    }
}

