package com.example.dicto.presentation.screens.translator

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
class TranslatorViewModelTest {

    private lateinit var viewModel: TranslatorViewModel
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

        viewModel = TranslatorViewModel(
            translationManager,
            fakePronunciation,
            fakeWordStorage
        )
    }

    @Test
    fun testInitialStateIsIdle() = runTest {
        val state = viewModel.uiState.first()
        assertIs<DictionaryUiState.Idle>(state)
    }

    @Test
    fun testEmptyQueryRemainsIdle() = runTest {
        viewModel.onQueryChanged("")
        val state = viewModel.uiState.first()
        assertIs<DictionaryUiState.Idle>(state)
    }

    @Test
    fun testSuccessfulTranslation() = runTest {
        viewModel.onQueryChanged("hello")

        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        assertIs<DictionaryUiState.Success>(state)

        val successState = state
        assertTrue(successState.fullTranslation.isNotEmpty())
    }

    @Test
    fun testSaveWord() = runTest {
        viewModel.toggleSave("hello")
        advanceUntilIdle() // Wait for suspend operation to complete

        val saved = fakeWordStorage.getSavedWords()
        assertTrue(saved.contains("hello"))
    }

    @Test
    fun testUnsaveWord() = runTest {
        fakeWordStorage.save("hello")
        advanceUntilIdle() // Wait for save to complete

        viewModel.toggleSave("hello")
        advanceUntilIdle() // Wait for unsave to complete

        val saved = fakeWordStorage.getSavedWords()
        assertTrue(!saved.contains("hello"))
    }

    @Test
    fun testPlayPronunciation() {
        val text = "مرحبا"
        viewModel.pronounceOriginal(text)

        assertEquals(1, fakePronunciation.speakArabicCallCount)
        assertEquals(text, fakePronunciation.lastSpokeText)
    }

    @Test
    fun testStopPronunciation() {
        viewModel.stopPronunciation()

        assertEquals(1, fakePronunciation.stopCallCount)
    }

    @Test
    fun testSearchQueryUpdates() = runTest {
        val query = "test query"
        viewModel.onQueryChanged(query)

        val collectedQuery = viewModel.searchQuery.first()
        assertEquals(query, collectedQuery)
    }

    @Test
    fun testMultipleSearches() = runTest {
        viewModel.onQueryChanged("first")

        val state1 = viewModel.uiState.first { it is DictionaryUiState.Success }
        assertIs<DictionaryUiState.Success>(state1)

        viewModel.onQueryChanged("second")

        val state2 = viewModel.uiState.first { it is DictionaryUiState.Success }
        assertIs<DictionaryUiState.Success>(state2)
    }

    @Test
    fun testWordResults() = runTest {
        viewModel.onQueryChanged("test word")

        val state = viewModel.uiState.first { it is DictionaryUiState.Success }
        val successState = state as DictionaryUiState.Success

        assertTrue(successState.wordTranslations.isNotEmpty())
        assertTrue(successState.wordTranslations.all { it.original.isNotEmpty() })
    }

    @Test
    fun testMultiplePronunciationCalls() {
        viewModel.pronounceOriginal("word1")
        viewModel.pronounceOriginal("word2")

        assertEquals(2, fakePronunciation.speakArabicCallCount)
        assertEquals("word2", fakePronunciation.lastSpokeText)
    }
}


