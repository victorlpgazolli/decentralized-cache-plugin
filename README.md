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

The way i see it, running your own build cache server is not ideal, you would need to setup the server, configure and maintain it.
I thing a cloud storage solution sis enough, it will probably be cheaper and easier to maintain, there are a few things you would need to configure it, and worry about data transfer, but overall it sounds good.

This project tries to approach the problem from a different angle, instead of relying on a centralized server or cloud storage,
the main goal here is to share cache between developers in a decentralized way, using a established peer-to-peer network.

## How it works:

Every time a developer compile the project, the plugin checks if someone on the network already has this build cache, if so, it downloads it and uses it.
When you download the build cache, you also become a seed for the build cache, so you can share it with other developers.

If no one has this build cache, the project compiles as it normally would, and after that the plugin announces that you have this build cache available in case someone else needs it.

For the network i chose [IPFS](https://ipfs.io/) since it is a well known, and it has a lot of support from the community. So to use this plugin, you would need to install `ipfs` and run it in the background.

## Usage

// TODO

### Apply plugin in settings.gradle

// TODO

### Configure build cache

// TODO

## Compatibility

The plugin is compatible with Gradle `8.4` and higher and Java `17` and higher.

