package com.aistra.hail.ui.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.shape.MaterialShapeDrawable
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
    val left = if (anchor.layoutDirection == View.LAYOUT_DIRECTION_RTL)
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
        background = MaterialShapeDrawable.createWithElevationOverlay(context, dp(6).toFloat()).apply {
            shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(dp(16).toFloat()).build()
        }
        clipToOutline = true
    }
    val popup = PopupWindow(scroll, width, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
        setBackgroundDrawable(GradientDrawable().apply { setColor(surface); cornerRadius = dp(16).toFloat() })
        elevation = dp(6).toFloat()
        isOutsideTouchable = true
        inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        animationStyle = R.style.HomeMenuAnimation
    }
    fun resize() {
        scroll.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST))
        if (popup.isShowing) popup.update(width, scroll.measuredHeight)
        else popup.height = scroll.measuredHeight
    }
    fun row(label: CharSequence, child: Boolean = false, selected: Boolean = false,
            arrow: ImageView? = null): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = dp(48)
        setPaddingRelative(dp(if (child) 28 else 20), dp(10), dp(20), dp(10))
        val ripple = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, ripple, true)
        setBackgroundResource(ripple.resourceId)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (child) addView(TextView(context).apply {
            text = if (selected) "✓" else ""
            setTextColor(primary)
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, LinearLayout.LayoutParams(dp(28), ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(TextView(context).apply {
            text = label
            textSize = 14f
            setTextColor(if (selected) primary else foreground)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        if (arrow != null) addView(arrow, LinearLayout.LayoutParams(dp(18), dp(18)))
        contentDescription = label
        isSelected = selected
        isFocusable = true
    }
    class Group(val heading: View, val arrow: ImageView, val items: LinearLayout) {
        var expanded = false
        var animation: ValueAnimator? = null
    }
    val groups = mutableListOf<Group>()
    fun reveal(group: Group, opening: Boolean) {
        val items = group.items
        val start = if (items.visibility == View.GONE) 0 else
            items.layoutParams.height.takeIf { it >= 0 } ?: items.height
        val previous = group.animation
        group.animation = null
        previous?.cancel()
        group.expanded = opening
        group.arrow.animate().rotation(if (opening) 180f else 0f).setDuration(180).start()
        ViewCompat.setStateDescription(group.heading, context.getString(if (opening)
            R.string.menu_expanded else R.string.menu_collapsed))
        items.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        val target = if (opening) items.measuredHeight else 0
        val startAlpha = if (start == 0) 0f else items.alpha
        items.visibility = View.VISIBLE
        items.layoutParams.height = start
        val animator = ValueAnimator.ofInt(start, target).apply {
            duration = if (opening) 200 else 150
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                items.layoutParams.height = it.animatedValue as Int
                items.alpha = startAlpha + ((if (opening) 1f else 0f) - startAlpha) * it.animatedFraction
                items.requestLayout()
                resize()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (group.animation !== animation) return
                    group.animation = null
                    items.visibility = if (opening) View.VISIBLE else View.GONE
                    items.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    items.alpha = 1f
                    resize()
                }
            })
        }
        group.animation = animator
        animator.start()
    }
    fun divider() {
        rows.addView(View(context).apply {
            setBackgroundColor(MaterialColors.getColor(anchor, com.google.android.material.R.attr.colorOutline).let {
                androidx.core.graphics.ColorUtils.setAlphaComponent(it, 45)
            })
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply {
                setMargins(dp(12), dp(6), dp(12), dp(6))
            }
        })
    }
    fun group(title: CharSequence, choices: List<Triple<CharSequence, Boolean, () -> Unit>>) {
        val arrow = ImageView(context).apply {
            setImageResource(R.drawable.ic_menu_expand)
            imageTintList = android.content.res.ColorStateList.valueOf(foreground)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        val heading = row(title, arrow = arrow)
        val children = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE }
        choices.forEach { (label, selected, action) -> children.addView(row(label, child = true, selected = selected).apply {
            setOnClickListener { popup.dismiss(); action() }
        }) }
        rows.addView(heading)
        rows.addView(children)
        val group = Group(heading, arrow, children)
        groups += group
        heading.setOnClickListener {
            val opening = !group.expanded
            groups.forEach { other ->
                val expand = other === group && opening
                if (other.expanded != expand) reveal(other, expand)
            }
        }
        ViewCompat.setStateDescription(heading, context.getString(R.string.menu_collapsed))
    }
    group(context.getString(R.string.sort_by), listOf(
        HailData.SORT_NAME to R.string.home_sort_name,
        HailData.SORT_INSTALL to R.string.home_sort_install,
    ).map { (key, label) ->
        Triple(context.getString(label), HailData.homeSortBy == key) { onSort(key) }
    })
    divider()
    listOf(R.id.action_freeze_current, R.id.action_unfreeze_current, R.id.action_freeze_all,
        R.id.action_unfreeze_all, R.id.action_freeze_non_whitelisted).forEach { id ->
        val item = menu.findItem(id)
        rows.addView(row(item.title ?: "").apply { setOnClickListener { popup.dismiss(); onAction(item) } })
    }
    divider()
    fun action(id: Int): Triple<CharSequence, Boolean, () -> Unit> {
        val item = menu.findItem(id)
        return Triple(item.title ?: "", false) { onAction(item) }
    }
    group(context.getString(R.string.action_import_app), listOf(action(R.id.action_import_clipboard), action(R.id.action_import_frozen)))
    group(context.getString(R.string.action_export_clipboard), listOf(action(R.id.action_export_current), action(R.id.action_export_all)))
    popup.setOnDismissListener {
        groups.forEach { group ->
            val animation = group.animation
            group.animation = null
            animation?.cancel()
            group.arrow.animate().cancel()
        }
    }
    resize()
    // Fixed window coordinates prevent resizing from re-anchoring or opening above the toolbar.
    val rootLocation = IntArray(2).also(anchor.rootView::getLocationOnScreen)
    val rootWindowLocation = IntArray(2).also(anchor.rootView::getLocationInWindow)
    popup.showAtLocation(anchor, Gravity.TOP or Gravity.LEFT,
        left - rootLocation[0] + rootWindowLocation[0], top - rootLocation[1] + rootWindowLocation[1])
    return popup
}
