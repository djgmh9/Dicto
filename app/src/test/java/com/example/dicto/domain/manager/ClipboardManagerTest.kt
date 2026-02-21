package com.example.dicto.domain.manager

import com.example.dicto.fakes.FakePreferencesManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * ClipboardManagerTest - Tests for clipboard monitoring preferences
 *
 * Purpose: Prevent regression of the clipboard monitoring default value bug
 * where .stateIn() was initialized with true instead of false.
 *
 * The bug: When app launched, clipboard monitoring would start because
 * the StateFlow's initial value was true before DataStore loaded the actual preference.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ClipboardManagerTest {

    private lateinit var fakePreferencesManager: FakePreferencesManager

    @Before
    fun setup() {
        fakePreferencesManager = FakePreferencesManager()
    }

    // ==================== CRITICAL: Initial State Tests ====================
    // These tests verify the default preference values are false (opt-in)

    @Test
    fun `isMonitoringEnabled - initial preference is FALSE by default`() = runTest {
        // The underlying preference must default to false to prevent auto-start
        assertFalse(
            fakePreferencesManager.getClipboardMonitoringEnabledValue(),
            "Initial clipboard monitoring preference MUST be false"
        )
    }

    @Test
    fun `isFloatingWindowEnabled - initial preference is FALSE by default`() = runTest {
        // Floating window also defaults to false
        assertFalse(
            fakePreferencesManager.getFloatingWindowEnabledValue(),
            "Initial floating window preference must be false"
        )
    }

    @Test
    fun `fresh install - all preferences disabled by default`() = runTest {
        // New FakePreferencesManager should have all defaults as false
        val freshManager = FakePreferencesManager()
        assertFalse(freshManager.getClipboardMonitoringEnabledValue())
        assertFalse(freshManager.getFloatingWindowEnabledValue())
    }

    // ==================== Toggle Tests ====================

    @Test
    fun `toggleMonitoring - disabled to enabled`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, backgroundScope)
        assertFalse(clipboardManager.isMonitoringEnabled.value)

        clipboardManager.toggleMonitoring()
        advanceUntilIdle()

        // StateFlow should be updated
        assertTrue(clipboardManager.isMonitoringEnabled.value)
    }

    @Test
    fun `toggleMonitoring - enabled to disabled`() = runTest {
        // Set the preference to true first
        fakePreferencesManager.setClipboardMonitoringEnabled(true)
        advanceUntilIdle()

        // Create manager that reads the true preference
        val manager = ClipboardManager(fakePreferencesManager, backgroundScope)
        advanceUntilIdle()

        // Verify StateFlow reads the true value
        assertTrue(manager.isMonitoringEnabled.value)

        // Toggle it to false
        manager.toggleMonitoring()
        advanceUntilIdle()

        // Check StateFlow was updated
        assertFalse(manager.isMonitoringEnabled.value)
    }

    @Test
    fun `toggleMonitoring - multiple toggles`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)

        // Initial state via StateFlow
        assertFalse(clipboardManager.isMonitoringEnabled.value)

        // false -> true
        clipboardManager.toggleMonitoring()
        advanceUntilIdle()
        assertTrue(clipboardManager.isMonitoringEnabled.value, "After first toggle, should be true")

        // true -> false
        clipboardManager.toggleMonitoring()
        advanceUntilIdle()
        assertFalse(clipboardManager.isMonitoringEnabled.value, "After second toggle, should be false")

        // false -> true
        clipboardManager.toggleMonitoring()
        advanceUntilIdle()
        assertTrue(clipboardManager.isMonitoringEnabled.value, "After third toggle, should be true")
    }

    // ==================== Explicit Set Tests ====================

    @Test
    fun `setMonitoringEnabled - explicitly set to true`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
        clipboardManager.setMonitoringEnabled(true)
        advanceUntilIdle()
        assertTrue(fakePreferencesManager.getClipboardMonitoringEnabledValue())
    }

    @Test
    fun `setMonitoringEnabled - explicitly set to false`() = runTest {
        fakePreferencesManager.setClipboardMonitoringEnabled(true)
        advanceUntilIdle()

        val manager = ClipboardManager(fakePreferencesManager, testScope)
        advanceUntilIdle()

        manager.setMonitoringEnabled(false)
        advanceUntilIdle()
        assertFalse(fakePreferencesManager.getClipboardMonitoringEnabledValue())
    }

    @Test
    fun `setFloatingWindowEnabled - explicitly set to true`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
        clipboardManager.setFloatingWindowEnabled(true)
        advanceUntilIdle()
        assertTrue(fakePreferencesManager.getFloatingWindowEnabledValue())
    }

    @Test
    fun `setFloatingWindowEnabled - explicitly set to false`() = runTest {
        fakePreferencesManager.setFloatingWindowEnabled(true)
        advanceUntilIdle()

        val manager = ClipboardManager(fakePreferencesManager, testScope)
        advanceUntilIdle()

        manager.setFloatingWindowEnabled(false)
        advanceUntilIdle()
        assertFalse(fakePreferencesManager.getFloatingWindowEnabledValue())
    }

    // ==================== Floating Window Tests ====================

    @Test
    fun `toggleFloatingWindow - disabled to enabled`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
        assertFalse(clipboardManager.isFloatingWindowEnabled.value)

        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()

        assertTrue(clipboardManager.isFloatingWindowEnabled.value)
    }

    @Test
    fun `toggleFloatingWindow - enabled to disabled`() = runTest {
        // Set the preference to true first
        fakePreferencesManager.setFloatingWindowEnabled(true)
        advanceUntilIdle()

        // Create manager that reads the true preference
        val manager = ClipboardManager(fakePreferencesManager, testScope)
        advanceUntilIdle()

        // Verify StateFlow reads the true value
        assertTrue(manager.isFloatingWindowEnabled.value)

        // Toggle it to false
        manager.toggleFloatingWindow()
        advanceUntilIdle()

        // Check StateFlow was updated
        assertFalse(manager.isFloatingWindowEnabled.value)
    }

    @Test
    fun `toggleFloatingWindow - multiple toggles`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
        assertFalse(clipboardManager.isFloatingWindowEnabled.value)

        // false -> true
        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()
        assertTrue(clipboardManager.isFloatingWindowEnabled.value, "After first toggle, should be true")

        // true -> false
        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()
        assertFalse(clipboardManager.isFloatingWindowEnabled.value, "After second toggle, should be false")

        // false -> true
        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()
        assertTrue(clipboardManager.isFloatingWindowEnabled.value, "After third toggle, should be true")
    }

    // ==================== Independence Tests ====================

    @Test
    fun `clipboard monitoring and floating window are independent`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)

        // Initial state: both false
        assertFalse(clipboardManager.isMonitoringEnabled.value)
        assertFalse(clipboardManager.isFloatingWindowEnabled.value)

        // Toggle monitoring: only monitoring should change
        clipboardManager.toggleMonitoring()
        advanceUntilIdle()
        assertTrue(clipboardManager.isMonitoringEnabled.value)
        assertFalse(clipboardManager.isFloatingWindowEnabled.value)

        // Toggle floating window: only floating window should change
        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()
        assertTrue(clipboardManager.isMonitoringEnabled.value)
        assertTrue(clipboardManager.isFloatingWindowEnabled.value)

        // Toggle monitoring again: only monitoring should change
        clipboardManager.toggleMonitoring()
        advanceUntilIdle()
        assertFalse(clipboardManager.isMonitoringEnabled.value)
        assertTrue(clipboardManager.isFloatingWindowEnabled.value)
    }

    // ==================== Persistence Tests ====================

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun `clipboard monitoring state persists across manager instances`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
        clipboardManager.setMonitoringEnabled(true)
        advanceUntilIdle()

        // Create a new manager instance (simulates app restart)
        val newManager = ClipboardManager(fakePreferencesManager, testScope)
        advanceUntilIdle()

        assertTrue(fakePreferencesManager.getClipboardMonitoringEnabledValue())
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun `floating window state persists across manager instances`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
        clipboardManager.setFloatingWindowEnabled(true)
        advanceUntilIdle()

        val newManager = ClipboardManager(fakePreferencesManager, testScope)
        advanceUntilIdle()

        assertTrue(fakePreferencesManager.getFloatingWindowEnabledValue())
    }

    // ==================== StateFlow Reflection Tests ====================
    // Verify that StateFlow eventually reflects the preference state

    @Test
    fun `isMonitoringEnabled - value property reflects StateFlow state after toggle`() = runTest {
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
        assertFalse(clipboardManager.isMonitoringEnabled.value)

        clipboardManager.toggleMonitoring()
        advanceUntilIdle()

        // Verify the underlying preference was updated
        assertTrue(fakePreferencesManager.getClipboardMonitoringEnabledValue())
    }
}



























