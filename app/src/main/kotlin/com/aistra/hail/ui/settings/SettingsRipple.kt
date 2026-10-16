package com.aistra.hail.ui.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

/** A bounded, solid circular ripple; does not use the platform's patterned ripple renderer. */
internal data class SettingsRipple(val color: Color) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        RippleNode(interactionSource, color)
}

private class RippleNode(
    private val interactions: InteractionSource,
    private val color: Color,
) : Modifier.Node(), DrawModifierNode {
    private class Wave(val press: PressInteraction.Press) {
        val radius = Animatable(0f)
        val opacity = Animatable(0f)
        var expansion: Job? = null
    }

    private val waves = mutableStateListOf<Wave>()
    private val focused = mutableStateOf(false)
    private val hovered = mutableStateOf(false)

    override fun onAttach() {
        coroutineScope.launch {
            interactions.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        val wave = Wave(interaction)
                        waves.add(wave)
                        coroutineScope.launch { wave.opacity.animateTo(1f, tween(60)) }
                        wave.expansion = coroutineScope.launch {
                            wave.radius.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
                        }
                    }
                    is PressInteraction.Release -> finish(interaction.press, cancelled = false)
                    is PressInteraction.Cancel -> finish(interaction.press, cancelled = true)
                    is FocusInteraction.Focus -> focused.value = true
                    is FocusInteraction.Unfocus -> focused.value = false
                    is HoverInteraction.Enter -> hovered.value = true
                    is HoverInteraction.Exit -> hovered.value = false
                }
            }
        }
    }

    private fun finish(press: PressInteraction.Press, cancelled: Boolean) {
        val wave = waves.firstOrNull { it.press === press } ?: return
        coroutineScope.launch {
            if (cancelled) wave.expansion?.cancel() else wave.expansion?.join()
            wave.opacity.animateTo(0f, tween(if (cancelled) 90 else 180))
            waves.remove(wave)
        }
    }

    override fun onDetach() {
        waves.clear()
        focused.value = false
        hovered.value = false
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        clipRect {
            if (focused.value || hovered.value) {
                drawRect(color.copy(alpha = color.alpha * if (focused.value) 0.08f else 0.04f))
            }
            waves.forEach { wave ->
                val pressed = wave.press.pressPosition
                val origin = if (pressed.x.isFinite() && pressed.y.isFinite()) {
                    Offset(pressed.x.coerceIn(0f, size.width), pressed.y.coerceIn(0f, size.height))
                } else center
                val radius = hypot(max(origin.x, size.width - origin.x), max(origin.y, size.height - origin.y))
                drawCircle(
                    color = color.copy(alpha = color.alpha * 0.14f * wave.opacity.value),
                    radius = radius * wave.radius.value,
                    center = origin,
                )
            }
        }
    }
}
