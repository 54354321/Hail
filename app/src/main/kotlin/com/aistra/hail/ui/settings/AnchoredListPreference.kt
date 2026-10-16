package com.aistra.hail.ui.settings

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.IntRect
import kotlin.math.roundToInt
import me.zhanghai.compose.preference.Preference

/** One stable row anchor for both enter and exit; closing never changes its position. */
@Composable
internal fun AnchoredListPreference(
    value: String,
    onValueChange: (String) -> Unit,
    values: List<String>,
    title: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    summary: @Composable () -> Unit,
    valueToText: (String) -> String,
) {
    var expanded by remember { mutableStateOf(false) }
    var rowBounds by remember { mutableStateOf<IntRect?>(null) }
    // Measure the actual clickable row. Popup's inferred parent can be a zero-height layout
    // from subcomposition, so it must never be used as the row's bottom edge.
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        Preference(
            title = title,
            icon = icon,
            summary = summary,
            modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                val origin = coordinates.positionOnScreen()
                if (origin.x.isFinite() && origin.y.isFinite()) {
                    val left = origin.x.roundToInt()
                    val top = origin.y.roundToInt()
                    rowBounds = IntRect(left, top, left + coordinates.size.width, top + coordinates.size.height)
                }
            },
            onClick = { expanded = !expanded },
        )
        SettingsOptionsMenu(
            width = maxWidth,
            rowBounds = rowBounds,
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            values.forEach { itemValue ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = { Text(valueToText(itemValue)) },
                    leadingIcon = if (itemValue == value) {
                        { Icon(Icons.Outlined.Check, contentDescription = null) }
                    } else null,
                    onClick = {
                        expanded = false
                        onValueChange(itemValue)
                    },
                )
            }
        }
    }
}
