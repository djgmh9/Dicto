package com.example.dicto.utils.logging

/**
 * FloatingWindowLogger - Floating window feature logger
 */
object FloatingWindowLogger {
    private val logger = LoggerProvider.getLogger()

    fun serviceCreated() = logger.debug(LogTags.FLOATING, ">>> Service created")
    fun onStartCommand() = logger.debug(LogTags.FLOATING, ">>> onStartCommand() called")
    fun startingForeground() = logger.debug(LogTags.FLOATING, ">>> Starting foreground service")
    fun foregroundStarted() = logger.debug(LogTags.FLOATING, ">>> Foreground service started")
    fun buttonShown(x: Int, y: Int) = logger.debug(LogTags.FLOATING, ">>> Button shown at x=$x, y=$y")
    fun buttonMoved(x: Int, y: Int) = logger.debug(LogTags.FLOATING, ">>> Button moved to x=$x, y=$y")
    fun positionSaved(x: Int, y: Int) = logger.debug(LogTags.FLOATING, ">>> Position saved: x=$x, y=$y")
    fun loadedPosition(x: Int, y: Int) = logger.debug(LogTags.FLOATING, ">>> Loaded saved position: x=$x, y=$y")
    fun error(message: String, throwable: Throwable? = null) = logger.error(LogTags.FLOATING, message, throwable)
}

/**
 * ClipboardLogger - Clipboard monitoring feature logger
 */
object ClipboardLogger {
    private val logger = LoggerProvider.getLogger()

    fun monitoringStarted() = logger.debug(LogTags.CLIPBOARD, ">>> Clipboard monitoring started")
    fun monitoringStopped() = logger.debug(LogTags.CLIPBOARD, ">>> Clipboard monitoring stopped")
    fun textCopied(text: String) = logger.debug(LogTags.CLIPBOARD, ">>> Clipboard content: ${text.take(50)}...")
    fun autoTranslateTriggered(text: String) = logger.debug(LogTags.CLIPBOARD, ">>> Auto-translate triggered: ${text.take(50)}...")
    fun noTextInClipboard() = logger.warn(LogTags.CLIPBOARD, ">>> No text in clipboard")
}

/**
 * TranslationLogger - Translation feature logger
 */
object TranslationLogger {
    private val logger = LoggerProvider.getLogger()

    fun translationStarted(text: String) = logger.debug(LogTags.TRANSLATE, ">>> Translation started: ${text.take(50)}...")
    fun translationSuccess(original: String, translation: String) = logger.debug(LogTags.TRANSLATE, ">>> Translation success: $original → $translation")
    fun translationError(text: String, error: String) = logger.error(LogTags.TRANSLATE, ">>> Translation failed for: ${text.take(50)}... Error: $error")
}

/**
 * LifecycleLogger - App lifecycle logger
 */
object LifecycleLogger {
    private val logger = LoggerProvider.getLogger()

    fun activityCreated(activityName: String) = logger.info(LogTags.LIFECYCLE, ">>> $activityName.onCreate()")
    fun activityStarted(activityName: String) = logger.info(LogTags.LIFECYCLE, ">>> $activityName.onStart()")
    fun activityResumed(activityName: String) = logger.info(LogTags.LIFECYCLE, ">>> $activityName.onResume()")
    fun activityPaused(activityName: String) = logger.info(LogTags.LIFECYCLE, ">>> $activityName.onPause()")
    fun activityStopped(activityName: String) = logger.info(LogTags.LIFECYCLE, ">>> $activityName.onStop()")
    fun activityDestroyed(activityName: String) = logger.info(LogTags.LIFECYCLE, ">>> $activityName.onDestroy()")
}

/**
 * StorageLogger - Data persistence logger
 */
object StorageLogger {
    private val logger = LoggerProvider.getLogger()

    fun wordSaved(word: String) = logger.debug(LogTags.STORAGE, ">>> Word saved: $word")
    fun wordDeleted(word: String) = logger.debug(LogTags.STORAGE, ">>> Word deleted: $word")
    fun loadingWords() = logger.debug(LogTags.STORAGE, ">>> Loading saved words")
    fun wordsLoaded(count: Int) = logger.debug(LogTags.STORAGE, ">>> Loaded $count words")
}

/**
 * TTSLogger - Text-to-speech logger
 */
object TTSLogger {
    private val logger = LoggerProvider.getLogger()

    fun speaking(text: String) = logger.debug(LogTags.TTS, ">>> Speaking: ${text.take(50)}...")
    fun stopped() = logger.debug(LogTags.TTS, ">>> TTS stopped")
    fun error(message: String) = logger.error(LogTags.TTS, ">>> TTS Error: $message")
}

/**
 * PermissionLogger - Permission handling logger
 */
object PermissionLogger {
    private val logger = LoggerProvider.getLogger()

    fun permissionRequested(permission: String) = logger.info(LogTags.PERMISSION, ">>> Requesting permission: $permission")
    fun permissionGranted(permission: String) = logger.info(LogTags.PERMISSION, ">>> Permission granted: $permission")
    fun permissionDenied(permission: String) = logger.warn(LogTags.PERMISSION, ">>> Permission denied: $permission")
}

