package com.aistra.hail.ui.home

/** Keeps pinned apps first and uses a stable name/package tie-break for equal install times. */
internal fun <T> orderHomeApps(
    apps: List<T>,
    byInstall: Boolean,
    pinned: (T) -> Boolean,
    installed: (T) -> Long?,
    byName: Comparator<T>,
): List<T> {
    val times = if (byInstall) apps.associateWith(installed) else emptyMap()
    return apps.sortedWith { a, b ->
        val pinOrder = pinned(b).compareTo(pinned(a))
        if (pinOrder != 0) pinOrder else {
            val timeOrder = if (byInstall) {
                (times[a] ?: Long.MAX_VALUE).compareTo(times[b] ?: Long.MAX_VALUE)
            } else 0
            if (timeOrder != 0) timeOrder else byName.compare(a, b)
        }
    }
}
