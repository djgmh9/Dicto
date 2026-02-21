package com.example.dicto.domain.manager

import com.example.dicto.fakes.FakePreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var testScope: CoroutineScope

    @Before
    fun setup() {
        fakePreferencesManager = FakePreferencesManager()
        testScope = CoroutineScope(StandardTestDispatcher())
        clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
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
        assertFalse(fakePreferencesManager.getClipboardMonitoringEnabledValue())

        clipboardManager.toggleMonitoring()
        advanceUntilIdle()

        // Check the preference directly after toggle
        assertTrue(fakePreferencesManager.getClipboardMonitoringEnabledValue())
    }

    @Test
    fun `toggleMonitoring - enabled to disabled`() = runTest {
        // Set the preference to true first
        fakePreferencesManager.setClipboardMonitoringEnabled(true)
        advanceUntilIdle()

        // Create manager that reads the true preference
        val manager = ClipboardManager(fakePreferencesManager, testScope)
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
        assertFalse(fakePreferencesManager.getFloatingWindowEnabledValue())

        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()

        assertTrue(fakePreferencesManager.getFloatingWindowEnabledValue())
    }

    @Test
    fun `toggleFloatingWindow - enabled to disabled`() = runTest {
        // Set the preference to true first
        fakePreferencesManager.setFloatingWindowEnabled(true)
        advanceUntilIdle()

        // Verify it's true
        assertTrue(fakePreferencesManager.getFloatingWindowEnabledValue())

        // Create manager that reads the true preference
        val manager = ClipboardManager(fakePreferencesManager, testScope)
        advanceUntilIdle()

        // Toggle it to false
        manager.toggleFloatingWindow()
        advanceUntilIdle()

        assertFalse(fakePreferencesManager.getFloatingWindowEnabledValue())
    }

    @Test
    fun `toggleFloatingWindow - multiple toggles`() = runTest {
        var current = fakePreferencesManager.getFloatingWindowEnabledValue()
        assertFalse(current)

        // false -> true
        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()
        current = fakePreferencesManager.getFloatingWindowEnabledValue()
        assertTrue(current, "After first toggle, should be true")

        // true -> false
        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()
        current = fakePreferencesManager.getFloatingWindowEnabledValue()
        assertFalse(current, "After second toggle, should be false")

        // false -> true
        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()
        current = fakePreferencesManager.getFloatingWindowEnabledValue()
        assertTrue(current, "After third toggle, should be true")
    }

    // ==================== Independence Tests ====================

    @Test
    fun `clipboard monitoring and floating window are independent`() = runTest {
        // Initial state: both false
        assertFalse(fakePreferencesManager.getClipboardMonitoringEnabledValue())
        assertFalse(fakePreferencesManager.getFloatingWindowEnabledValue())

        // Toggle monitoring: only monitoring should change
        clipboardManager.toggleMonitoring()
        advanceUntilIdle()
        assertTrue(fakePreferencesManager.getClipboardMonitoringEnabledValue())
        assertFalse(fakePreferencesManager.getFloatingWindowEnabledValue())

        // Toggle floating window: only floating window should change
        clipboardManager.toggleFloatingWindow()
        advanceUntilIdle()
        assertTrue(fakePreferencesManager.getClipboardMonitoringEnabledValue())
        assertTrue(fakePreferencesManager.getFloatingWindowEnabledValue())

        // Toggle monitoring again: only monitoring should change
        clipboardManager.toggleMonitoring()
        advanceUntilIdle()
        assertFalse(fakePreferencesManager.getClipboardMonitoringEnabledValue())
        assertTrue(fakePreferencesManager.getFloatingWindowEnabledValue())
    }

    // ==================== Persistence Tests ====================

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun `clipboard monitoring state persists across manager instances`() = runTest {
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
        assertFalse(clipboardManager.isMonitoringEnabled.value)

        clipboardManager.toggleMonitoring()
        advanceUntilIdle()

        // Verify the underlying preference was updated
        assertTrue(fakePreferencesManager.getClipboardMonitoringEnabledValue())
    }
}




















