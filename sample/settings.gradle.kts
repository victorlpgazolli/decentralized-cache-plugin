rootProject.name = "sample"

pluginManagement {
    repositories {
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
        peerIpnsList = listOf(
            "/ipns/k51qzi5uqu5dl6o7ryaysxvnci02dt399cdnfk7v6pwhoka7qxuhoeu5v0qg76",
            "/ipns/k51qzi5uqu5di16xkjsdwb7k54b5kjyocw0i7fjds1wwi8aw9xkhr20bwb8uf4"
        )
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