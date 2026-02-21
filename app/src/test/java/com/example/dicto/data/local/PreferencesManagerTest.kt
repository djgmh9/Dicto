package com.example.dicto.data.local

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * PreferencesManagerTest - Unit tests for app settings persistence
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PreferencesManagerTest {

    private lateinit var context: Context
    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        preferencesManager = DefaultPreferencesManager(context)
    }

    @Test
    fun `default values - matches expectations`() = runTest {
        // Critical: clipboard monitoring must be FALSE by default to prevent auto-start bug
        val clipboardEnabled = preferencesManager.clipboardMonitoringEnabled.first()
        assertFalse("Clipboard monitoring should be DISABLED (false) by default to fix auto-start bug", clipboardEnabled)

        val floatingEnabled = preferencesManager.floatingWindowEnabled.first()
        assertFalse("Floating window should be disabled by default", floatingEnabled)

        val xPosition = preferencesManager.floatingButtonX.first()
        assertEquals("Default X position should be 0", 0, xPosition)

        val yPosition = preferencesManager.floatingButtonY.first()
        assertEquals("Default Y position should be 100", 100, yPosition)
    }

    @Test
    fun `setClipboardMonitoringEnabled - updates value correctly`() = runTest {
        preferencesManager.setClipboardMonitoringEnabled(false)
        assertFalse(preferencesManager.clipboardMonitoringEnabled.first())
        
        preferencesManager.setClipboardMonitoringEnabled(true)
        assertTrue(preferencesManager.clipboardMonitoringEnabled.first())
    }

    @Test
    fun `setFloatingWindowEnabled - updates value correctly`() = runTest {
        preferencesManager.setFloatingWindowEnabled(true)
        assertTrue(preferencesManager.floatingWindowEnabled.first())
        
        preferencesManager.setFloatingWindowEnabled(false)
        assertFalse(preferencesManager.floatingWindowEnabled.first())
    }

    @Test
    fun `setFloatingButtonPosition - updates X and Y correctly`() = runTest {
        preferencesManager.setFloatingButtonPosition(-250, 1500)
        
        assertEquals(-250, preferencesManager.floatingButtonX.first())
        assertEquals(1500, preferencesManager.floatingButtonY.first())
    }
}
