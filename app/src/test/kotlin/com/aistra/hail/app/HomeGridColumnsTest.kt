package com.aistra.hail.app

import org.junit.Assert.*
import org.junit.Test

class HomeGridColumnsTest {
    @Test fun defaultPhoneLayoutKeepsFourColumns() {
        assertEquals(4, homeGridColumns(360, 1f, 64, false))
        assertEquals(5, homeGridColumns(360, 1f, 64, true))
    }

    @Test fun enlargingIconsMovesItemsOntoTheNextRow() {
        assertEquals(4, homeGridColumns(360, 1f, 64, false))
        assertEquals(3, homeGridColumns(360, 1f, 96, false))
    }

    @Test fun rotationAndSplitScreenUseTheActualAvailableWidth() {
        assertEquals(8, homeGridColumns(640, 1f, 64, false))
        assertEquals(4, homeGridColumns(320, 1f, 64, false))
    }

    @Test fun densityDoesNotChangeTheNumberOfColumns() {
        assertEquals(homeGridColumns(360, 1f, 80, false), homeGridColumns(1080, 3f, 80, false))
    }

    @Test fun noMeasuredWidthStillProducesAValidLayout() {
        assertEquals(1, homeGridColumns(0, 3f, 64, false))
    }

    @Test fun supportedSizesFitAndNeverGainColumnsWhenEnlarged() {
        for (width in listOf(280, 320, 360, 400, 640, 960)) {
            for (compact in listOf(false, true)) {
                var previous = Int.MAX_VALUE
                for (size in 48..96 step 4) {
                    val columns = homeGridColumns(width, 1f, size, compact)
                    assertTrue(columns in 1..previous)
                    assertTrue(columns * (size + if (compact) 8 else 16) <= width)
                    previous = columns
                }
            }
        }
    }
}
