package com.example.dicto

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.dicto.presentation.screens.DictionaryViewModel
import com.example.dicto.domain.model.DictionaryUiState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DictionaryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DictionaryViewModel
    private lateinit var mockApplication: Application

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockApplication = mockk(relaxed = true)
        viewModel = DictionaryViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ========================================
    // SEARCH QUERY TESTS
    // ========================================

    @Test
    fun `onQueryChanged updates search query`() = runTest(testDispatcher) {
        // When
        viewModel.onQueryChanged("hello")
        advanceUntilIdle()

        // Then
        assertEquals("hello", viewModel.searchQuery.value)
    }

    @Test
    fun `empty query returns Idle state`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            // Initial state
            assertEquals(DictionaryUiState.Idle, awaitItem())

            // Set empty query
            viewModel.onQueryChanged("")
            advanceTimeBy(700) // Past debounce

            // Should remain Idle
            expectNoEvents()
        }
    }

    @Test
    fun `blank query returns Idle state`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(DictionaryUiState.Idle, awaitItem())

            viewModel.onQueryChanged("   ")
            advanceTimeBy(700)

            expectNoEvents()
        }
    }

    @Test
    fun `search query is debounced for 600ms`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Skip initial Idle

            // Type multiple characters quickly
            viewModel.onQueryChanged("h")
            advanceTimeBy(100)

            viewModel.onQueryChanged("he")
            advanceTimeBy(100)

            viewModel.onQueryChanged("hel")
            advanceTimeBy(100)

            // No emission yet (less than 600ms)
            expectNoEvents()

            // After 600ms total debounce
            advanceTimeBy(400)

            // Should now emit Loading then Success/Error
            assertEquals(DictionaryUiState.Loading, awaitItem())

            // Skip the result (Success or Error depending on translation API)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========================================
    // CLIPBOARD MONITORING TESTS
    // ========================================

    @Test
    fun `clipboard monitoring is enabled by default`() = runTest(testDispatcher) {
        assertTrue(viewModel.clipboardMonitoringEnabled.value)
    }

    @Test
    fun `toggleClipboardMonitoring changes state`() = runTest(testDispatcher) {
        // Initially enabled
        assertTrue(viewModel.clipboardMonitoringEnabled.value)

        // Toggle off
        viewModel.toggleClipboardMonitoring()
        assertFalse(viewModel.clipboardMonitoringEnabled.value)

        // Toggle on
        viewModel.toggleClipboardMonitoring()
        assertTrue(viewModel.clipboardMonitoringEnabled.value)
    }

    @Test
    fun `onClipboardTextFound updates query when monitoring enabled`() = runTest(testDispatcher) {
        viewModel.onClipboardTextFound("clipboard text")
        advanceUntilIdle()

        assertEquals("clipboard text", viewModel.searchQuery.value)
    }

    @Test
    fun `onClipboardTextFound ignores when monitoring disabled`() = runTest(testDispatcher) {
        viewModel.toggleClipboardMonitoring() // Disable
        viewModel.onClipboardTextFound("clipboard text")
        advanceUntilIdle()

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `onClipboardTextFound ignores blank text`() = runTest(testDispatcher) {
        viewModel.onClipboardTextFound("   ")
        advanceUntilIdle()

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `onClipboardTextFound ignores duplicate text`() = runTest(testDispatcher) {
        // Set initial query
        viewModel.onQueryChanged("same text")
        advanceUntilIdle()

        // Try to set same text from clipboard
        val initialValue = viewModel.searchQuery.value
        viewModel.onClipboardTextFound("same text")
        advanceUntilIdle()

        // Should not trigger new query
        assertEquals(initialValue, viewModel.searchQuery.value)
    }

    // ========================================
    // PHRASE BUILDING TESTS
    // ========================================

    @Test
    fun `onPhraseSelectionChanged with empty list clears phrase`() = runTest(testDispatcher) {
        viewModel.onPhraseSelectionChanged(emptyList())
        advanceUntilIdle()

        assertEquals("", viewModel.selectedPhrase.value)
        assertEquals(null, viewModel.phraseTranslation.value)
    }

    @Test
    fun `onPhraseSelectionChanged joins words with space`() = runTest(testDispatcher) {
        viewModel.onPhraseSelectionChanged(listOf("hello", "world"))
        advanceUntilIdle()

        assertEquals("hello world", viewModel.selectedPhrase.value)
    }

    @Test
    fun `onPhraseSelectionChanged triggers translation`() = runTest(testDispatcher) {
        viewModel.phraseTranslation.test {
            skipItems(1) // Skip initial null

            viewModel.onPhraseSelectionChanged(listOf("test", "phrase"))
            advanceUntilIdle()

            // Should have a translation (might be empty string if API fails)
            val translation = awaitItem()
            assertTrue(translation != null)
        }
    }

    // ========================================
    // WORD SAVING TESTS
    // ========================================

    @Test
    fun `toggleSave is called without exceptions`() = runTest(testDispatcher) {
        // Should not throw
        viewModel.toggleSave("test word")
        advanceUntilIdle()
    }

    // ========================================
    // UI STATE TESTS
    // ========================================

    @Test
    fun `uiState emits Loading before translation`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Skip initial Idle

            viewModel.onQueryChanged("test")
            advanceTimeBy(700) // Past debounce

            // Should emit Loading
            assertEquals(DictionaryUiState.Loading, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple queries cancel previous translation`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Skip initial Idle

            // First query
            viewModel.onQueryChanged("first")
            advanceTimeBy(700)
            skipItems(1) // Skip Loading

            // Second query before first completes
            viewModel.onQueryChanged("second")
            advanceTimeBy(700)

            // Should show Loading for second query
            assertEquals(DictionaryUiState.Loading, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing query returns to Idle state`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Skip initial Idle

            // Set query
            viewModel.onQueryChanged("test")
            advanceTimeBy(700)
            skipItems(2) // Skip Loading and result

            // Clear query
            viewModel.onQueryChanged("")
            advanceTimeBy(700)

            // Should return to Idle
            assertEquals(DictionaryUiState.Idle, awaitItem())
        }
    }
}

