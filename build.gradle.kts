plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.serialization)

}
val projectGroupId = "dev.victorlpgazolli.decentralized-cache-plugin"
val projectArtifactId = "decentralized-cache-plugin"
val versionNumber = "1.0.0"

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }

}


gradlePlugin {
    plugins {
        create("decentralizedCachePlugin") {
            id = projectGroupId
            group = projectGroupId
            implementationClass = "dev.victorlpgazolli.DecentralizedCachePlugin"
            version = versionNumber
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }

}


dependencies {
    implementation(libs.ipfs)
    implementation("io.github.novacrypto:Base58:2022.01.17")
    implementation(libs.kotlinx.serialization)
}
publishing {
    repositories {
        mavenLocal()
    }
    publications {
//        named<MavenPublication>("pluginMaven") {
//            groupId    = projectGroupId
//            artifactId = projectArtifactId
//            version    = versionNumber
//        }
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = projectGroupId
            artifactId = projectArtifactId
            version = versionNumber

            pom {
                name = "Decentralized cache plugin"
                description = "Use IPFS as the backend for a Gradle remote build cache"
                inceptionYear = "2025"
                url = "https://github.com/victorlpgazolli/decentralized-cache-plugin"
                version = versionNumber
                groupId = projectGroupId
                artifactId = projectArtifactId
            }
        }
    }
}
