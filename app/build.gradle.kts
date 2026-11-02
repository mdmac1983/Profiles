plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tj.launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tj.launcher"
        // Android 10 (API 29) Go edition is the target device (BRIEF.md). targetSdk matches the
        // runtime so no newer-Android compatibility behaviour is silently assumed; revisit if
        // the app is ever to run on a newer phone.
        minSdk = 29
        targetSdk = 29
        versionCode = 1
        versionName = "0.1.0"
    }

    // The keystore is NOT in git (BRIEF.md). Signing is wired only when the file exists, so
    // tests, lint and debug builds work in any fresh container; a release build without it
    // is refused by tools/ensure-build-env.sh --release and by android.yml's gate, never
    // silently produced as an unsigned APK that looks installable.
    val ks = file("sideload.jks")
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
            // Go edition: small APK and low RAM matter. R8 shrinks code and resources.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("sideload")?.let { signingConfig = it }
        }
        debug {
            // Same key as release when present, so a debug install can never block a later
            // release install over a certificate mismatch.
            signingConfigs.findByName("sideload")?.let { signingConfig = it }
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        // Pinned toolchain (BRIEF.md): upgrades are deliberate, not lint-driven.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
        // Play Store rule only: this app is sideloaded onto a fixed Android 10 device, never
        // published, and targetSdk 29 is deliberate (BRIEF.md). Remove if that ever changes.
        disable += "ExpiredTargetSdkVersion"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions { unitTests { isReturnDefaultValues = true } }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    // Deliberately nothing but the Kotlin stdlib (added by the plugin): a Go launcher must stay
    // small and light on RAM, and the UI toolkit is an OPEN decision (BRIEF.md). Add a
    // dependency only with a reason written in BRIEF.md.
    testImplementation("junit:junit:4.13.2")
}
