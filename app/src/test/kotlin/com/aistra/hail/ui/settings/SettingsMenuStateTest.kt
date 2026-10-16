package com.aistra.hail.ui.settings

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.*
import org.junit.Test

class SettingsMenuStateTest {
    private val themeRow = Rect(0f, 300f, 400f, 380f)
    private val expandedTheme = Rect(0f, 300f, 400f, 550f)

    @Test fun tappingTheSameRowCancelsInsteadOfReopening() {
        val state = SettingsMenuState()
        state.toggle("theme", themeRow)
        state.updateBounds("theme", expandedTheme)
        state.dismissOutside(Offset(120f, 340f))
        state.toggle("theme", expandedTheme)
        assertNull(state.activeKey)
    }

    @Test fun optionsBelowTheRowRemainClickable() {
        val state = SettingsMenuState()
        state.toggle("theme", themeRow)
        state.updateBounds("theme", expandedTheme)
        state.dismissOutside(Offset(120f, 500f))
        assertEquals("theme", state.activeKey)
    }

    @Test fun tappingAnotherControlDismissesBeforeItsOwnClick() {
        val state = SettingsMenuState()
        state.toggle("theme", themeRow)
        state.updateBounds("theme", expandedTheme)
        state.dismissOutside(Offset(350f, 620f))
        assertNull(state.activeKey)
    }

    @Test fun openingAnotherMenuReplacesTheFirst() {
        val state = SettingsMenuState()
        state.toggle("theme", themeRow)
        state.toggle("icons", Rect(0f, 550f, 400f, 630f))
        assertEquals("icons", state.activeKey)
        // A closing row must not overwrite the bounds belonging to the new menu.
        state.updateBounds("theme", expandedTheme)
        state.dismissOutside(Offset(200f, 600f))
        assertEquals("icons", state.activeKey)
    }

    @Test fun scrollingUpdatesTheInsideHitArea() {
        val state = SettingsMenuState()
        state.toggle("theme", themeRow)
        state.updateBounds("theme", Rect(0f, 100f, 400f, 350f))
        state.dismissOutside(Offset(120f, 150f))
        assertEquals("theme", state.activeKey)
        state.dismissOutside(Offset(120f, 500f))
        assertNull(state.activeKey)
    }

    @Test fun dismissedMenuCannotLeaveStaleBounds() {
        val state = SettingsMenuState()
        state.toggle("theme", themeRow)
        state.dismiss()
        state.updateBounds("theme", expandedTheme)
        state.toggle("icons", Rect(0f, 650f, 400f, 730f))
        state.dismissOutside(Offset(120f, 500f))
        assertNull(state.activeKey)
    }
}
