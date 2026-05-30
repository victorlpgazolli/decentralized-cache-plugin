pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
plugins {
    id("dev.victorlpgazolli.decentralized-cache-plugin") version "1.1.0"
}

buildCache {
    local { isEnabled = false }

    remote<dev.victorlpgazolli.DecentralizedConfiguration> {
        isEnabled = true
        isPush    = true
        peerIpnsList = listOf(
            "/ipns/k51qzi5uqu5dhv6ac3tjl39vm9nljzd2yfxt4rgrhg41crl3337bhpxletgt16",
            "/ipns/k51qzi5uqu5djvi4be7r7u511n7hok1yntr4am7glm9kmdd2weyr7ov16ecpea",
        )
        verbose = true
    }
}

rootProject.name = "sample"
