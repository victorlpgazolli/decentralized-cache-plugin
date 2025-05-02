package dev.victorlpgazolli.service

import dev.victorlpgazolli.DecentralizedConfiguration
import dev.victorlpgazolli.client.IpfsClient
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory

private val LOG_TAG = "[decentralized-cache]"
internal class IpfsBuildCacheServiceFactory : BuildCacheServiceFactory<DecentralizedConfiguration> {
    override fun createBuildCacheService(
        decentralizedConfiguration: DecentralizedConfiguration,
        describer: BuildCacheServiceFactory.Describer,
    ): BuildCacheService {
        println("$LOG_TAG creating build cache service")

        val ipfsClient = IpfsClient(
//            decentralizedConfiguration.provider
        )

       println("$LOG_TAG Using ipfs client with version ${ipfsClient.version}")

        return IpfsBuildCacheService(
            ipfsClient
        )
    }
}