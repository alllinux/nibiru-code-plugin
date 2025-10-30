pluginManagement {
    val kotlinVersion = settings.providers.gradleProperty("kotlinVersion").orElse("1.9.22").get()
    val intellijGradlePluginVersion = "1.16.1"

    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.intellij") version intellijGradlePluginVersion
    }

    repositories {
        // Try Maven Central first (often faster/more reliable)
        mavenCentral()
        // Then try Gradle Plugin Portal
        gradlePluginPortal()
        // Fallback to Google's Maven repository
        google()
        // JetBrains repository mirrors
        maven { url = uri("https://cache-redirector.jetbrains.com/maven-central") }
        maven { url = uri("https://cache-redirector.jetbrains.com/plugins.gradle.org/m2") }
        // Direct Maven Central fallback
        maven { url = uri("https://repo1.maven.org/maven2") }
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" ->
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version ?: kotlinVersion}")
                "org.jetbrains.intellij" ->
                    useModule("org.jetbrains.intellij.plugins:gradle-intellij-plugin:${requested.version ?: intellijGradlePluginVersion}")
            }
        }
    }
}

rootProject.name = "nibiru-code-plugin"
