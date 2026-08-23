package dev.victorlpgazolli.ipfs

import dev.victorlpgazolli.DecentralizedConfiguration
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration

internal fun interface IpfsConnector {
    operator fun invoke(configuration: DecentralizedConfiguration): IpfsConnectedSession
}

internal fun IpfsConnector(): IpfsConnector {
    return IpfsConnectorImpl()
}

private class IpfsConnectorImpl: IpfsConnector {
    override fun invoke(configuration: DecentralizedConfiguration): IpfsConnectedSession {
        val ipfsConfiguration = configuration.hostBaseUrl?.let {
            IPFSConfiguration("$it/api/v0/")
        } ?: IPFSConfiguration()

        return IpfsConnectedSession(
            baseUrl = configuration.hostBaseUrl ?: "http://127.0.0.1:5001",
            client = IPFS(ipfsConfiguration)
        )
    }

}