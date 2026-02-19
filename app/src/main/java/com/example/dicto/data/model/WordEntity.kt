package com.example.dicto.data.model

/**
 * WordEntity - Data model for stored words
 * Represents how words are stored in local database/storage
 */
data class WordEntity(
    val original: String,
    val translation: String,
    val timestamp: Long = System.currentTimeMillis()
)

