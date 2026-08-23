package dev.victorlpgazolli.ipfs

sealed class IpfsReaderKey(
    val path: String
) {
    data class IpfsPath(
        val pathWithoutIpfsPrefix: String
    ): IpfsReaderKey(
        path = "/ipfs/$pathWithoutIpfsPrefix"
    ) {
        companion object
    }

    data class IpnsPath(
        val pathWithoutIpnsPrefix: String
    ): IpfsReaderKey(
        path = "/ipns/$pathWithoutIpnsPrefix"
    ) {
        companion object
    }

    val pathWithoutPrefix: String
        get() = when (this) {
            is IpfsPath -> this.pathWithoutIpfsPrefix
            is IpnsPath -> this.pathWithoutIpnsPrefix
        }
}

internal interface IpfsReader {
    fun read(key: IpfsReaderKey): String?
}

internal fun IpfsReader(
    ipfsConnectedSession: IpfsConnectedSession
): IpfsReader {
    return IpfsReaderImpl(
        ipfs = ipfsConnectedSession
    )
}

class IpfsReaderImpl(
    private val ipfs: IpfsConnectedSession,
): IpfsReader {
    override fun read(key: IpfsReaderKey): String? {
        return ipfs.cat(key.pathWithoutPrefix)
    }
}

data class IpfsReaderUnknownKeyException(
    val hash: String,
    override val message: String = "Unknown key type for IPFS reader",
    override val cause: Throwable? = null
): Exception(message, cause)