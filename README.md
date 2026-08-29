# Decentralized Gradle Build Cache Plugin

Plugin to use a decentralized network as a medium to fetch and share cache for Gradle builds.

## Motivation:
Gradle build cache is a great feature to speed up builds by reusing outputs from previous builds. 
Reading the [documentation](https://docs.gradle.org/current/userguide/build_cache.html) i could find 2 ways of approaching this:
1. You could run your own build cache server, using the [docker image](https://hub.docker.com/r/gradle/build-cache-node/) for example
2. Using a [managed service](https://gradle.com/develocity/product/build-cache/) which probably is not free

There are plugins that you could use to store your build cache in a cloud storage:
- [androidx/gcp-gradle-build-cache](https://github.com/androidx/gcp-gradle-build-cache) (using aws or gcp storage provider)
- [craigatk/object-store-cache-plugin](https://github.com/craigatk/object-store-cache-plugin) (reference to the project that i forked from) 
- ... probably many more ...

The way i see it, sometimes running your own build cache server can be challenging, you would need to setup the server, configure and maintain it.
I think a cloud storage solution is enough, it will probably be cheaper and easier to maintain, but you would still need to configure it, and worry about data transfer, but overall it sounds good.

This project tries to approach the problem from a different angle, instead of relying on a centralized server or cloud storage,
the main goal here is to share cache between developers in a decentralized way, using a established peer-to-peer network.
I couldn't find any other plugin that does this, so i decided to create one.


## How it works:

Every time a developer compile the project, the plugin checks if someone on the network already has this build cache, if so, it downloads it and uses it.
When you download the build cache, you also become a seed for it, so you can share with other developers.

If no one can provide the cache you need, your project compiles as it normally would, and after that the plugin announces to the network that you have this specific build cache available in case someone else needs it.

The decentralized network i chose is called [IPFS](https://ipfs.io/), i highly recommend reading the documentation to understand how it works. For this project specifically, it satisfies the requirements and it seems to have support from the community, which is enough for me.

>[!IMPORTANT]
> DO NOT use this plugin in production, it is just a proof of concept... use it at your own risk.

## Usage

### Apply plugin in settings.gradle

```kotlin
pluginManagement {
    repositories {
        mavenCentral() // required for this plugin to be resolved
        maven("https://jitpack.io") // required for the plugin dependency to be resolved
    }
}

plugins {
    id("dev.victorlpgazolli.decentralized-cache-plugin")
}
```

### Configure build cache

```kotlin
buildCache {
    // forcing remote cache, not required:
    local { isEnabled = false } 

    remote<dev.victorlpgazolli.DecentralizedConfiguration> {
        isEnabled = true
        isPush = true

        // list of ipns of peers you want to fetch 
        // your cache from, not required but recommended:
        peerIpnsList = listOf() 
    }
}
```

### Running ipfs daemon

This project was created with the assumption that you already have an ipfs node running on your machine, if not, please follow the instructions on the [ipfs documentation](https://docs.ipfs.io/install/command-line/) to install and run it.

It is important to have in mind that IPFS connects you to a public p2p network, so any cache created with this plugin enabled will be public,
So if you are working on a private project you should consider using IPFS in a private network instead. My recommendation is to set up like I did [here](./init-config.sh)

This command will start the ipfs daemon, which is required for the plugin to work properly:
```shell
ipfs daemon
```
