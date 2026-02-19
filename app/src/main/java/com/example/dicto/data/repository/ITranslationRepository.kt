package com.example.dicto.data.repository

/**
 * TranslationRepository - Interface for translation data source
 *
 * Abstraction layer that allows swapping implementations:
 * - ML Kit (offline)
 * - API-based (online)
 * - Cached results
 * - Multiple providers
 */
interface ITranslationRepository {
    suspend fun translateText(text: String): Result<String>
    suspend fun isModelDownloaded(): Boolean
    suspend fun downloadModel(): Result<Unit>
    fun close()
}

