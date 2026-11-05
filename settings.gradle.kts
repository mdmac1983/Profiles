pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Launcher"
include(":app")
include(":phoneguide")
include(":testnotifier")   // CI-only test app for the emulator check (never released)
// AOSP Launcher3 icon library (see launcher3/PATCHES.md)
include(":iconloaderlib")
project(":iconloaderlib").projectDir = file("launcher3/iconloaderlib")
