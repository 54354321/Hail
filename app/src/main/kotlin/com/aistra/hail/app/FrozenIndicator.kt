package com.aistra.hail.app

enum class FrozenIndicator(val key: String) {
    BADGE("badge"), TEXT("text"), OVERLAY("overlay"), NONE("none");

    companion object {
        fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: BADGE
    }
}

internal fun homeGridColumns(availableWidth: Int, density: Float, iconDp: Int): Int {
    if (availableWidth <= 0 || density <= 0) return 1
    val cell = (iconDp + 16) * density
    return (availableWidth / cell).toInt().coerceAtLeast(1)
}

internal fun resolvedIconSize(saved: Float?, legacyCompact: Boolean): Int {
    val value = saved?.takeIf { it.isFinite() } ?: if (legacyCompact) 56f else 64f
    return (48 + kotlin.math.round((value.coerceIn(48f, 96f) - 48f) / 8f).toInt() * 8)
}
