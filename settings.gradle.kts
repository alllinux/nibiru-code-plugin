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
    }
}

rootProject.name = "nibiru-code-plugin"
