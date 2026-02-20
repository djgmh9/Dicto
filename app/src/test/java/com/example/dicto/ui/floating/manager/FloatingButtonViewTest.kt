package com.example.dicto.ui.floating.manager

import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.WindowManager
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * FloatingButtonViewTest
 *
 * Uses Robolectric so Android classes (WindowManager.LayoutParams, ImageView)
 * are available in JVM unit tests without a device.
 *
 * BUG 1 - Ghost button:
 *   Old show() re-created floatingView even when one already existed while
 *   hidden, abandoning the old view in memory as a ghost.
 *
 * BUG 2 - Wrong position after re-add:
 *   Old show() recreated layoutParams with initialX/initialY on every call,
 *   resetting the button to its spawn position instead of the dragged position.
 *
 * BUG 3 - Touch handler lost after re-add:
 *   Old show() created a brand-new ImageView on every call, losing the
 *   setOnTouchListener wired on first creation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class FloatingButtonViewTest {

    private lateinit var context: Context
    private lateinit var windowManager: WindowManager
    private lateinit var view: FloatingButtonView

    private var touchCallCount = 0
    private val touchListener: (MotionEvent) -> Boolean = { _ ->
        touchCallCount++
        true
    }

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        windowManager = mockk(relaxed = true)
        touchCallCount = 0
        view = FloatingButtonView(
            context = context,
            windowManager = windowManager,
            initialX = 0,
            initialY = 100
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIRST-TIME CREATION
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `show - first call adds view to WindowManager exactly once`() {
        view.show(touchListener)
        verify(exactly = 1) { windowManager.addView(any(), any()) }
    }

    @Test
    fun `show - first call stores layoutParams`() {
        assertNull(view.getLayoutParams())
        view.show(touchListener)
        assertNotNull(view.getLayoutParams())
    }

    @Test
    fun `show - first call uses initialX and initialY`() {
        val v = FloatingButtonView(context, windowManager, initialX = 42, initialY = 77)
        v.show(touchListener)
        val params = v.getLayoutParams()!!
        assertEquals(42, params.x)
        assertEquals(77, params.y)
    }

    @Test
    fun `isVisible returns false before first show`() {
        assertFalse(view.isVisible())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUG 1 – Ghost button: show() must NOT create a second view when hidden
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `show after hide - reuses the same ImageView instance (no ghost)`() {
        view.show(touchListener)
        val originalView = view.getFloatingView()

        view.hide()
        view.show(touchListener)

        assertSame(
            "Re-show must reuse the same ImageView - a new instance leaves old one as ghost",
            originalView,
            view.getFloatingView()
        )
    }

    @Test
    fun `show called twice - adds view only once`() {
        view.show(touchListener)
        view.show(touchListener)
        verify(exactly = 1) { windowManager.addView(any(), any()) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUG 2 – Wrong position: must preserve layoutParams after hide/show
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `show after hide - preserves dragged position`() {
        view.show(touchListener)

        // Simulate user dragging the button to a new position
        val params = view.getLayoutParams()!!
        params.x = -376
        params.y = 324

        view.hide()
        view.show(touchListener)

        val reAddedParams = view.getLayoutParams()!!
        assertEquals(
            "x must be -376 after hide/show - old bug would reset to initialX",
            -376,
            reAddedParams.x
        )
        assertEquals(324, reAddedParams.y)
    }

    @Test
    fun `show after hide - reuses the same LayoutParams object`() {
        view.show(touchListener)
        val originalParams = view.getLayoutParams()

        view.hide()
        view.show(touchListener)

        assertSame(
            "Re-show must reuse same LayoutParams object - new one would reset position",
            originalParams,
            view.getLayoutParams()
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUG 3 – Touch handler survives hide/show
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `show after hide - addView called twice total (create then re-add)`() {
        view.show(touchListener)  // first: create
        view.hide()
        view.show(touchListener)  // second: re-add existing view
        verify(exactly = 2) { windowManager.addView(any(), any()) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HIDE
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `hide - does NOT null floatingView reference`() {
        view.show(touchListener)
        view.hide()
        assertNotNull(
            "floatingView must be kept after hide() so re-add works without recreation",
            view.getFloatingView()
        )
    }

    @Test
    fun `hide - does NOT null layoutParams`() {
        view.show(touchListener)
        view.hide()
        assertNotNull(
            "layoutParams must be kept after hide() to preserve position",
            view.getLayoutParams()
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DESTROY
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `destroy - nulls out floatingView`() {
        view.show(touchListener)
        view.destroy()
        assertNull(view.getFloatingView())
    }

    @Test
    fun `destroy - nulls out layoutParams`() {
        view.show(touchListener)
        view.destroy()
        assertNull(view.getLayoutParams())
    }

    @Test
    fun `destroy - getCurrentPosition returns null`() {
        view.show(touchListener)
        view.destroy()
        assertNull(view.getCurrentPosition())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCurrentPosition
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `getCurrentPosition - null before show`() {
        assertNull(view.getCurrentPosition())
    }

    @Test
    fun `getCurrentPosition - returns initial position after show`() {
        val v = FloatingButtonView(context, windowManager, initialX = 10, initialY = 20)
        v.show(touchListener)
        val pos = v.getCurrentPosition()!!
        assertEquals(10, pos.first)
        assertEquals(20, pos.second)
    }

    @Test
    fun `getCurrentPosition - reflects updated x and y in layoutParams`() {
        view.show(touchListener)
        val params = view.getLayoutParams()!!
        params.x = -500
        params.y = 300
        val pos = view.getCurrentPosition()!!
        assertEquals(-500, pos.first)
        assertEquals(300, pos.second)
    }

    @Test
    fun `getCurrentPosition - null after destroy`() {
        view.show(touchListener)
        view.destroy()
        assertNull(view.getCurrentPosition())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // isVisible
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `isVisible - false when floatingView is null`() {
        assertFalse(view.isVisible())
    }
}
