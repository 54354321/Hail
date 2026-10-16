package com.aistra.hail.ui.settings

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/** Reveals the full-width menu vertically, without scaling text or shifting its anchor. */
@Composable
internal fun SettingsOptionsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    width: Dp,
    rowBounds: IntRect?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val visibility = remember { MutableTransitionState(false) }
    visibility.targetState = expanded
    var opensUpwards by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val gap = with(density) { 4.dp.roundToPx() }
    val margin = with(density) { 8.dp.roundToPx() }
    val position = remember(gap, margin, rowBounds) {
        SettingsMenuPositionProvider(gap, margin, rowBounds) { opensUpwards = it }
    }
    if (rowBounds != null && (visibility.currentState || visibility.targetState)) {
        val transition = rememberTransition(visibility, label = "Settings options")
        val reveal by transition.animateFloat(
            transitionSpec = { tween(if (targetState) 200 else 140) },
            label = "Vertical reveal",
        ) { if (it) 1f else 0f }
        val maxHeight = minOf(348.dp, (LocalConfiguration.current.screenHeightDp.dp - 32.dp).coerceAtLeast(48.dp))
        Popup(popupPositionProvider = position, onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true)) {
            Surface(
                modifier = Modifier.width(width).drawWithContent {
                    // Full layout size is retained during both animations, so placement cannot jump.
                    if (reveal >= 1f) drawContent() else {
                        val visibleHeight = size.height * reveal
                        clipRect(
                            top = if (opensUpwards) size.height - visibleHeight else 0f,
                            bottom = if (opensUpwards) size.height else visibleHeight,
                        ) { this@drawWithContent.drawContent() }
                    }
                },
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
            ) {
                Column(Modifier.heightIn(max = maxHeight).verticalScroll(rememberScrollState()).padding(vertical = 8.dp),
                    content = content)
            }
        }
    }
}

internal class SettingsMenuPositionProvider(
    private val gap: Int,
    private val verticalMargin: Int,
    private val measuredRowBounds: IntRect? = null,
    private val onDirection: (Boolean) -> Unit = {},
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val row = measuredRowBounds ?: anchorBounds
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = (if (layoutDirection == LayoutDirection.Ltr) row.left
            else row.right - popupContentSize.width).coerceIn(0, maxX)
        val below = row.bottom + gap
        val above = row.top - gap - popupContentSize.height
        val upwards = when {
            below + popupContentSize.height <= windowSize.height - verticalMargin -> false
            above >= verticalMargin -> true
            else -> row.top > windowSize.height - row.bottom
        }
        val minY = minOf(verticalMargin, (windowSize.height - popupContentSize.height).coerceAtLeast(0))
        val maxY = (windowSize.height - popupContentSize.height - verticalMargin).coerceAtLeast(minY)
        val y = (if (upwards) above else below).coerceIn(minY, maxY)
        onDirection(upwards)
        return IntOffset(x, y)
    }
}
