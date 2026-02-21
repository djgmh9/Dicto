package com.example.dicto.domain.manager

/**
 * IPronunciationManager - Interface for text-to-speech operations
 *
 * This interface defines the contract for pronunciation management,
 * allowing for different implementations (real TTS, fake for testing, etc.)
 */
interface IPronunciationManager {
    /**
     * Speak text in Arabic
     */
    fun speakArabic(text: String)

    /**
     * Speak text in English
     */
    fun speakEnglish(text: String)

    /**
     * Stop current pronunciation
     */
    fun stop()

    /**
     * Shutdown and release resources
     */
    fun shutdown()
}

