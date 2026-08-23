package dev.victorlpgazolli.ipfs

import dev.victorlpgazolli.utils.Logger
import okhttp3.ResponseBody

fun interface ProvideHashToNetworkUseCase {
    operator fun invoke(hash: String)
}

class ProvideHashToNetworkUseCaseImpl(
    private val ipfsConnectedSession: IpfsConnectedSession,
    private val logger: Logger,
): ProvideHashToNetworkUseCase {
    override fun invoke(hash: String) {
        logger.log(
            context = "ProvideHashToNetworkUseCase",
            message = "Providing hash $hash to the network"
        )
        ipfsConnectedSession.connection.callCmd("routing/provide?arg=$hash").use(ResponseBody::string)
    }
}