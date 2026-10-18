package com.aistra.hail.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.aistra.hail.R
import com.aistra.hail.app.FrozenIndicator
import com.google.android.material.color.MaterialColors
import kotlin.math.min

/** Status decorations are drawn after the bitmap, independently of its grayscale filter. */
class FrozenIconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val density = resources.displayMetrics.density
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bounds = RectF()
    private var frozen = false
    private var indicator = FrozenIndicator.BADGE

    fun setFrozenState(value: Boolean, style: FrozenIndicator) {
        frozen = value
        indicator = style
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!frozen) return
        val blue = ContextCompat.getColor(context, R.color.frozen_indicator_blue)
        val container = ContextCompat.getColor(context, R.color.frozen_indicator_container)
        when (indicator) {
            FrozenIndicator.BADGE -> {
                val radius = 9f * density
                val ring = 1.5f * density
                val cx = min(width - radius - ring, (width - paddingRight).toFloat())
                val cy = min(height - radius - ring, (height - paddingBottom).toFloat())
                fill.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface)
                canvas.drawCircle(cx, cy, radius + ring, fill)
                fill.color = container
                canvas.drawCircle(cx, cy, radius, fill)
                drawFrozenSnowflake(canvas, stroke, cx, cy, radius * .66f, blue)
            }
            FrozenIndicator.OVERLAY -> {
                bounds.set(paddingLeft.toFloat(), paddingTop.toFloat(),
                    (width - paddingRight).toFloat(), (height - paddingBottom).toFloat())
                fill.color = container
                fill.alpha = 190
                canvas.drawRoundRect(bounds, bounds.width() * .25f, bounds.height() * .25f, fill)
                fill.alpha = 255
                drawFrozenSnowflake(canvas, stroke, bounds.centerX(), bounds.centerY(),
                    min(bounds.width(), bounds.height()) * .25f, blue)
            }
            else -> Unit
        }
    }
}

internal fun drawFrozenSnowflake(canvas: Canvas, paint: Paint, x: Float, y: Float, radius: Float, color: Int) {
    paint.color = color
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = radius * .16f
    paint.strokeCap = Paint.Cap.ROUND
    for (arm in 0 until 6) {
        canvas.save()
        canvas.rotate(arm * 60f, x, y)
        canvas.drawLine(x, y, x, y - radius, paint)
        canvas.drawLine(x, y - radius * .58f, x - radius * .3f, y - radius * .85f, paint)
        canvas.drawLine(x, y - radius * .58f, x + radius * .3f, y - radius * .85f, paint)
        canvas.restore()
    }
}
