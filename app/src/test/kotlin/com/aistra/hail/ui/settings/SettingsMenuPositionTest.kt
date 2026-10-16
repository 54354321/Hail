package com.aistra.hail.ui.settings

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.*
import org.junit.Test

class SettingsMenuPositionTest {
    @Test fun measuredRowOverridesAnIncorrectPopupParent() {
        val measuredRow = IntRect(20, 450, 340, 540)
        val position = SettingsMenuPositionProvider(4, 8, measuredRow)
        val wrongParents = listOf(IntRect(0, 0, 360, 0), IntRect(0, 250, 360, 250), IntRect(0, 0, 360, 700))
        for (parent in wrongParents) {
            assertEquals(IntOffset(20, 544), position.calculatePosition(
                parent, IntSize(360, 900), LayoutDirection.Ltr, IntSize(320, 160)))
        }
    }

    @Test fun measuredBottomRowDeterminesTheUpwardPlacement() {
        val position = SettingsMenuPositionProvider(4, 8, IntRect(20, 600, 340, 670))
        assertEquals(IntOffset(20, 436), position.calculatePosition(
            IntRect(0, 0, 0, 0), IntSize(360, 700), LayoutDirection.Ltr, IntSize(320, 160)))
    }

    @Test fun menuStartsBelowTheWholeRow() {
        var upward = true
        val position = SettingsMenuPositionProvider(4, 8) { upward = it }
        assertEquals(IntOffset(20, 274), position.calculatePosition(
            IntRect(20, 200, 340, 270), IntSize(360, 700), LayoutDirection.Ltr, IntSize(320, 160)))
        assertFalse(upward)
    }

    @Test fun bottomRowOpensUpwards() {
        var upward = false
        val position = SettingsMenuPositionProvider(4, 8) { upward = it }
        assertEquals(IntOffset(20, 436), position.calculatePosition(
            IntRect(20, 600, 340, 670), IntSize(360, 700), LayoutDirection.Ltr, IntSize(320, 160)))
        assertTrue(upward)
    }

    @Test fun rtlAlignsToTheRowsRightEdge() {
        val position = SettingsMenuPositionProvider(4, 8)
        assertEquals(IntOffset(130, 274), position.calculatePosition(
            IntRect(30, 200, 330, 270), IntSize(360, 700), LayoutDirection.Rtl, IntSize(200, 160)))
    }

    @Test fun fullWidthAndLongMenusStayInsideTheWindow() {
        val position = SettingsMenuPositionProvider(4, 8)
        for (rowTop in listOf(0, 40, 200, 540, 620)) {
            for (height in listOf(50, 160, 348, 668)) {
                val anchor = IntRect(0, rowTop, 360, rowTop + 72)
                val size = IntSize(360, height)
                val offset = position.calculatePosition(anchor, IntSize(360, 700), LayoutDirection.Ltr, size)
                assertEquals(0, offset.x)
                assertTrue("Menu must be below the top edge", offset.y >= 0)
                assertTrue("Menu must be above the bottom edge", offset.y + height <= 700)
                // Closing uses the same complete measured size, not the shrinking visible portion.
                assertEquals(offset, position.calculatePosition(anchor, IntSize(360, 700), LayoutDirection.Ltr, size))
            }
        }
    }
}
