package com.example.dicto.utils.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

/**
 * ClipboardMonitor - Handles clipboard monitoring logic
 *
 * Single Responsibility: Monitor clipboard changes and notify via callback
 * This follows the separation of concerns pattern by isolating clipboard logic
 * from UI and ViewModel concerns.
 *
 * Modes:
 * - ONE_TIME: Check clipboard once when monitoring starts
 * - CONTINUOUS: Check clipboard continuously (every 1 second)
 */
class ClipboardMonitor(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val mode: MonitoringMode = MonitoringMode.ONE_TIME
) {
    private var monitoringJob: Job? = null
    private var lastClipboardText: String? = null
    private var onTextFound: ((String) -> Unit)? = null

    enum class MonitoringMode {
        ONE_TIME,      // Check clipboard once when monitoring starts
        CONTINUOUS     // Check clipboard continuously
    }

    /**
     * Start monitoring clipboard
     *
     * @param onNewText Callback invoked when new text is found
     *
     * Behavior depends on mode:
     * - ONE_TIME: Check clipboard once and stop
     * - CONTINUOUS: Check clipboard every 1 second
     */
    fun startMonitoring(onNewText: (String) -> Unit) {
        Log.d(TAG, "Starting clipboard monitoring (mode: $mode)")

        if (monitoringJob?.isActive == true) {
            Log.d(TAG, "Monitoring already active, skipping start")
            return
        }

        this.onTextFound = onNewText

        when (mode) {
            MonitoringMode.ONE_TIME -> {
                // Check once immediately and stop
                Log.d(TAG, "ONE_TIME mode: checking clipboard once")
                checkClipboard()
                // Job is not created for ONE_TIME mode
            }
            MonitoringMode.CONTINUOUS -> {
                // Check immediately on start
                checkClipboard()

                // Start continuous monitoring
                monitoringJob = lifecycleScope.launch {
                    while (isActive) {
                        delay(CLIPBOARD_CHECK_INTERVAL_MS)
                        checkClipboard()
                    }
                }
            }
        }
    }

    /**
     * Stop monitoring clipboard
     */
    fun stopMonitoring() {
        Log.d(TAG, "Stopping clipboard monitoring")
        monitoringJob?.cancel()
        monitoringJob = null
    }

    /**
     * Check clipboard for new text
     * Only invokes callback if text is new and meets validation criteria
     */
    private fun checkClipboard() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // Check if clipboard has content
            if (!clipboard.hasPrimaryClip()) {
                Log.d(TAG, "No primary clip in clipboard")
                return
            }

            val clip = clipboard.primaryClip
            if (clip == null || clip.itemCount == 0) {
                Log.d(TAG, "Clipboard clip is null or empty")
                return
            }

            val item = clip.getItemAt(0)
            val text = extractTextFromClipboardItem(item, context)

            if (text == null) {
                Log.d(TAG, "Could not extract text from clipboard item")
                return
            }

            Log.d(TAG, "Clipboard text: '$text', Last: '$lastClipboardText'")

            // Only trigger if text is different from last processed
            if (isNewText(text)) {
                Log.d(TAG, "✓ Found new clipboard text: $text")
                lastClipboardText = text
                onTextFound?.invoke(text)
            } else {
                Log.d(TAG, "⊘ Skipping clipboard text - duplicate or invalid")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Clipboard error: ${e.message}", e)
        }
    }

    /**
     * Validate that text is new and should trigger translation
     */
    private fun isNewText(text: String): Boolean {
        return text.isNotBlank() && text != lastClipboardText
    }

    /**
     * Extract text from clipboard item with fallback strategies
     */
    private fun extractTextFromClipboardItem(
        item: android.content.ClipData.Item,
        context: Context
    ): String? {
        return when {
            item.text != null -> item.text.toString()
            item.coerceToText(context) != null -> item.coerceToText(context).toString()
            else -> null
        }
    }

    companion object {
        private const val TAG = "ClipboardMonitor"
        private const val CLIPBOARD_CHECK_INTERVAL_MS = 1000L // Check every 1 second
    }
}

