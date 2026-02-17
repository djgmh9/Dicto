import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslationRepository {

    // For this example, let's hardcode English -> German.
    // In a real app, you would pass these as arguments.
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ARABIC)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()

    private val translator = Translation.getClient(options)

    // Suspend function to be called from Coroutines
    suspend fun translateText(text: String): Result<String> {
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

    fun close() {
        translator.close()
    }
}