package dev.victorlpgazolli.ipfs

import dev.victorlpgazolli.utils.Logger
import java.util.concurrent.CompletableFuture
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody

fun interface ProvideHashToNetworkUseCase {
    operator fun invoke(cacheKey: String, ipfsHash: String)
}

internal class ProvideHashToNetworkUseCaseImpl(
    private val ipfsConnectedSession: IpfsConnectedSession,
    private val logger: Logger,
) : ProvideHashToNetworkUseCase {

    private val json = Json { ignoreUnknownKeys = true }

    override fun invoke(cacheKey: String, ipfsHash: String) {
        runCatching {
                logger.log(
                    context = "ProvideHashToNetworkUseCase",
                    message = "Providing IPFS hash $ipfsHash for cache key $cacheKey to the network",
                )

                ipfsConnectedSession.connection
                    .callCmd("routing/provide?arg=$ipfsHash")
                    .use(ResponseBody::string)

                CompletableFuture.runAsync { updateAndPublishManifest(cacheKey, ipfsHash) }
            }
            .onFailure {
                logger.log(
                    context = "ProvideHashToNetworkUseCase",
                    message = "Failed to provide hash $ipfsHash to the network: ${it.message}",
                )
            }
    }

    private fun updateAndPublishManifest(cacheKey: String, ipfsHash: String) =
        runCatching {
                logger.log("ManifestUpdate", "Publishing manifest $ipfsHash to IPNS...")
                val publishResponse =
                    ipfsConnectedSession.connection
                        .callCmd("name/publish?arg=$ipfsHash")
                        .use(ResponseBody::string)

                logger.log("ManifestUpdate", "Successfully published to IPNS: $publishResponse")
            }
            .onFailure {
                logger.log("ManifestUpdate", "Failed to update and publish manifest: ${it.message}")
            }
}
