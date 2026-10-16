package com.aistra.hail.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import me.zhanghai.compose.preference.Preference

@Composable
internal fun AnchoredListPreference(
    key: String,
    value: String,
    onValueChange: (String) -> Unit,
    values: List<String>,
    title: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    summary: @Composable () -> Unit,
    valueToText: (String) -> String,
) {
    val menus = LocalSettingsMenuState.current
    val expanded = menus.activeKey == key
    var bounds by remember { mutableStateOf(Rect.Zero) }
    DisposableEffect(menus, key) {
        onDispose { if (menus.activeKey == key) menus.dismiss() }
    }
    Column(Modifier.fillMaxWidth().onGloballyPositioned {
        bounds = it.boundsInWindow()
        menus.updateBounds(key, bounds)
    }) {
        Preference(
            title = title,
            icon = icon,
            summary = summary,
            modifier = Modifier.fillMaxWidth(),
            onClick = { menus.toggle(key, bounds) },
        )
        SettingsOptionsMenu(expanded = expanded) {
            values.forEach { itemValue ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = expanded,
                    text = { Text(valueToText(itemValue)) },
                    leadingIcon = if (itemValue == value) {
                        { Icon(Icons.Outlined.Check, contentDescription = null) }
                    } else null,
                    onClick = {
                        menus.dismiss()
                        onValueChange(itemValue)
                    },
                )
            }
        }
    }
}
