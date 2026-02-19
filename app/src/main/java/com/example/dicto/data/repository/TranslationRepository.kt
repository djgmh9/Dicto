package com.example.dicto.data.repository

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/**
 * TranslationRepository - ML Kit implementation of translation data source
 *
 * Data Layer Component
 * Responsibilities:
 * - Manage ML Kit translator lifecycle
 * - Handle model downloading
 * - Perform text translation
 * - Abstract translation data source from domain layer
 *
 * Currently uses ML Kit for offline translation
 * Can be extended to support:
 * - Online translation APIs
 * - Multiple translation providers
 * - Caching strategies
 */
class TranslationRepository : ITranslationRepository {

    // For this example, hardcode arabic to english
    // In a real app, you would pass these as arguments or use dependency injection
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ARABIC)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()

    private val translator = Translation.getClient(options)

    /**
     * Translate text from source to target language
     *
     * @param text Text to translate
     * @return Result containing translated text or error
     */
    override suspend fun translateText(text: String): Result<String> {
        return try {
            // 1. Check if model needs downloading
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            // This ensures the model is ready. If it's already there, it does nothing.
            // If not, it downloads it (this might take a few seconds the first time).
            translator.downloadModelIfNeeded(conditions).await()

            // 2. Perform translation
            val translatedText = translator.translate(text).await()
            Result.success(translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if translation model is downloaded
     */
    override suspend fun isModelDownloaded(): Boolean {
        return try {
            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Explicitly download the translation model
     */
    override suspend fun downloadModel(): Result<Unit> {
        return try {
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Close the translator and free resources
     */
    override fun close() {
        translator.close()
    }
}

