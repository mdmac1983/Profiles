plugins {
    id("com.android.application")
}

// The PHONE-side adb guide (Tj, 2026-11-04): a tiny app for the host phone that lists every adb command the tablet needs, one per card with a Copy button.
// Plain views, no dependencies, no permissions. Signed with the same key as the launcher by android.yml and attached to the same Release.
android {
    namespace = "com.tj.phoneguide"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.tj.phoneguide"
        minSdk = 21
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
    }
    val ks = file("../app/sideload.jks")
    if (ks.exists()) {
        signingConfigs.create("sideload") {
            storeFile = ks
            storePassword = "launcher"
            keyAlias = "launcher"
            keyPassword = "launcher"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfigs.findByName("sideload")?.let { signingConfig = it }
        }
        debug { signingConfigs.findByName("sideload")?.let { signingConfig = it } }
    }
    lint { abortOnError = false }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
