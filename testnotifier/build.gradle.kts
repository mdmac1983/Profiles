plugins {
    id("com.android.application")
}

// CI-ONLY test helper for the emulator check (AB3, Tj 2026-11-05: "never show notifications from apps opened in the quickstep launcher"). An app that posts ONE notification every time it is opened, so the
// scenario can prove which apps' notifications the custom launcher shows. Two flavours: "quick" (com.tj.testnotifier = the app Tj opens in Quickstep) and "ctl" (com.tj.testnotifier.ctl = an app only the
// custom launcher opened, whose notification must stay). Built only by emulator.yml; never signed, never attached to a Release, no permissions.
android {
    namespace = "com.tj.testnotifier"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.tj.testnotifier"
        minSdk = 29
        targetSdk = 29
        versionCode = 1
        versionName = "1.0"
    }
    flavorDimensions += "role"
    productFlavors {
        create("quick") {
            dimension = "role"
            manifestPlaceholders["appLabel"] = "Notifier quick"
        }
        create("ctl") {
            dimension = "role"
            applicationIdSuffix = ".ctl"
            manifestPlaceholders["appLabel"] = "Notifier ctl"
        }
    }
    lint { abortOnError = false }
}
