package com.example.dicto.data.repository

import com.example.dicto.fakes.FakeTranslationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeTranslationRepositoryTest {

    private lateinit var fakeRepository: FakeTranslationRepository

    @Before
    fun setUp() {
        fakeRepository = FakeTranslationRepository()
    }

    @Test
    fun testSuccessfulTranslateText() = runTest {
        val result = fakeRepository.translateText("hello")

        assertTrue(result.isSuccess)
        assertEquals("olleh", result.getOrNull())
    }

    @Test
    fun testEmptyStringTranslation() = runTest {
        val result = fakeRepository.translateText("")

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull())
    }

    @Test
    fun testFailedTranslation() = runTest {
        fakeRepository.setShouldFail(true, "Service error")

        val result = fakeRepository.translateText("test")

        assertTrue(result.isFailure)
        assertEquals("Service error", result.exceptionOrNull()?.message)
    }

    @Test
    fun testMultipleTranslations() = runTest {
        val result1 = fakeRepository.translateText("first")
        val result2 = fakeRepository.translateText("second")

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)

        assertEquals("tsrif", result1.getOrNull())
        assertEquals("dnoces", result2.getOrNull())
    }

    @Test
    fun testFailureMessageCustomization() = runTest {
        val customMessage = "Custom error message"
        fakeRepository.setShouldFail(true, customMessage)

        val result = fakeRepository.translateText("test")

        assertTrue(result.isFailure)
        assertEquals(customMessage, result.exceptionOrNull()?.message)
    }

    @Test
    fun testRecoveryFromFailure() = runTest {
        fakeRepository.setShouldFail(true)

        var result = fakeRepository.translateText("test")
        assertTrue(result.isFailure)

        fakeRepository.setShouldFail(false)

        result = fakeRepository.translateText("test")
        assertTrue(result.isSuccess)
    }

    @Test
    fun testModelDownloadedByDefault() = runTest {
        val isDownloaded = fakeRepository.isModelDownloaded()
        assertTrue(isDownloaded)
    }

    @Test
    fun testModelDownload() = runTest {
        fakeRepository.setModelDownloaded(false)
        assertFalse(fakeRepository.isModelDownloaded())

        val result = fakeRepository.downloadModel()
        assertTrue(result.isSuccess)
        assertTrue(fakeRepository.isModelDownloaded())
    }

    @Test
    fun testCloseDoesNotThrow() {
        // Should not throw any exception
        fakeRepository.close()
    }
}
