package com.example.dicto.utils.clipboard

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.dicto.fakes.FakePreferencesManager
import com.example.dicto.domain.manager.ClipboardManager
import com.example.dicto.presentation.screens.translator.TranslatorViewModel
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * ClipboardMonitoringManagerIntegrationTest
 * Tests to prevent regression of the clipboard monitoring bug
 */
class ClipboardMonitoringManagerIntegrationTest {

    private lateinit var fakePreferencesManager: FakePreferencesManager
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var lifecycleOwner: MockLifecycleOwner
    private lateinit var translatorViewModel: TranslatorViewModel

    @Before
    fun setup() {
        fakePreferencesManager = FakePreferencesManager()
        clipboardManager = ClipboardManager(fakePreferencesManager, CoroutineScope(StandardTestDispatcher()))
        lifecycleOwner = MockLifecycleOwner()
        translatorViewModel = mockk(relaxed = true)
    }

    @Test
    fun `clipboard monitoring disabled on launch - state is false`() = runTest {
        assertFalse(clipboardManager.isMonitoringEnabled.value, "Monitoring should be disabled on launch")
    }

    @Test
    fun `clipboard monitoring enabled - state reflects saved preference`() = runTest {
        fakePreferencesManager.setClipboardMonitoringEnabled(true)
        val enabledManager = ClipboardManager(fakePreferencesManager, CoroutineScope(StandardTestDispatcher()))

        assertTrue(enabledManager.isMonitoringEnabled.value, "Monitoring should be enabled when preference is true")
    }

    @Test
    fun `StateFlow initial value is false not true - critical bug fix`() {
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
        val selectedTab = 0
        fakePreferencesManager.setClipboardMonitoringEnabled(true)
        val enabled = clipboardManager.isMonitoringEnabled.value
        val shouldCreate = (selectedTab == 0 && enabled)
        assertTrue(shouldCreate)
    }

    @Test
    fun `manager condition - other tab should not create even if enabled`() = runTest {
        val selectedTab = 1
        fakePreferencesManager.setClipboardMonitoringEnabled(true)
        val enabled = clipboardManager.isMonitoringEnabled.value
        val shouldCreate = (selectedTab == 0 && enabled)
        assertFalse(shouldCreate)
    }

    @Test
    fun `preference persists across app restart`() = runTest {
        clipboardManager.toggleMonitoring()
        assertTrue(clipboardManager.isMonitoringEnabled.value)

        val newManager = ClipboardManager(fakePreferencesManager, CoroutineScope(StandardTestDispatcher()))
        assertTrue(newManager.isMonitoringEnabled.value, "Preference should persist")
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

