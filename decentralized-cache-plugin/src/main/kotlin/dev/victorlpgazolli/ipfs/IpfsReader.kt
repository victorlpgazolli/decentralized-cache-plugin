package dev.victorlpgazolli.ipfs

import dev.victorlpgazolli.utils.Logger
import java.io.InputStream

sealed class IpfsReaderKey(val path: String) {
    data class IpfsPath(val pathWithoutIpfsPrefix: String) :
        IpfsReaderKey(path = "/ipfs/$pathWithoutIpfsPrefix") {
        companion object
    }

    data class IpnsPath(val pathWithoutIpnsPrefix: String) :
        IpfsReaderKey(path = "/ipns/$pathWithoutIpnsPrefix") {
        companion object
    }

    val pathWithoutPrefix: String
        get() =
            when (this) {
                is IpfsPath -> this.pathWithoutIpfsPrefix
                is IpnsPath -> this.pathWithoutIpnsPrefix
            }
}

internal interface IpfsReader {
    fun read(key: IpfsReaderKey): InputStream
}

internal fun IpfsReader(ipfsConnectedSession: IpfsConnectedSession, logger: Logger): IpfsReader {
    return IpfsReaderImpl(ipfs = ipfsConnectedSession, logger = logger)
}

class IpfsReaderImpl(private val ipfs: IpfsConnectedSession, private val logger: Logger) :
    IpfsReader {
    override fun read(key: IpfsReaderKey): InputStream {
        val hash =
            when (key) {
                is IpfsReaderKey.IpnsPath -> ipfs.nameResolve(key.pathWithoutPrefix)
                is IpfsReaderKey.IpfsPath -> key.pathWithoutPrefix
            }
        return ipfs.catStream(hash)
    }
}

data class IpfsReaderUnknownKeyException(
    val hash: String,
    override val message: String = "Unknown key type for IPFS reader",
    override val cause: Throwable? = null,
) : Exception(message, cause)
