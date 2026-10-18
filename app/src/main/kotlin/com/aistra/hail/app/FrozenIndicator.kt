package com.aistra.hail.app

enum class FrozenIndicator(val key: String) {
    BADGE("badge"), TEXT("text"), OVERLAY("overlay"), NONE("none");

    companion object {
        fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: BADGE
    }
}

internal fun homeGridColumns(availableWidth: Int, density: Float, iconDp: Int, compact: Boolean): Int {
    if (availableWidth <= 0 || density <= 0) return 1
    val cell = (iconDp + if (compact) 8 else 16) * density
    return (availableWidth / cell).toInt().coerceAtLeast(1)
}
