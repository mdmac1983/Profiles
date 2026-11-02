pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Launcher"
include(":app")
// AOSP Launcher3 icon library (see launcher3/PATCHES.md)
include(":iconloaderlib")
project(":iconloaderlib").projectDir = file("launcher3/iconloaderlib")
