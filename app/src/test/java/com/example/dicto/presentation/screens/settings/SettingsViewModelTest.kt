package com.example.dicto.presentation.screens.settings

import com.example.dicto.fakes.FakeClipboardManager
import com.example.dicto.fakes.FakeFloatingWindowManager
import com.example.dicto.fakes.FakePreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var fakePreferencesManager: FakePreferencesManager
    private lateinit var fakeClipboardManager: FakeClipboardManager
    private lateinit var fakeFloatingWindowManager: FakeFloatingWindowManager
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakePreferencesManager = FakePreferencesManager()
        fakeClipboardManager = FakeClipboardManager()
        fakeFloatingWindowManager = FakeFloatingWindowManager()

        viewModel = SettingsViewModel(
            fakePreferencesManager,
            fakeClipboardManager,
            fakeFloatingWindowManager
        )
    }

    @Test
    fun testInitialClipboardMonitoringState() = runTest {
        val state = viewModel.clipboardMonitoringEnabled.first()
        assertFalse(state) // Default is now false for privacy
    }

    @Test
    fun testInitialFloatingWindowState() = runTest {
        val state = viewModel.floatingWindowEnabled.first()
        assertFalse(state)
    }

    @Test
    fun testToggleClipboardMonitoring() = runTest {
        viewModel.toggleClipboardMonitoring()
        advanceUntilIdle()

        val state = fakeClipboardManager.getMonitoringEnabled()
        assertTrue(state) // Toggles from false to true
    }

    @Test
    fun testToggleClipboardMonitoringMultipleTimes() = runTest {
        // Initial state is false
        viewModel.toggleClipboardMonitoring()
        advanceUntilIdle()
        assertTrue(fakeClipboardManager.getMonitoringEnabled()) // false -> true

        viewModel.toggleClipboardMonitoring()
        advanceUntilIdle()
        assertFalse(fakeClipboardManager.getMonitoringEnabled()) // true -> false

        viewModel.toggleClipboardMonitoring()
        advanceUntilIdle()
        assertTrue(fakeClipboardManager.getMonitoringEnabled()) // false -> true
    }

    @Test
    fun testToggleFloatingWindow() = runTest {
        viewModel.toggleFloatingWindow()
        advanceUntilIdle()

        val state = fakeClipboardManager.getFloatingWindowEnabled()
        assertTrue(state) // Toggles from false to true
    }

    @Test
    fun testToggleFloatingWindowMultipleTimes() = runTest {
        // Initial state is false
        viewModel.toggleFloatingWindow()
        advanceUntilIdle()
        assertTrue(fakeClipboardManager.getFloatingWindowEnabled()) // false -> true

        viewModel.toggleFloatingWindow()
        advanceUntilIdle()
        assertFalse(fakeClipboardManager.getFloatingWindowEnabled()) // true -> false
    }

    @Test
    fun testClipboardMonitoringStateReflected() = runTest {
        // Initial state is false, so toggle makes it true
        viewModel.toggleClipboardMonitoring()
        advanceUntilIdle()

        val state = viewModel.clipboardMonitoringEnabled.first()
        assertTrue(state)
    }

    @Test
    fun testFloatingWindowStateReflected() = runTest {
        viewModel.toggleFloatingWindow()
        advanceUntilIdle()

        val state = viewModel.floatingWindowEnabled.first()
        assertTrue(state)
    }

    @Test
    fun testIndependentToggles() = runTest {
        viewModel.toggleClipboardMonitoring()
        advanceUntilIdle()

        val clipboardState = fakeClipboardManager.getMonitoringEnabled()
        val floatingState = fakeClipboardManager.getFloatingWindowEnabled()

        assertTrue(clipboardState) // Toggled from false to true
        assertFalse(floatingState) // Remains false (not toggled)
    }
}

