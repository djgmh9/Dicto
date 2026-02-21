package com.example.dicto.presentation.screens.saved

import com.example.dicto.domain.model.DictionaryUiState
import com.example.dicto.domain.manager.TranslationManager
import com.example.dicto.fakes.FakePronunciationManager
import com.example.dicto.fakes.FakeTranslationRepository
import com.example.dicto.fakes.FakeWordStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
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

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())

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
    fun testInitialStateIsLoading() = runTest {
        val state = viewModel.uiState.first()
        assertIs<DictionaryUiState.Loading>(state)
    }

    @Test
    fun testEmptySavedWordsShowsSuccess() = runTest {
        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        assertIs<DictionaryUiState.Success>(state)

        val successState = state as DictionaryUiState.Success
        assertTrue(successState.wordTranslations.isEmpty())
    }

    @Test
    fun testSavedWordsDisplay() = runTest {
        fakeWordStorage.save("word1")
        fakeWordStorage.save("word2")

        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        val successState = state as DictionaryUiState.Success

        assertEquals(2, successState.wordTranslations.size)
    }

    @Test
    fun testSavedWordsAreTranslated() = runTest {
        fakeWordStorage.save("hello")

        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        val successState = state as DictionaryUiState.Success

        assertTrue(successState.wordTranslations.any { it.original == "hello" })
        assertTrue(successState.wordTranslations.all { it.translation.isNotEmpty() })
    }

    @Test
    fun testDeleteWord() = runTest {
        fakeWordStorage.save("word1")

        viewModel.onDeleteWord("word1")

        val saved = fakeWordStorage.getSavedWords()
        assertTrue(!saved.contains("word1"))
    }

    @Test
    fun testSearchFilter() = runTest {
        fakeWordStorage.save("hello")
        fakeWordStorage.save("world")

        viewModel.onSearchQueryChanged("hello")

        val filtered = viewModel.filteredWords.first()
        assertEquals(1, filtered.size)
        assertEquals("hello", filtered[0].original)
    }

    @Test
    fun testSearchFilterEmpty() = runTest {
        fakeWordStorage.save("hello")
        fakeWordStorage.save("world")

        viewModel.onSearchQueryChanged("nonexistent")

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
    fun testErrorHandling() = runTest {
        fakeRepository.setShouldFail(true, "Translation service error")

        // Create new viewModel to trigger error with failing repository
        val errorViewModel = SavedWordsViewModel(
            fakeWordStorage,
            TranslationManager(fakeRepository),
            fakePronunciation
        )

        fakeWordStorage.save("test")

        val state = errorViewModel.uiState.first { it is DictionaryUiState.Error }
        assertIs<DictionaryUiState.Error>(state)

        val errorState = state as DictionaryUiState.Error
        assertTrue(errorState.message.contains("Translation service error"))
    }

    @Test
    fun testBackwardCompatibility() = runTest {
        fakeWordStorage.save("test1")
        fakeWordStorage.save("test2")

        val words = viewModel.savedWordsList.first()
        assertEquals(2, words.size)
    }


    @Test
    fun testWordsSortedReverseOrder() = runTest {
        fakeWordStorage.save("first")
        fakeWordStorage.save("second")
        fakeWordStorage.save("third")

        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        val successState = state as DictionaryUiState.Success

        // Words are saved in reverse order
        assertEquals("third", successState.wordTranslations[0].original)
    }
}

