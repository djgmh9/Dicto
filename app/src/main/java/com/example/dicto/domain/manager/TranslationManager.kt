package com.example.dicto.domain.manager

import android.util.Log
import com.example.dicto.data.repository.ITranslationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private const val TAG = "TranslationMgr"

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
    private val repository: ITranslationRepository
) {
    /**
     * Translate a full sentence
     */
    suspend fun translateSentence(query: String): Result<String> {
        Log.d(TAG, "[SENTENCE] Translating sentence: '$query'")
        return try {
            val result = repository.translateText(query)
            if (result.isSuccess) {
                Log.d(TAG, "[SENTENCE] Success: '${result.getOrDefault("")}'")
            } else {
                Log.e(TAG, "[SENTENCE] Failed to translate")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "[SENTENCE] Error translating sentence: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Split sentence into unique words and translate each
     */
    suspend fun translateWords(query: String): Result<List<TranslatedWord>> {
        Log.d(TAG, "[WORDS] Translating words from: '$query'")
        return try {
            coroutineScope {
                // 1. Split into words (Unicode aware)
                val uniqueWords = query.trim()
                    .split(Regex("[^\\p{L}]+"))
                    .filter { it.isNotEmpty() }
                    .distinctBy { it.lowercase() }

                Log.d(TAG, "[WORDS] Found ${uniqueWords.size} unique words: $uniqueWords")

                // 2. Translate words in parallel
                val translatedWords = uniqueWords.map { word ->
                    async {
                        Log.d(TAG, "[WORDS] Translating word: '$word'")
                        val translation = repository.translateText(word).getOrDefault("")
                        Log.d(TAG, "[WORDS] Word '$word' -> '$translation'")
                        TranslatedWord(word, translation)
                    }
                }.awaitAll()

                Log.d(TAG, "[WORDS] Completed translating ${translatedWords.size} words")
                Result.success(translatedWords)
            }
        } catch (e: Exception) {
            Log.e(TAG, "[WORDS] Error translating words: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Translate a phrase (selected words)
     */
    suspend fun translatePhrase(words: List<String>): Result<String> {
        Log.d(TAG, "[PHRASE] Translating phrase: $words")
        return try {
            if (words.isEmpty()) {
                Log.d(TAG, "[PHRASE] Empty phrase, returning empty string")
                return Result.success("")
            }
            val combinedPhrase = words.joinToString(" ")
            Log.d(TAG, "[PHRASE] Combined phrase: '$combinedPhrase'")
            val result = repository.translateText(combinedPhrase)
            if (result.isSuccess) {
                Log.d(TAG, "[PHRASE] Success: '${result.getOrDefault("")}'")
            } else {
                Log.e(TAG, "[PHRASE] Failed to translate")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "[PHRASE] Error translating phrase: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun close() {
        Log.d(TAG, "[CLOSE] Closing TranslationManager")
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

