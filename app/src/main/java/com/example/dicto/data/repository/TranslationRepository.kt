package com.example.dicto.data.repository

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

private const val TAG = "TranslationRepo"

/**
 * TranslationRepository - ML Kit implementation of translation data source
 *
 * Data Layer Component — injected as a @Singleton via Hilt.
 *
 * Self-healing design: if the ML Kit Translator is ever closed (e.g. because the
 * deprecated DictionaryViewModel closed its own copy), a fresh instance is created
 * automatically on the next call so translation always works after the user returns
 * to the app or reopens it from recents.
 *
 * Lifecycle contract:
 * - Do NOT call close() from ViewModel.onCleared() — the singleton must survive
 *   ViewModel recreation.
 * - close() should only be called from Application.onTerminate() (process shutdown).
 *   ML Kit does not require explicit cleanup; the OS reclaims resources when the
 *   process is killed.
 */
class TranslationRepository : ITranslationRepository {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ARABIC)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()

    // Mutable so we can replace a closed instance with a fresh one.
    @Volatile
    private var translator: Translator = Translation.getClient(options)

    @Volatile
    private var isClosed = false

    /** Returns the current translator, re-creating it if it was previously closed. */
    private fun getTranslator(): Translator {
        if (isClosed) {
            Log.d(TAG, "[TRANSLATOR] Translator was closed — creating a fresh instance")
            translator = Translation.getClient(options)
            isClosed = false
        }
        return translator
    }

    /**
     * Translate text from Arabic to English.
     */
    override suspend fun translateText(text: String): Result<String> {
        Log.d(TAG, "[TRANSLATE] translateText: '$text'")
        return try {
            val t = getTranslator()

            val conditions = DownloadConditions.Builder().requireWifi().build()
            Log.d(TAG, "[TRANSLATE] Ensuring model is ready...")
            t.downloadModelIfNeeded(conditions).await()
            Log.d(TAG, "[TRANSLATE] Model ready")

            val translatedText = t.translate(text).await()
            Log.d(TAG, "[TRANSLATE] Result: '$translatedText'")
            Result.success(translatedText)
        } catch (e: IllegalStateException) {
            // Translator was closed between getTranslator() and translate(); retry once.
            Log.w(TAG, "[TRANSLATE] Translator closed mid-call — retrying with fresh instance", e)
            isClosed = true
            return try {
                val t = getTranslator()
                val conditions = DownloadConditions.Builder().requireWifi().build()
                t.downloadModelIfNeeded(conditions).await()
                val translatedText = t.translate(text).await()
                Log.d(TAG, "[TRANSLATE] Retry succeeded: '$translatedText'")
                Result.success(translatedText)
            } catch (retryEx: Exception) {
                Log.e(TAG, "[TRANSLATE] Retry failed: ${retryEx.message}", retryEx)
                Result.failure(retryEx)
            }
        } catch (e: Exception) {
            Log.e(TAG, "[TRANSLATE] Error during translation: ${e.message} (Fix with AI)", e)
            Result.failure(e)
        }
    }

    /**
     * Check if translation model is downloaded.
     */
    override suspend fun isModelDownloaded(): Boolean {
        return try {
            val conditions = DownloadConditions.Builder().requireWifi().build()
            getTranslator().downloadModelIfNeeded(conditions).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "[MODEL_CHECK] Failed: ${e.message}", e)
            false
        }
    }

    /**
     * Explicitly download the translation model (no Wi-Fi restriction).
     */
    override suspend fun downloadModel(): Result<Unit> {
        return try {
            val conditions = DownloadConditions.Builder().build()
            getTranslator().downloadModelIfNeeded(conditions).await()
            Log.d(TAG, "[DOWNLOAD_MODEL] Model downloaded")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "[DOWNLOAD_MODEL] Failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Release the ML Kit translator.
     *
     * Only call this from Application.onTerminate() (i.e. process shutdown).
     * Do NOT call from ViewModel.onCleared() — the singleton must outlive ViewModels.
     */
    override fun close() {
        Log.d(TAG, "[CLOSE] Closing translator (should only happen on process shutdown)")
        isClosed = true
        translator.close()
    }
}

