package com.aistra.hail.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp

internal class SettingsMenuState {
    var activeKey by mutableStateOf<String?>(null)
        private set
    private var bounds: Rect? = null

    fun toggle(key: String, rowBounds: Rect) {
        if (activeKey == key) dismiss() else {
            bounds = rowBounds
            activeKey = key
        }
    }

    fun updateBounds(key: String, value: Rect) {
        if (activeKey == key) bounds = value
    }

    fun dismissOutside(point: Offset) {
        if (bounds?.contains(point) == false) dismiss()
    }

    fun dismiss() {
        activeKey = null
        bounds = null
    }
}

internal val LocalSettingsMenuState = staticCompositionLocalOf<SettingsMenuState> {
    error("Settings menus require ProvideSettingsMenus")
}

@Composable
internal fun ProvideSettingsMenus(content: @Composable (Modifier) -> Unit) {
    val state = remember { SettingsMenuState() }
    var origin by remember { mutableStateOf(Offset.Zero) }
    BackHandler(enabled = state.activeKey != null) { state.dismiss() }
    CompositionLocalProvider(LocalSettingsMenuState provides state) {
        content(Modifier.fillMaxSize()
            .onGloballyPositioned { origin = it.positionInWindow() }
            .pointerInput(state) {
                awaitEachGesture {
                    // Observe without consuming: outside controls still receive their own tap.
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    state.dismissOutside(down.position + origin)
                }
            })
    }
}

/** Shares the row's normal layout, so neither window insets nor popup gravity can move it. */
@Composable
internal fun SettingsOptionsMenu(
    expanded: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(animationSpec = tween(200), expandFrom = Alignment.Top),
        exit = shrinkVertically(animationSpec = tween(140), shrinkTowards = Alignment.Top),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp,
        ) {
            Column(Modifier.heightIn(max = 348.dp).verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp), content = content)
        }
    }
}
