rootProject.name = "sample"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
    }

    includeBuild("../")
}

plugins {
    id("dev.victorlpgazolli.decentralized-cache-plugin")
}

buildCache {
    local { isEnabled = false }

    remote<dev.victorlpgazolli.DecentralizedConfiguration> {
        isEnabled = true
        isPush = true
        peerIpnsList = emptyList()
        verbose = true
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}