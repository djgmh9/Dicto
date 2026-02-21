package com.example.dicto.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * WordStorageTest - Unit tests for vocabulary persistence
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WordStorageTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var context: Context
    private lateinit var wordStorage: WordStorage
    private lateinit var testDataStore: DataStore<Preferences>
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()

        // Create a unique DataStore for each test to avoid data pollution
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                tmpFolder.newFile("test_word_storage_${System.currentTimeMillis()}.preferences_pb")
            }
        )

        wordStorage = DefaultWordStorage(context, testDataStore)
    }

    @Test
    fun `savedWordsFlow - starts empty`() = runTest {
        val words = wordStorage.savedWordsFlow.first()
        assertTrue("Saved words should be empty initially", words.isEmpty())
    }

    @Test
    fun `toggleWord - adds word when not present`() = runTest {
        val word = "Hello"
        wordStorage.toggleWord(word)
        
        val words = wordStorage.savedWordsFlow.first()
        assertTrue("Word should be added to storage", words.contains(word))
        assertEquals(1, words.size)
    }

    @Test
    fun `toggleWord - removes word when already present`() = runTest {
        val word = "Hello"
        wordStorage.toggleWord(word) // Add
        wordStorage.toggleWord(word) // Remove
        
        val words = wordStorage.savedWordsFlow.first()
        assertFalse("Word should be removed from storage", words.contains(word))
        assertTrue(words.isEmpty())
    }

    @Test
    fun `toggleWord - handles multiple words`() = runTest {
        wordStorage.toggleWord("Apple")
        wordStorage.toggleWord("Banana")
        wordStorage.toggleWord("Cherry")
        
        val words = wordStorage.savedWordsFlow.first()
        assertEquals(3, words.size)
        assertTrue(words.contains("Apple"))
        assertTrue(words.contains("Banana"))
        assertTrue(words.contains("Cherry"))
    }
}
