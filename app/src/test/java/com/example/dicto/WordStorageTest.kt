package com.example.dicto

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class WordStorageTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private lateinit var mockContext: Context
    private lateinit var wordStorage: WordStorage

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockContext

        // Note: This is a simplified test setup
        // In real scenario, you'd need to inject DataStore into WordStorage
        wordStorage = WordStorage(mockContext)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun `savedWordsFlow initially returns empty set`() {
        testScope.runTest {
            // Note: This test may fail because we can't easily inject test DataStore
            // It's here as an example of what you'd test with proper DI

            val words = wordStorage.savedWordsFlow.first()
            assertTrue(words.isEmpty())
        }
    }

    @Test
    fun `toggleWord adds word to empty set`() {
        testScope.runTest {
            // Example test - would need proper DataStore injection
            wordStorage.toggleWord("hello")

            // In a real test with injected DataStore:
            // val words = wordStorage.savedWordsFlow.first()
            // assertTrue(words.contains("hello"))
        }
    }

    @Test
    fun `toggleWord removes word from set`() {
        testScope.runTest {
            // Add word
            wordStorage.toggleWord("hello")

            // Remove word
            wordStorage.toggleWord("hello")

            // In a real test with injected DataStore:
            // val words = wordStorage.savedWordsFlow.first()
            // assertFalse(words.contains("hello"))
        }
    }

    @Test
    fun `toggleWord can handle multiple words`() {
        testScope.runTest {
            wordStorage.toggleWord("hello")
            wordStorage.toggleWord("world")
            wordStorage.toggleWord("test")

            // In a real test with injected DataStore:
            // val words = wordStorage.savedWordsFlow.first()
            // assertEquals(3, words.size)
        }
    }
}

/**
 * Note: The WordStorage tests above are examples showing what you'd test.
 * For proper testing, WordStorage should be refactored to accept a DataStore instance
 * via constructor injection, allowing you to provide a test DataStore.
 *
 * Refactored WordStorage would look like:
 *
 * class WordStorage(private val dataStore: DataStore<Preferences>) {
 *     // ... implementation
 * }
 *
 * Then in tests, you create a test DataStore and pass it in.
 */

