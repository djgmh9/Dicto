package com.example.dicto.domain

import android.util.Log
import com.example.dicto.data.repository.TranslationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * TranslationManager - Handles all translation-related logic
 *
 * Single Responsibility: Manage translation operations
 * Features:
 * - Full sentence translation
 * - Word-by-word translation
 * - Parallel translation requests
 *
 * Separated from ViewModel for:
 * - Easier testing
 * - Reusability
 * - Clear separation of concerns
 */
class TranslationManager(
    private val repository: TranslationRepository
) {
    /**
     * Translate a full sentence
     */
    suspend fun translateSentence(query: String): Result<String> {
        return try {
            val result = repository.translateText(query)
            result
        } catch (e: Exception) {
            Log.e("TranslationManager", "Error translating sentence: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Split sentence into unique words and translate each
     */
    suspend fun translateWords(query: String): Result<List<TranslatedWord>> {
        return try {
            coroutineScope {
                // 1. Split into words (Unicode aware)
                val uniqueWords = query.trim()
                    .split(Regex("[^\\p{L}]+"))
                    .filter { it.isNotEmpty() }
                    .distinctBy { it.lowercase() }

                // 2. Translate words in parallel
                val translatedWords = uniqueWords.map { word ->
                    async {
                        val translation = repository.translateText(word).getOrDefault("")
                        TranslatedWord(word, translation)
                    }
                }.awaitAll()

                Result.success(translatedWords)
            }
        } catch (e: Exception) {
            Log.e("TranslationManager", "Error translating words: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Translate a phrase (selected words)
     */
    suspend fun translatePhrase(words: List<String>): Result<String> {
        return try {
            if (words.isEmpty()) {
                return Result.success("")
            }
            val combinedPhrase = words.joinToString(" ")
            repository.translateText(combinedPhrase)
        } catch (e: Exception) {
            Log.e("TranslationManager", "Error translating phrase: ${e.message}")
            Result.failure(e)
        }
    }

    fun close() {
        repository.close()
    }
}

/**
 * Data class for translated word
 */
data class TranslatedWord(
    val original: String,
    val translation: String
)

