// Replaces upstream's build.gradle (AGP 3.x). Same sources, current plugin.
plugins { id("com.android.library") }

android {
    namespace = "com.android.launcher3.icons"
    compileSdk = 36
    defaultConfig { minSdk = 29 }
    sourceSets {
        getByName("main") {
            java.srcDirs("src", "src_full_lib")
            manifest.srcFile("AndroidManifest.xml")
            res.srcDirs("res")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint { abortOnError = false }
}
dependencies {
    implementation("androidx.core:core:1.13.1")
}
