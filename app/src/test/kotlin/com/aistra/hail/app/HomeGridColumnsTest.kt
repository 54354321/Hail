package com.aistra.hail.app

import org.junit.Assert.*
import org.junit.Test

class HomeGridColumnsTest {
    @Test fun defaultPhoneLayoutKeepsFourColumns() {
        assertEquals(4, homeGridColumns(360, 1f, 64))
        assertEquals(5, homeGridColumns(360, 1f, 56))
    }

    @Test fun enlargingIconsMovesItemsOntoTheNextRow() {
        assertEquals(4, homeGridColumns(360, 1f, 64))
        assertEquals(3, homeGridColumns(360, 1f, 96))
    }

    @Test fun rotationAndSplitScreenUseTheActualAvailableWidth() {
        assertEquals(8, homeGridColumns(640, 1f, 64))
        assertEquals(4, homeGridColumns(320, 1f, 64))
    }

    @Test fun densityDoesNotChangeTheNumberOfColumns() {
        assertEquals(homeGridColumns(360, 1f, 80), homeGridColumns(1080, 3f, 80))
    }

    @Test fun noMeasuredWidthStillProducesAValidLayout() {
        assertEquals(1, homeGridColumns(0, 3f, 64))
    }

    @Test fun supportedSizesFitAndNeverGainColumnsWhenEnlarged() {
        for (width in listOf(280, 320, 360, 400, 640, 960)) {
            run {
                var previous = Int.MAX_VALUE
                for (size in 48..96 step 8) {
                    val columns = homeGridColumns(width, 1f, size)
                    assertTrue(columns in 1..previous)
                    assertTrue(columns * (size + 16) <= width)
                    previous = columns
                }
            }
        }
    }
    @Test fun legacyCompactPreferenceBecomesASmallerSize() {
        assertEquals(56, resolvedIconSize(null, true))
        assertEquals(64, resolvedIconSize(null, false))
        assertEquals(80, resolvedIconSize(80f, true))
    }
    @Test fun existingSizesSnapToASupportedLevelAndStayInRange() {
        for (value in -20..140) {
            val size = resolvedIconSize(value.toFloat(), false)
            assertTrue(size in 48..96 && (size - 48) % 8 == 0)
            assertTrue(kotlin.math.abs(size - value.coerceIn(48, 96)) <= 4)
        }
        assertEquals(64, resolvedIconSize(Float.NaN, false))
    }
}
