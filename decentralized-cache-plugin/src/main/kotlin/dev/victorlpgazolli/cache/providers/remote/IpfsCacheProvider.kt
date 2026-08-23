package dev.victorlpgazolli.cache.providers.remote

import dev.victorlpgazolli.cache.model.CacheKeyType
import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.ipfs.IpfsConnectedSession
import dev.victorlpgazolli.ipfs.IpfsReader
import dev.victorlpgazolli.ipfs.toReaderKey
import dev.victorlpgazolli.utils.Logger
import java.io.File


internal class IpfsCacheProvider(
    private val ipfsReader: IpfsReader,
    private val ipfsConnectedSession: IpfsConnectedSession,
    private val logger: Logger,
) : CacheProvider {

    override fun get(key: String): ByteArray? = runCatching {
        val readerKeyType = key.toReaderKey()
        return ipfsReader.read(readerKeyType)?.toByteArray()
    }.getOrNull()

    override fun put(key: CacheKeyType, value: ByteArray): CacheKeyType = runCatching {
       when (key) {
            is CacheKeyType.FilePath -> {
                val result = ipfsConnectedSession.client.add.file(File(key.fullpath))

                return key.copy(
                    ipfsHash = result.Hash
                )
            }
            is CacheKeyType.Object -> {
                return key
            }
       }
    }.onFailure {
        logger.log("put", "Failed to put value for key: $key, error: ${it.message}")
    }.getOrThrow()

    override fun remove(key: String) {
        logger.log("remove", "Removing value for key: $key")
        val readerKeyType = key.toReaderKey()
        logger.log("remove", "Cannot remove IPFS path: ${readerKeyType.pathWithoutPrefix}")
    }
}