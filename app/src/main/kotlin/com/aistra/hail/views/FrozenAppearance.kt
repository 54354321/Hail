package com.aistra.hail.views

import android.graphics.Canvas
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ReplacementSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aistra.hail.R
import com.aistra.hail.app.FrozenIndicator
import com.aistra.hail.app.HailData
import kotlin.math.roundToInt

/** A drawn span avoids colored emoji fonts overriding the requested blue. */
private class NameSnowflakeSpan(private val color: Int) : ReplacementSpan() {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int,
        fm: Paint.FontMetricsInt?): Int = (paint.textSize * .9f).roundToInt()

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float,
        top: Int, y: Int, bottom: Int, paint: Paint) {
        val centerY = y + (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f
        drawFrozenSnowflake(canvas, stroke, x + paint.textSize * .4f, centerY,
            paint.textSize * .36f, color)
    }
}

fun TextView.frozenAppName(name: CharSequence, frozen: Boolean, whitelisted: Boolean = false) {
    text = SpannableStringBuilder().apply {
        if (frozen && HailData.frozenNameSnowflake) {
            append("❄ ")
            setSpan(NameSnowflakeSpan(ContextCompat.getColor(context, R.color.frozen_indicator_blue)),
                0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (whitelisted) append("🔒")
        append(name)
    }
}

fun TextView.bindFrozenCaption(frozen: Boolean) {
    text = context.getString(R.string.frozen_status)
    visibility = if (HailData.frozenIndicator == FrozenIndicator.TEXT) {
        if (frozen) View.VISIBLE else View.INVISIBLE
    } else View.GONE
}

fun FrozenIconView.bindFrozenAppearance(frozen: Boolean) {
    val size = (HailData.appIconSize * resources.displayMetrics.density).roundToInt()
    if (layoutParams.width != size || layoutParams.height != size) {
        layoutParams = layoutParams.apply { width = size; height = size }
    }
    setFrozenState(frozen, HailData.frozenIndicator)
}
