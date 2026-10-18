package com.aistra.hail.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeAppOrderTest {
    private data class App(val name: String, val time: Long?, val pinned: Boolean = false)
    private fun order(apps: List<App>, install: Boolean) = orderHomeApps(
        apps, install, { it.pinned }, { it.time }, compareBy { it.name })

    @Test fun nameOrderIgnoresInstallDate() {
        val apps = listOf(App("Zebra", 1), App("Alpha", 30), App("Beta", 10))
        assertEquals(listOf("Alpha", "Beta", "Zebra"), order(apps, false).map { it.name })
    }

    @Test fun installOrderIsOldestFirst() {
        val apps = listOf(App("Alpha", 30), App("Zebra", 1), App("Beta", 10))
        assertEquals(listOf("Zebra", "Beta", "Alpha"), order(apps, true).map { it.name })
    }

    @Test fun pinnedAppsRemainFirstInBothModes() {
        val apps = listOf(App("Alpha", 1), App("Zebra", 30, true), App("Beta", 10))
        for (install in listOf(false, true)) assertEquals("Zebra", order(apps, install).first().name)
    }

    @Test fun unavailablePackagesGoAfterKnownInstallDates() {
        val apps = listOf(App("Gone", null), App("Installed", 30))
        assertEquals(listOf("Installed", "Gone"), order(apps, true).map { it.name })
    }

    @Test fun equalTimesHaveStableNameOrder() {
        val apps = listOf(App("Zebra", 10), App("Alpha", 10))
        assertEquals(listOf("Alpha", "Zebra"), order(apps, true).map { it.name })
    }
}
