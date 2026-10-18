package com.aistra.hail.ui.main

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aistra.hail.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.roundToInt

/** A single compact chooser instead of a second toolbar popup. */
internal fun Fragment.showSortOrderDialog(
    selected: String?,
    values: List<String>,
    labels: List<String>,
    onSelected: (String) -> Unit,
) {
    val dialog = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.sort_by)
        .setBackgroundInsetStart(0)
        .setBackgroundInsetEnd(0)
        .setSingleChoiceItems(labels.toTypedArray(), values.indexOf(selected)) { chooser, index ->
            chooser.dismiss()
            onSelected(values[index])
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
    val metrics = resources.displayMetrics
    val width = minOf((320 * metrics.density).roundToInt(),
        (metrics.widthPixels - (48 * metrics.density).roundToInt()).coerceAtLeast(1))
    dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
}
