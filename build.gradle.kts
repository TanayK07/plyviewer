// Root-level build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // Add the Hilt plugin classpath.
        classpath(libs.hilt.android.gradle.plugin)
    }
}

// You can also declare common plugins (if you use a version catalog)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
