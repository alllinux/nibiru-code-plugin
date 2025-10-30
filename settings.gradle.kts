pluginManagement {
    repositories {
        // Try Maven Central first (often faster/more reliable)
        mavenCentral()
        // Then try Gradle Plugin Portal
        gradlePluginPortal()
        // Fallback to Google's Maven repository
        google()
        // JetBrains repository
        maven { url = uri("https://cache-redirector.jetbrains.com/maven-central") }
        maven { url = uri("https://cache-redirector.jetbrains.com/plugins.gradle.org/m2") }
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" ->
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                "org.jetbrains.intellij" ->
                    useModule("org.jetbrains.intellij.plugins:gradle-intellij-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "nibiru-code-plugin"
