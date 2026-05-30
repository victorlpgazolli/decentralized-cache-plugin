import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serialization)
}

val pluginId = "dev.victorlpgazolli.decentralized-cache-plugin"
val mavenGroupId = "dev.victorlpgazolli"
val versionNumber = "1.1.0"

group = "dev.victorlpgazolli"
version = versionNumber


gradlePlugin {
    plugins {
        create("decentralizedCachePlugin") {
            id = pluginId
            group = mavenGroupId
            implementationClass = "dev.victorlpgazolli.DecentralizedCachePlugin"
            version = versionNumber
            tags.set(listOf("cache", "ipfs", "gradle", "distributed"))
            displayName = "Decentralized Gradle Build Cache (IPFS)"
            description = "Gradle build cache backed by IPFS"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.ipfs)
    implementation(libs.novaCrypto)
    implementation(libs.kotlinx.serialization)
}


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    if (System.getenv("CI") != null) {
        signAllPublications()
    }

    coordinates(
        groupId = mavenGroupId,
        version = versionNumber
    )

    pom {
        name.set("Decentralized Gradle build cache plugin")
        description.set("Decentralized Gradle build cache plugin")
        inceptionYear.set("2025")
        url.set("https://github.com/victorlpgazolli/decentralized-cache-plugin")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("victorlpgazolli")
            }
        }

        scm {
            url.set("https://github.com/victorlpgazolli/decentralized-cache-plugin")
        }
    }
}