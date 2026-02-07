package dev.victorlpgazolli.service

import dev.victorlpgazolli.DecentralizedConfiguration
import dev.victorlpgazolli.client.CacheManifest
import dev.victorlpgazolli.client.IpfsClient
import dev.victorlpgazolli.utils.SimpleLogger
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory

private val LOG_TAG = "[decentralized-cache]"
internal class IpfsBuildCacheServiceFactory : BuildCacheServiceFactory<DecentralizedConfiguration> {
    override fun createBuildCacheService(
        decentralizedConfiguration: DecentralizedConfiguration,
        describer: BuildCacheServiceFactory.Describer,
    ): BuildCacheService {
        println("$LOG_TAG creating build cache service")

        val logger = SimpleLogger()
        val ipfsClient = IpfsClient(
            configuration = decentralizedConfiguration,
            cacheManifest = CacheManifest(logger),
            logger = logger
        )

       println("$LOG_TAG Using ipfs client with version ${ipfsClient.version}")

        return IpfsBuildCacheService(
            ipfsClient = ipfsClient,
            logger = logger
        )
    }
}