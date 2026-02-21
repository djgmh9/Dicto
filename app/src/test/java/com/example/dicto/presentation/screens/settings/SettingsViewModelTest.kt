package com.example.dicto.presentation.screens.settings

import com.example.dicto.fakes.FakeClipboardManager
import com.example.dicto.fakes.FakeFloatingWindowManager
import com.example.dicto.fakes.FakePreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var fakePreferencesManager: FakePreferencesManager
    private lateinit var fakeClipboardManager: FakeClipboardManager
    private lateinit var fakeFloatingWindowManager: FakeFloatingWindowManager

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())

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
        assertFalse(state)
    }

    @Test
    fun testInitialFloatingWindowState() = runTest {
        val state = viewModel.floatingWindowEnabled.first()
        assertFalse(state)
    }

    @Test
    fun testToggleClipboardMonitoring() = runTest {
        viewModel.toggleClipboardMonitoring()

        val state = fakeClipboardManager.getMonitoringEnabled()
        assertTrue(state)
    }

    @Test
    fun testToggleClipboardMonitoringMultipleTimes() = runTest {
        viewModel.toggleClipboardMonitoring()
        assertTrue(fakeClipboardManager.getMonitoringEnabled())

        viewModel.toggleClipboardMonitoring()
        assertFalse(fakeClipboardManager.getMonitoringEnabled())

        viewModel.toggleClipboardMonitoring()
        assertTrue(fakeClipboardManager.getMonitoringEnabled())
    }

    @Test
    fun testToggleFloatingWindow() = runTest {
        viewModel.toggleFloatingWindow()

        val state = fakeClipboardManager.getFloatingWindowEnabled()
        assertTrue(state)
    }

    @Test
    fun testToggleFloatingWindowMultipleTimes() = runTest {
        viewModel.toggleFloatingWindow()
        assertTrue(fakeClipboardManager.getFloatingWindowEnabled())

        viewModel.toggleFloatingWindow()
        assertFalse(fakeClipboardManager.getFloatingWindowEnabled())
    }

    @Test
    fun testClipboardMonitoringStateReflected() = runTest {
        viewModel.toggleClipboardMonitoring()

        val state = viewModel.clipboardMonitoringEnabled.first { it }
        assertTrue(state)
    }

    @Test
    fun testFloatingWindowStateReflected() = runTest {
        viewModel.toggleFloatingWindow()

        val state = viewModel.floatingWindowEnabled.first { it }
        assertTrue(state)
    }

    @Test
    fun testIndependentToggles() = runTest {
        viewModel.toggleClipboardMonitoring()

        val clipboardState = fakeClipboardManager.getMonitoringEnabled()
        val floatingState = fakeClipboardManager.getFloatingWindowEnabled()

        assertTrue(clipboardState)
        assertFalse(floatingState)
    }
}

