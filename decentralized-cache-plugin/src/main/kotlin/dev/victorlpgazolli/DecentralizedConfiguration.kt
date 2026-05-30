package dev.victorlpgazolli

import org.gradle.caching.configuration.AbstractBuildCache


abstract class DecentralizedConfiguration: AbstractBuildCache() {
    val hostBaseUrl: String? = System.getenv("IPFS_NODE_URL") // if not defined 127.0.0.1:5001 will be used
    abstract var peerIpnsList: List<String>
    var verbose: Boolean = false
}

