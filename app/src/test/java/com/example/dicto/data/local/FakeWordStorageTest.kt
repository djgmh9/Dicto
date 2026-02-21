package com.example.dicto.data.local

import com.example.dicto.fakes.FakeWordStorage
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class FakeWordStorageTest {

    private lateinit var storage: FakeWordStorage

    @Before
    fun setUp() {
        storage = FakeWordStorage()
    }

    @Test
    fun testInitiallyEmpty() {
        val words = storage.getSavedWords()
        assertTrue(words.isEmpty())
    }

    @Test
    fun testSaveWord(): Unit = runBlocking {
        storage.save("hello")

        val words = storage.getSavedWords()
        assertTrue(words.contains("hello"))
        assertEquals(1, words.size)
    }

    @Test
    fun testSaveMultipleWords() = runBlocking {
        storage.save("word1")
        storage.save("word2")
        storage.save("word3")

        val words = storage.getSavedWords()
        assertEquals(3, words.size)
        assertTrue(words.contains("word1"))
        assertTrue(words.contains("word2"))
        assertTrue(words.contains("word3"))
    }

    @Test
    fun testRemoveWord() = runBlocking {
        storage.save("hello")
        storage.remove("hello")

        val words = storage.getSavedWords()
        assertFalse(words.contains("hello"))
        assertTrue(words.isEmpty())
    }

    @Test
    fun testToggleWordAdd() = runBlocking {
        storage.toggleWord("hello")

        val words = storage.getSavedWords()
        assertTrue(words.contains("hello"))
    }

    @Test
    fun testToggleWordRemove() = runBlocking {
        storage.save("hello")
        storage.toggleWord("hello")

        val words = storage.getSavedWords()
        assertFalse(words.contains("hello"))
    }

    @Test
    fun testToggleWordMultipleTimes() = runBlocking {
        storage.toggleWord("word")
        assertTrue(storage.getSavedWords().contains("word"))

        storage.toggleWord("word")
        assertFalse(storage.getSavedWords().contains("word"))

        storage.toggleWord("word")
        assertTrue(storage.getSavedWords().contains("word"))
    }

    @Test
    fun testRemoveNonexistentWord() = runBlocking {
        storage.remove("nonexistent")

        val words = storage.getSavedWords()
        assertTrue(words.isEmpty())
    }

    @Test
    fun testSaveDuplicateWord() = runBlocking {
        storage.save("word")
        storage.save("word")

        val words = storage.getSavedWords()
        assertEquals(1, words.size)
    }

    @Test
    fun testClear() = runBlocking {
        storage.save("word1")
        storage.save("word2")

        storage.clear()

        val words = storage.getSavedWords()
        assertTrue(words.isEmpty())
    }

    @Test
    fun testFlowEmitsUpdates() = runBlocking {
        var collectedSize = 0
        storage.savedWordsFlow.collect { words ->
            collectedSize = words.size
        }

        storage.save("hello")
        assertEquals(1, collectedSize)
    }
}
