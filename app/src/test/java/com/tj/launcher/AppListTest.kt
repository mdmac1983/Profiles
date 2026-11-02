package com.tj.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

class AppListTest {
    private fun app(label: String, pkg: String) = AppEntry(label, pkg, "$pkg.Main")

    @Test fun sortsByLabelIgnoringCaseThenPackage() {
        val sorted = AppList.sorted(listOf(app("camera", "b"), app("Calendar", "c"), app("Camera", "a")))
        assertEquals(listOf("c", "a", "b"), sorted.map { it.packageName })
    }

    @Test fun hidesItself() {
        val kept = AppList.withoutSelf(listOf(app("Launcher", "com.tj.launcher"), app("Maps", "m")), "com.tj.launcher")
        assertEquals(listOf("m"), kept.map { it.packageName })
    }

    @Test fun filtersByLabelIgnoringCaseAndTrimming() {
        val apps = listOf(app("Maps", "m"), app("Messages", "x"), app("Clock", "c"))
        assertEquals(listOf("Messages"), AppList.filter(apps, " ME ").map { it.label })
    }

    @Test fun blankQueryKeepsEverything() {
        val apps = listOf(app("Maps", "m"), app("Clock", "c"))
        assertEquals(2, AppList.filter(apps, "  ").size)
    }
}
