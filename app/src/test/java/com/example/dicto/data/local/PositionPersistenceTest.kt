package com.example.dicto.data.local

import android.os.Build
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * PositionPersistenceTest
 *
 * Uses Robolectric for tests that need Context.resources (DisplayMetrics).
 *
 * BUG - Position constrained on save:
 *   savePosition() was calling constrainPositionToBounds() before writing to
 *   DataStore, turning a valid drag position x=-376 into x=-75 (minX boundary).
 *   When the service restarted it loaded -75 instead of -376.
 *
 *   Fix: save the EXACT position. WindowManager already keeps the button on
 *   screen; constrainPositionToBounds() is only for bounding/validation logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PositionPersistenceTest {

    private lateinit var preferencesManager: PreferencesManager
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        preferencesManager = mockk(relaxed = true)
        every { preferencesManager.floatingButtonX } returns flowOf(0)
        every { preferencesManager.floatingButtonY } returns flowOf(100)
        coEvery { preferencesManager.setFloatingButtonPosition(any(), any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun persistence() = PositionPersistence(
        context = RuntimeEnvironment.getApplication(),
        preferencesManager = preferencesManager,
        coroutineScope = testScope
    )

    // ─────────────────────────────────────────────────────────────────────────
    // BUG: savePosition must NOT constrain the value before writing
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `savePosition - saves exact x without constraining`() = testScope.runTest {
        persistence().savePosition(-376, 324)
        advanceUntilIdle()
        coVerify { preferencesManager.setFloatingButtonPosition(-376, 324) }
    }

    @Test
    fun `savePosition - does not truncate large negative x to minX boundary`() = testScope.runTest {
        persistence().savePosition(-569, 470)
        advanceUntilIdle()
        // Must NOT save -75 (old minX = -BUTTON_SIZE/2 = -75)
        coVerify(exactly = 0) { preferencesManager.setFloatingButtonPosition(-75, any()) }
        coVerify { preferencesManager.setFloatingButtonPosition(-569, 470) }
    }

    @Test
    fun `savePosition - saves exact y without constraining`() = testScope.runTest {
        persistence().savePosition(100, 1850)
        advanceUntilIdle()
        coVerify { preferencesManager.setFloatingButtonPosition(100, 1850) }
    }

    @Test
    fun `savePosition - saves zero coordinates exactly`() = testScope.runTest {
        persistence().savePosition(0, 0)
        advanceUntilIdle()
        coVerify { preferencesManager.setFloatingButtonPosition(0, 0) }
    }

    @Test
    fun `savePosition - saves positive coordinates exactly`() = testScope.runTest {
        persistence().savePosition(500, 800)
        advanceUntilIdle()
        coVerify { preferencesManager.setFloatingButtonPosition(500, 800) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // savePositionSync
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `savePositionSync - saves exact position without constraining`() = testScope.runTest {
        persistence().savePositionSync(-622, -182)
        coVerify { preferencesManager.setFloatingButtonPosition(-622, -182) }
    }

    @Test
    fun `savePositionSync - does not apply minX constraint`() = testScope.runTest {
        persistence().savePositionSync(-999, 100)
        coVerify { preferencesManager.setFloatingButtonPosition(-999, 100) }
        coVerify(exactly = 0) { preferencesManager.setFloatingButtonPosition(-75, any()) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // constrainPositionToBounds - still works correctly for its own use
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `constrainPositionToBounds - x too far left clamped to minX`() {
        // Screen 1080px wide, BUTTON_SIZE=150 → minX = -75
        val (x, _) = persistence().constrainPositionToBounds(-9999, 100)
        assertEquals(-75, x)
    }

    @Test
    fun `constrainPositionToBounds - x too far right clamped to maxX`() {
        // maxX = screenWidth - 75
        val (x, _) = persistence().constrainPositionToBounds(9999, 100)
        val screenWidth = RuntimeEnvironment.getApplication().resources.displayMetrics.widthPixels
        assertEquals(screenWidth - 75, x)
    }

    @Test
    fun `constrainPositionToBounds - y above top clamped to 0`() {
        val (_, y) = persistence().constrainPositionToBounds(0, -100)
        assertEquals(0, y)
    }

    @Test
    fun `constrainPositionToBounds - y below bottom clamped to maxY`() {
        val screenHeight = RuntimeEnvironment.getApplication().resources.displayMetrics.heightPixels
        val (_, y) = persistence().constrainPositionToBounds(0, 9999)
        assertEquals(screenHeight - 150, y)
    }

    @Test
    fun `constrainPositionToBounds - valid position unchanged`() {
        val (x, y) = persistence().constrainPositionToBounds(200, 400)
        assertEquals(200, x)
        assertEquals(400, y)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // wasPositionConstrained
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `wasPositionConstrained - true when x changed`() {
        assertTrue(persistence().wasPositionConstrained(-500, -75, 100, 100))
    }

    @Test
    fun `wasPositionConstrained - true when y changed`() {
        assertTrue(persistence().wasPositionConstrained(100, 100, -100, 0))
    }

    @Test
    fun `wasPositionConstrained - false when position unchanged`() {
        assertFalse(persistence().wasPositionConstrained(200, 200, 400, 400))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadPosition
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `loadPosition - returns non-null pair when preferences available`() {
        assertNotNull(persistence().loadPosition())
    }
}
