package dev.victorlpgazolli

import org.gradle.caching.configuration.AbstractBuildCache

data class IPFSConfig(
    val hostBaseUrl: String? = null,
    val publishKeyName: String, // used for /api/v0/name/publish
    val baseIpns: String, // used for fetching
)

open class DecentralizedConfiguration: AbstractBuildCache() {
//    lateinit var provider: IPFSConfig
}

