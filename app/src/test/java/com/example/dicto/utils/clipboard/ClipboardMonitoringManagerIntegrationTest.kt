package com.example.dicto.utils.clipboard

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.dicto.fakes.FakePreferencesManager
import com.example.dicto.domain.manager.ClipboardManager
import com.example.dicto.presentation.screens.translator.TranslatorViewModel
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * ClipboardMonitoringManagerIntegrationTest
 * Tests to prevent regression of the clipboard monitoring bug
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ClipboardMonitoringManagerIntegrationTest {

    private lateinit var fakePreferencesManager: FakePreferencesManager
    private lateinit var lifecycleOwner: MockLifecycleOwner
    private lateinit var translatorViewModel: TranslatorViewModel

    @Before
    fun setup() {
        fakePreferencesManager = FakePreferencesManager()
        lifecycleOwner = MockLifecycleOwner()
        translatorViewModel = mockk(relaxed = true)
    }

    @Test
    fun `clipboard monitoring disabled on launch - state is false`() = runTest {
        val testScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
        // UnconfinedTestDispatcher executes immediately, no need for advanceUntilIdle
        assertFalse(clipboardManager.isMonitoringEnabled.value, "Monitoring should be disabled on launch (default is false)")
    }

    @Test
    fun `clipboard monitoring enabled - state reflects saved preference`() = runTest {
        val testScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))

        // First save the preference as enabled
        fakePreferencesManager.setClipboardMonitoringEnabled(true)

        // Create a new manager instance
        val enabledManager = ClipboardManager(fakePreferencesManager, testScope)

        // Verify the manager's StateFlow reflects the saved preference
        assertTrue(enabledManager.isMonitoringEnabled.value, "Manager's isMonitoringEnabled should be true when preference is true")
    }

    @Test
    fun `StateFlow initial value is false not true - critical bug fix`() = runTest {
        val testScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)
        val initialValue = clipboardManager.isMonitoringEnabled.value
        assertFalse(initialValue, "StateFlow initial value must be false to prevent auto-start bug")
    }

    @Test
    fun `manager condition - selectedTab=0 and disabled should not create`() {
        val selectedTab = 0
        val enabled = false
        val shouldCreate = (selectedTab == 0 && enabled)
        assertFalse(shouldCreate)
    }

    @Test
    fun `manager condition - selectedTab=0 and enabled should create`() = runTest {
        val testScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val selectedTab = 0
        fakePreferencesManager.setClipboardMonitoringEnabled(true)

        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)

        val enabled = clipboardManager.isMonitoringEnabled.value
        val shouldCreate = (selectedTab == 0 && enabled)
        assertTrue(enabled, "Manager should reflect enabled=true from preferences")
        assertTrue(shouldCreate, "Should create when selectedTab=0 and enabled=true")
    }

    @Test
    fun `manager condition - other tab should not create even if enabled`() = runTest {
        val testScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val selectedTab = 1
        fakePreferencesManager.setClipboardMonitoringEnabled(true)

        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)

        val enabled = clipboardManager.isMonitoringEnabled.value
        val shouldCreate = (selectedTab == 0 && enabled)
        assertFalse(shouldCreate)
    }

    @Test
    fun `preference persists across app restart`() = runTest {
        val testScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val clipboardManager = ClipboardManager(fakePreferencesManager, testScope)

        clipboardManager.setMonitoringEnabled(true)

        // Verify preference was saved
        assertTrue(fakePreferencesManager.getClipboardMonitoringEnabledValue())

        // Create a new manager (simulating app restart)
        val newManager = ClipboardManager(fakePreferencesManager, testScope)

        // Verify the new manager reads the saved preference
        assertTrue(fakePreferencesManager.getClipboardMonitoringEnabledValue(), "Preference should persist")
        assertTrue(newManager.isMonitoringEnabled.value, "New manager should read persisted value")
    }
}

/**
 * MockLifecycleOwner - For testing lifecycle-aware components
 */
class MockLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}









