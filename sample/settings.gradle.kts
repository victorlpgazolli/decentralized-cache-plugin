rootProject.name = "sample"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
    }
    // Inclui o build pai para resolver o plugin
    includeBuild("../")
}

// 1. Aplique o plugin AQUI, no settings
plugins {
    id("dev.victorlpgazolli.decentralized-cache-plugin")
}

// 2. Configure o cache AQUI
buildCache {
    local { isEnabled = false }

    // O plugin já registrou 'DecentralizedConfiguration' no seu SettingsPlugin
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