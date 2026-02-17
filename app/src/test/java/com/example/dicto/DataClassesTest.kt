package com.example.dicto

import org.junit.Test
import org.junit.Assert.*

class WordResultTest {

    @Test
    fun `WordResult creation with default isSaved`() {
        val word = WordResult("hello", "مرحبا")

        assertEquals("hello", word.original)
        assertEquals("مرحبا", word.translation)
        assertFalse(word.isSaved)
    }

    @Test
    fun `WordResult creation with isSaved true`() {
        val word = WordResult("hello", "مرحبا", isSaved = true)

        assertTrue(word.isSaved)
    }

    @Test
    fun `WordResult copy changes isSaved`() {
        val word = WordResult("hello", "مرحبا", isSaved = false)
        val savedWord = word.copy(isSaved = true)

        assertFalse(word.isSaved)
        assertTrue(savedWord.isSaved)
        assertEquals(word.original, savedWord.original)
        assertEquals(word.translation, savedWord.translation)
    }

    @Test
    fun `WordResult equality based on all properties`() {
        val word1 = WordResult("hello", "مرحبا", isSaved = false)
        val word2 = WordResult("hello", "مرحبا", isSaved = false)
        val word3 = WordResult("hello", "مرحبا", isSaved = true)

        assertEquals(word1, word2)
        assertNotEquals(word1, word3) // Different isSaved
    }
}

class DictionaryUiStateTest {

    @Test
    fun `Idle state is data object`() {
        val idle1 = DictionaryUiState.Idle
        val idle2 = DictionaryUiState.Idle

        assertEquals(idle1, idle2)
        assertSame(idle1, idle2) // Same instance
    }

    @Test
    fun `Loading state is data object`() {
        val loading1 = DictionaryUiState.Loading
        val loading2 = DictionaryUiState.Loading

        assertEquals(loading1, loading2)
        assertSame(loading1, loading2)
    }

    @Test
    fun `Error state contains message`() {
        val error = DictionaryUiState.Error("Network error")

        assertEquals("Network error", error.message)
    }

    @Test
    fun `Success state contains translation and words`() {
        val words = listOf(
            WordResult("hello", "مرحبا"),
            WordResult("world", "عالم")
        )
        val success = DictionaryUiState.Success(
            fullTranslation = "مرحبا بالعالم",
            wordTranslations = words
        )

        assertEquals("مرحبا بالعالم", success.fullTranslation)
        assertEquals(2, success.wordTranslations.size)
        assertEquals("hello", success.wordTranslations[0].original)
    }

    @Test
    fun `Success state can have empty word list`() {
        val success = DictionaryUiState.Success(
            fullTranslation = "test",
            wordTranslations = emptyList()
        )

        assertEquals("test", success.fullTranslation)
        assertTrue(success.wordTranslations.isEmpty())
    }

    @Test
    fun `Success state copy updates wordTranslations`() {
        val words = listOf(WordResult("hello", "مرحبا", isSaved = false))
        val success = DictionaryUiState.Success("مرحبا", words)

        val updatedWords = words.map { it.copy(isSaved = true) }
        val updatedSuccess = success.copy(wordTranslations = updatedWords)

        assertFalse(success.wordTranslations[0].isSaved)
        assertTrue(updatedSuccess.wordTranslations[0].isSaved)
    }

    @Test
    fun `different UI states are not equal`() {
        val idle: DictionaryUiState = DictionaryUiState.Idle
        val loading: DictionaryUiState = DictionaryUiState.Loading
        val error: DictionaryUiState = DictionaryUiState.Error("Error")
        val success: DictionaryUiState = DictionaryUiState.Success("", emptyList())

        assertNotEquals(idle, loading)
        assertNotEquals(loading, error)
        assertNotEquals(error, success)
        assertNotEquals(success, idle)
    }
}

