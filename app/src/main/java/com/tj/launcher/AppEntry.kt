package com.tj.launcher

/** One launchable app: pure data, so ordering and filtering are unit-testable on the JVM. */
data class AppEntry(val label: String, val packageName: String, val activityName: String)

object AppList {
    /** Case-insensitive label order; the package name breaks ties so the order is stable. */
    fun sorted(apps: List<AppEntry>): List<AppEntry> =
        apps.sortedWith(compareBy<AppEntry>({ it.label.lowercase() }, { it.packageName }))

    /** Hide the launcher itself: it is the Home screen, not something to launch from it. */
    fun withoutSelf(apps: List<AppEntry>, selfPackage: String): List<AppEntry> =
        apps.filterNot { it.packageName == selfPackage }

    /** Substring match on the label, ignoring case; a blank query keeps everything. */
    fun filter(apps: List<AppEntry>, query: String): List<AppEntry> =
        if (query.isBlank()) apps else apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
}
