package com.aistra.hail.ui.home

import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.aistra.hail.R
import com.aistra.hail.app.HailData
import com.google.android.material.color.MaterialColors
import kotlin.math.roundToInt

/** One anchored window; groups reveal their children directly below the parent row. */
internal fun showHomeOverflowMenu(
    anchor: View,
    menu: Menu,
    onAction: (MenuItem) -> Unit,
    onSort: (String) -> Unit,
): PopupWindow {
    val context = anchor.context
    fun dp(value: Int) = (value * context.resources.displayMetrics.density).roundToInt()
    val surface = MaterialColors.getColor(anchor, com.google.android.material.R.attr.colorSurface)
    val foreground = MaterialColors.getColor(anchor, com.google.android.material.R.attr.colorOnSurface)
    val primary = MaterialColors.getColor(anchor, androidx.appcompat.R.attr.colorPrimary)
    val frame = Rect().also(anchor::getWindowVisibleDisplayFrame)
    val location = IntArray(2).also(anchor::getLocationOnScreen)
    val width = minOf(dp(280), frame.width() - dp(24)).coerceAtLeast(1)
    val top = (location[1] + anchor.height + dp(4)).coerceAtMost(frame.bottom - dp(48))
    val left = if (ViewCompat.getLayoutDirection(anchor) == ViewCompat.LAYOUT_DIRECTION_RTL)
        location[0].coerceIn(frame.left, (frame.right - width).coerceAtLeast(frame.left))
    else (location[0] + anchor.width - width).coerceIn(frame.left, (frame.right - width).coerceAtLeast(frame.left))
    val maxHeight = (frame.bottom - top - dp(12)).coerceAtLeast(1)
    val rows = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, dp(8), 0, dp(8))
    }
    val scroll = ScrollView(context).apply {
        isFillViewport = false
        addView(rows, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        background = GradientDrawable().apply { setColor(surface); cornerRadius = dp(12).toFloat() }
        clipToOutline = true
    }
    val popup = PopupWindow(scroll, width, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
        setBackgroundDrawable(GradientDrawable().apply { setColor(surface); cornerRadius = dp(12).toFloat() })
        elevation = dp(6).toFloat()
        isOutsideTouchable = true
        inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        animationStyle = 0
    }
    fun resize() {
        scroll.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST))
        if (popup.isShowing) popup.update(width, scroll.measuredHeight)
        else popup.height = scroll.measuredHeight
    }
    fun row(label: CharSequence, child: Boolean = false, selected: Boolean = false) = TextView(context).apply {
        text = label
        textSize = if (child) 14f else 15f
        setTextColor(if (selected) primary else foreground)
        gravity = Gravity.CENTER_VERTICAL
        minHeight = dp(48)
        setPaddingRelative(dp(if (child) 32 else 20), dp(10), dp(20), dp(10))
        val ripple = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, ripple, true)
        setBackgroundResource(ripple.resourceId)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    val groups = mutableListOf<Pair<TextView, LinearLayout>>()
    fun group(title: CharSequence, choices: List<Pair<CharSequence, () -> Unit>>) {
        val heading = row("$title  ▾")
        val children = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE }
        choices.forEach { (label, action) -> children.addView(row(label, child = true).apply {
            setOnClickListener { popup.dismiss(); action() }
        }) }
        rows.addView(heading)
        rows.addView(children)
        groups += heading to children
        heading.setOnClickListener {
            val opening = children.visibility != View.VISIBLE
            groups.forEach { (parent, items) ->
                items.visibility = if (items === children && opening) View.VISIBLE else View.GONE
                parent.text = parent.text.toString().dropLast(1) + if (items.visibility == View.VISIBLE) "▴" else "▾"
                ViewCompat.setStateDescription(parent, context.getString(if (items.visibility == View.VISIBLE)
                    R.string.menu_expanded else R.string.menu_collapsed))
            }
            resize()
        }
        ViewCompat.setStateDescription(heading, context.getString(R.string.menu_collapsed))
    }
    group(context.getString(R.string.sort_by), listOf(
        HailData.SORT_NAME to R.string.home_sort_name,
        HailData.SORT_INSTALL to R.string.home_sort_install,
    ).map { (key, label) ->
        (if (HailData.homeSortBy == key) "✓  " else "     ") + context.getString(label) to { onSort(key) }
    })
    listOf(R.id.action_freeze_current, R.id.action_unfreeze_current, R.id.action_freeze_all,
        R.id.action_unfreeze_all, R.id.action_freeze_non_whitelisted).forEach { id ->
        val item = menu.findItem(id)
        rows.addView(row(item.title ?: "").apply { setOnClickListener { popup.dismiss(); onAction(item) } })
    }
    fun action(id: Int): Pair<CharSequence, () -> Unit> {
        val item = menu.findItem(id)
        return (item.title ?: "") to { onAction(item) }
    }
    group(context.getString(R.string.action_import_app), listOf(action(R.id.action_import_clipboard), action(R.id.action_import_frozen)))
    group(context.getString(R.string.action_export_clipboard), listOf(action(R.id.action_export_current), action(R.id.action_export_all)))
    resize()
    // Fixed window coordinates prevent resizing from re-anchoring or opening above the toolbar.
    val rootLocation = IntArray(2).also(anchor.rootView::getLocationOnScreen)
    val rootWindowLocation = IntArray(2).also(anchor.rootView::getLocationInWindow)
    popup.showAtLocation(anchor, Gravity.TOP or Gravity.LEFT,
        left - rootLocation[0] + rootWindowLocation[0], top - rootLocation[1] + rootWindowLocation[1])
    return popup
}
