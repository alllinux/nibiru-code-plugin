buildscript {
    repositories {
        mavenCentral()
        maven { url = uri("https://cache-redirector.jetbrains.com/maven-central") }
        maven { url = uri("https://cache-redirector.jetbrains.com/plugins.gradle.org/m2") }
    }
    dependencies {
        val kotlinVersion: String by project
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.1"
}

apply(plugin = "base")
apply(plugin = "org.jetbrains.kotlin.jvm")

val pluginGroup: String by project
val pluginVersion: String by project
val platformType: String by project
val platformVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project

group = pluginGroup
version = pluginVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

intellij {
    version.set(platformVersion)
    type.set(platformType) // IntelliJ IDEA Ultimate
    // PHP plugin not needed - this plugin works standalone
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    patchPluginXml {
        sinceBuild.set(pluginSinceBuild)
        // Target the IntelliJ 2024.2 (build 252) line and allow future bugfix releases
        untilBuild.set(pluginUntilBuild)
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
