package com.example.dicto.fakes

import com.example.dicto.domain.manager.IPronunciationManager

/**
 * Fake implementation of IPronunciationManager for testing
 * Does nothing but tracks calls for verification
 */
class FakePronunciationManager : IPronunciationManager {
    var lastSpokeText: String? = null
        private set
    var lastSpokeEnglishText: String? = null
        private set
    var speakArabicCallCount = 0
        private set
    var speakEnglishCallCount = 0
        private set
    var stopCallCount = 0
        private set
    var shutdownCallCount = 0
        private set

    override fun speakArabic(text: String) {
        speakArabicCallCount++
        lastSpokeText = text
    }

    override fun speakEnglish(text: String) {
        speakEnglishCallCount++
        lastSpokeEnglishText = text
    }

    override fun stop() {
        stopCallCount++
    }

    override fun shutdown() {
        shutdownCallCount++
    }

    fun reset() {
        lastSpokeText = null
        lastSpokeEnglishText = null
        speakArabicCallCount = 0
        speakEnglishCallCount = 0
        stopCallCount = 0
        shutdownCallCount = 0
    }
}

