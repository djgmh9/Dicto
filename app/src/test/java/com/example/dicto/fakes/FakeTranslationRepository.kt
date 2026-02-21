package com.example.dicto.fakes

import com.example.dicto.data.repository.ITranslationRepository

/**
 * Fake implementation of ITranslationRepository for testing
 * Returns predictable, consistent results without actual translation service
 */
class FakeTranslationRepository : ITranslationRepository {
    private var shouldFail = false
    private var failureMessage = "Translation failed"
    private var translationDelay = 0L
    private var modelDownloaded = true

    fun setShouldFail(fail: Boolean, message: String = "Translation failed") {
        shouldFail = fail
        failureMessage = message
    }

    fun setTranslationDelay(delayMs: Long) {
        translationDelay = delayMs
    }

    fun setModelDownloaded(downloaded: Boolean) {
        modelDownloaded = downloaded
    }

    override suspend fun translateText(text: String): Result<String> {
        if (translationDelay > 0) {
            kotlinx.coroutines.delay(translationDelay)
        }
        return if (shouldFail) {
            Result.failure(Exception(failureMessage))
        } else {
            // Simple reversal for testing
            Result.success(text.reversed())
        }
    }

    override suspend fun isModelDownloaded(): Boolean {
        return modelDownloaded
    }

    override suspend fun downloadModel(): Result<Unit> {
        return if (shouldFail) {
            Result.failure(Exception(failureMessage))
        } else {
            modelDownloaded = true
            Result.success(Unit)
        }
    }

    override fun close() {
        // No-op for fake
    }
}

