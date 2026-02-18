package com.example.dicto

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClipboardMonitoringIntegrationTest {

    private lateinit var context: Context
    private lateinit var clipboardManager: ClipboardManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    @Test
    fun clipboard_CanSetAndRetrieveText() {
        // Given
        val testText = "Hello World"
        val clip = ClipData.newPlainText("test", testText)

        // When
        clipboardManager.setPrimaryClip(clip)

        // Then
        assertTrue(clipboardManager.hasPrimaryClip())
        val retrievedText = clipboardManager.primaryClip?.getItemAt(0)?.text.toString()
        assertEquals(testText, retrievedText)
    }

    @Test
    fun clipboard_CanRetrieveArabicText() {
        // Given
        val arabicText = "مرحبا بالعالم"
        val clip = ClipData.newPlainText("arabic", arabicText)

        // When
        clipboardManager.setPrimaryClip(clip)

        // Then
        val retrievedText = clipboardManager.primaryClip?.getItemAt(0)?.text.toString()
        assertEquals(arabicText, retrievedText)
    }

    @Test
    fun clipboard_HandlesHtmlContent() {
        // Given
        val htmlText = "<b>Bold text</b>"
        val clip = ClipData.newHtmlText("html", "Bold text", htmlText)

        // When
        clipboardManager.setPrimaryClip(clip)

        // Then
        assertTrue(clipboardManager.hasPrimaryClip())
        val item = clipboardManager.primaryClip?.getItemAt(0)
        assertNotNull(item)

        // Should be able to coerce to text
        val coercedText = item?.coerceToText(context).toString()
        assertTrue(coercedText.isNotBlank())
    }

    @Test
    fun clipboard_HandlesEmptyClip() {
        // Given
        val emptyClip = ClipData.newPlainText("empty", "")

        // When
        clipboardManager.setPrimaryClip(emptyClip)

        // Then
        assertTrue(clipboardManager.hasPrimaryClip())
        val text = clipboardManager.primaryClip?.getItemAt(0)?.text.toString()
        assertEquals("", text)
    }

    @Test
    fun clipboard_MultipleItemsInClip() {
        // Given
        val clip = ClipData.newPlainText("first", "First item")
        clip.addItem(ClipData.Item("Second item"))
        clip.addItem(ClipData.Item("Third item"))

        // When
        clipboardManager.setPrimaryClip(clip)

        // Then
        val primaryClip = clipboardManager.primaryClip
        assertNotNull(primaryClip)
        assertEquals(3, primaryClip?.itemCount ?: )
        assertEquals("First item", primaryClip?.getItemAt(0)?.text.toString())
        assertEquals("Second item", primaryClip?.getItemAt(1)?.text.toString())
        assertEquals("Third item", primaryClip?.getItemAt(2)?.text.toString())
    }

    @Test
    fun clipboard_MimeTypeDetection() {
        // Given
        val plainTextClip = ClipData.newPlainText("plain", "Plain text")

        // When
        clipboardManager.setPrimaryClip(plainTextClip)

        // Then
        val description = clipboardManager.primaryClipDescription
        assertNotNull(description)
        assertTrue(description?.hasMimeType("text/plain") ?: )
    }

    @Test
    fun clipboard_CoerceToTextFromIntent() = runTest {
        // Given - URI or Intent based clip
        val clip = ClipData.newPlainText("label", "Some text")

        // When
        clipboardManager.setPrimaryClip(clip)

        // Then
        val item = clipboardManager.primaryClip?.getItemAt(0)
        val coercedText = item?.coerceToText(context)
        assertNotNull(coercedText)
        assertEquals("Some text", coercedText.toString())
    }
}

/**
 * These integration tests verify that clipboard operations work correctly.
 * They test the actual Android clipboard system behavior that MainActivity relies on.
 *
 * Run these tests on a device or emulator using:
 * ./gradlew connectedAndroidTest
 */

