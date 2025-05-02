pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}
rootProject.name = "decentralized-cache-plugin"

plugins {
    id("dev.victorlpgazolli.decentralized-cache-plugin") version "1.0.0"
}

buildCache {
    local { isEnabled = false }

    remote<dev.victorlpgazolli.DecentralizedConfiguration> {
        isEnabled = true
        isPush    = true
    }
}