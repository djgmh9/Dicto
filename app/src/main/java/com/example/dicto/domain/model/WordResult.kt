package com.example.dicto.domain.model

/**
 * WordResult - Represents a single word translation result
 *
 * Domain model for word translations with save state
 *
 * @property original The original word (in source language)
 * @property translation The translated word (in target language)
 * @property isSaved Whether this word is saved to the vocabulary list
 */
data class WordResult(
    val original: String,
    val translation: String,
    val isSaved: Boolean = false
)

