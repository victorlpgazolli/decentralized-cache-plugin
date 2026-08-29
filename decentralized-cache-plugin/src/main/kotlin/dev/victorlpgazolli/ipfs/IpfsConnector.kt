package dev.victorlpgazolli.ipfs

import dev.victorlpgazolli.DecentralizedConfiguration
import dev.victorlpgazolli.utils.Logger
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration

internal fun interface IpfsConnector {
    operator fun invoke(configuration: DecentralizedConfiguration): IpfsConnectedSession
}

internal fun IpfsConnector(logger: Logger): IpfsConnector {
    return IpfsConnectorImpl(logger)
}

private class IpfsConnectorImpl(private val logger: Logger) : IpfsConnector {
    override fun invoke(configuration: DecentralizedConfiguration): IpfsConnectedSession {
        logger.log(
            context = "IpfsConnector",
            message =
                "Connecting to IPFS host at ${configuration.hostBaseUrl ?: "http://127.0.0.1:5001"}",
        )
        val ipfsConfiguration =
            configuration.hostBaseUrl?.let { IPFSConfiguration("$it/api/v0/") }
                ?: IPFSConfiguration()

        return IpfsConnectedSession(
            baseUrl = configuration.hostBaseUrl ?: "http://127.0.0.1:5001",
            client = IPFS(ipfsConfiguration),
        )
    }
}
