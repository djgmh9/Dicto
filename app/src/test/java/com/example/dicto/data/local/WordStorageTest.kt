package com.example.dicto.data.local

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * WordStorageTest - Unit tests for vocabulary persistence
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WordStorageTest {

    private lateinit var context: Context
    private lateinit var wordStorage: WordStorage

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        wordStorage = DefaultWordStorage(context)
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
