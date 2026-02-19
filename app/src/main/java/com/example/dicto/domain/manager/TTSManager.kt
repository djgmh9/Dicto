package com.example.dicto.domain.manager

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * TTSManager - Handles Text-to-Speech functionality
 *
 * Single Responsibility: Manage TTS lifecycle, initialization, and pronunciation
 * Features:
 * - Lazy initialization of TextToSpeech engine
 * - Error handling and recovery
 * - Language support (Arabic and English)
 * - Cleanup on app shutdown
 *
 * Responsibilities:
 * - Initialize TTS engine
 * - Handle TTS initialization errors
 * - Speak text in specified language
 * - Clean up resources
 *
 * Not Responsible For:
 * - UI rendering
 * - State management
 * - Coroutine scope (passed in)
 */
class TTSManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    companion object {
        private const val TAG = "TTSManager"
    }

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var initializationInProgress = false

    // Callbacks for initialization
    private var onInitSuccess: (() -> Unit)? = null
    private var onInitError: ((String) -> Unit)? = null

    /**
     * Initialize TTS engine
     * Safe to call multiple times - will skip if already initialized
     *
     * @param onSuccess Called when TTS is ready to use
     * @param onError Called if TTS initialization fails
     */
    fun initialize(
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (isInitialized) {
            Log.d(TAG, "TTS already initialized")
            onSuccess?.invoke()
            return
        }

        if (initializationInProgress) {
            Log.d(TAG, "TTS initialization already in progress")
            return
        }

        initializationInProgress = true
        onInitSuccess = onSuccess
        onInitError = onError

        coroutineScope.launch {
            try {
                textToSpeech = TextToSpeech(context) { status ->
                    handleInitializationStatus(status)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating TextToSpeech instance", e)
                isInitialized = false
                initializationInProgress = false
                onInitError?.invoke("Failed to initialize TTS: ${e.message}")
            }
        }
    }

    /**
     * Handle TTS initialization status callback
     */
    private fun handleInitializationStatus(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS initialized successfully")
            isInitialized = true
            initializationInProgress = false

            // Set language to English by default
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "English language not fully supported, using default")
            }

            onInitSuccess?.invoke()
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
            isInitialized = false
            initializationInProgress = false
            onInitError?.invoke("TTS initialization failed")
        }
    }

    /**
     * Speak the given text in the specified language
     *
     * @param text The text to speak
     * @param language The language code (e.g., Locale.ENGLISH, Locale("ar"))
     * @param onComplete Called when speech finishes
     */
    fun speak(
        text: String,
        language: Locale = Locale.ENGLISH,
        onComplete: (() -> Unit)? = null
    ) {
        if (text.isBlank()) {
            Log.w(TAG, "Cannot speak blank text")
            return
        }

        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, initializing now...")
            initialize(
                onSuccess = {
                    performSpeak(text, language, onComplete)
                },
                onError = { error ->
                    Log.e(TAG, "Failed to initialize TTS: $error")
                }
            )
            return
        }

        performSpeak(text, language, onComplete)
    }

    /**
     * Internal function to actually perform speech
     */
    private fun performSpeak(
        text: String,
        language: Locale,
        onComplete: (() -> Unit)?
    ) {
        try {
            textToSpeech?.let { tts ->
                // Set language
                val langResult = tts.setLanguage(language)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Language not supported: $language, using default")
                }

                // Stop any currently playing audio
                tts.stop()

                // Speak with completion callback
                val utteranceId = "utterance_${System.currentTimeMillis()}"
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(TAG, "Speech started: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d(TAG, "Speech completed: $utteranceId")
                        onComplete?.invoke()
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "Speech error: $utteranceId")
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        Log.e(TAG, "Speech error: $utteranceId - Code: $errorCode")
                    }
                })

                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                Log.d(TAG, "Speaking text: $text in language: ${language.displayName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during speech", e)
        }
    }

    /**
     * Stop current speech
     */
    fun stop() {
        try {
            textToSpeech?.stop()
            Log.d(TAG, "Speech stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech", e)
        }
    }

    /**
     * Check if TTS is currently initialized
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Clean up resources
     * Call this when ViewModel is destroyed or app is closing
     */
    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isInitialized = false
            Log.d(TAG, "TTS shutdown complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during TTS shutdown", e)
        }
    }
}