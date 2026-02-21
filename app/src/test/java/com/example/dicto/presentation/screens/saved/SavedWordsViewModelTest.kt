package com.example.dicto.presentation.screens.saved

import com.example.dicto.domain.model.DictionaryUiState
import com.example.dicto.domain.manager.TranslationManager
import com.example.dicto.fakes.FakePronunciationManager
import com.example.dicto.fakes.FakeTranslationRepository
import com.example.dicto.fakes.FakeWordStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class SavedWordsViewModelTest {

    private lateinit var viewModel: SavedWordsViewModel
    private lateinit var fakeRepository: FakeTranslationRepository
    private lateinit var fakeWordStorage: FakeWordStorage
    private lateinit var fakePronunciation: FakePronunciationManager
    private lateinit var translationManager: TranslationManager
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakeRepository = FakeTranslationRepository()
        fakeWordStorage = FakeWordStorage()
        fakePronunciation = FakePronunciationManager()
        translationManager = TranslationManager(fakeRepository)

        viewModel = SavedWordsViewModel(
            fakeWordStorage,
            translationManager,
            fakePronunciation
        )
    }

    @Test
    fun testEmptySavedWordsShowsSuccess() = runTest {
        advanceUntilIdle() // Give flows time to emit
        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        assertIs<DictionaryUiState.Success>(state)

        val successState = state
        assertTrue(successState.wordTranslations.isEmpty())
    }

    @Test
    fun testSavedWordsDisplay() = runTest {
        fakeWordStorage.save("word1")
        fakeWordStorage.save("word2")
        advanceUntilIdle() // Wait for flows to process

        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        val successState = state as DictionaryUiState.Success

        assertEquals(2, successState.wordTranslations.size)
    }

    @Test
    fun testSavedWordsAreTranslated() = runTest {
        fakeWordStorage.save("hello")
        advanceUntilIdle() // Wait for flows to process

        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        val successState = state as DictionaryUiState.Success

        assertTrue(successState.wordTranslations.any { it.original == "hello" })
        assertTrue(successState.wordTranslations.all { it.translation.isNotEmpty() })
    }

    @Test
    fun testDeleteWord() = runTest {
        fakeWordStorage.save("word1")
        advanceUntilIdle()

        viewModel.onDeleteWord("word1")
        advanceUntilIdle()

        val saved = fakeWordStorage.getSavedWords()
        assertTrue(!saved.contains("word1"), "Word should be removed from storage")
    }

    @Test
    fun testSearchFilter() = runTest {
        fakeWordStorage.save("hello")
        fakeWordStorage.save("world")
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("hello")
        advanceUntilIdle()

        val filtered = viewModel.filteredWords.first()
        assertEquals(1, filtered.size, "Should have 1 filtered result")
        assertEquals("hello", filtered[0].original)
    }

    @Test
    fun testSearchFilterEmpty() = runTest {
        fakeWordStorage.save("hello")
        fakeWordStorage.save("world")
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("nonexistent")
        advanceUntilIdle()

        val filtered = viewModel.filteredWords.first()
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun testSearchQueryUpdates() = runTest {
        val query = "search query"
        viewModel.onSearchQueryChanged(query)

        val collectedQuery = viewModel.searchQuery.first()
        assertEquals(query, collectedQuery)
    }

    @Test
    fun testPlayPronunciation() {
        val text = "كلمة"
        viewModel.onPlayPronunciation(text)

        assertEquals(1, fakePronunciation.speakArabicCallCount)
        assertEquals(text, fakePronunciation.lastSpokeText)
    }

    @Test
    fun testStopPronunciation() {
        viewModel.onStopPronunciation()

        assertEquals(1, fakePronunciation.stopCallCount)
    }

    @Test
    fun testMultipleSavedWords() = runTest {
        // Test with multiple words to ensure they're all processed
        fakeWordStorage.save("first")
        fakeWordStorage.save("second")
        fakeWordStorage.save("third")
        advanceUntilIdle()

        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        val successState = state as DictionaryUiState.Success

        assertEquals(3, successState.wordTranslations.size)
        // Verify all words are translated (even if translation is just reversed)
        assertTrue(successState.wordTranslations.all { it.translation.isNotEmpty() })
    }

    @Test
    fun testBackwardCompatibility() = runTest {
        fakeWordStorage.save("test1")
        fakeWordStorage.save("test2")
        advanceUntilIdle()

        val words = viewModel.savedWordsList.first()
        assertEquals(2, words.size)
    }


    @Test
    fun testWordsSortedReverseOrder() = runTest {
        fakeWordStorage.save("first")
        fakeWordStorage.save("second")
        fakeWordStorage.save("third")
        advanceUntilIdle()

        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        val successState = state as DictionaryUiState.Success

        // Words are saved in reverse order
        assertEquals("third", successState.wordTranslations[0].original)
    }
}

