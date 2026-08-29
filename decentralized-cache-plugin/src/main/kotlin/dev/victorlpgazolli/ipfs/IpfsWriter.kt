package dev.victorlpgazolli.ipfs

import dev.victorlpgazolli.utils.Logger
import java.io.File


internal interface IpfsWriter {
    fun write(file: File): String
}

internal fun IpfsWriter(
    ipfsConnectedSession: IpfsConnectedSession,
    logger: Logger,
): IpfsWriter {
    return IpfsWriterImpl(
        ipfs = ipfsConnectedSession,
        logger = logger
    )
}

private class IpfsWriterImpl(
    private val ipfs: IpfsConnectedSession,
    private val logger: Logger,
): IpfsWriter {
    override fun write(file: File): String {
        return ipfs.client.add.file(file).Hash
    }
}
