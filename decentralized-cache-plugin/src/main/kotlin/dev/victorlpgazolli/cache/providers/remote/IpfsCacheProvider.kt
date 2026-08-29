package dev.victorlpgazolli.cache.providers.remote

import dev.victorlpgazolli.cache.ManifestCacheHelper
import dev.victorlpgazolli.cache.model.CacheKeyType
import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.ipfs.IpfsConnectedSession
import dev.victorlpgazolli.ipfs.IpfsReader
import dev.victorlpgazolli.ipfs.IpfsReaderKey
import dev.victorlpgazolli.utils.Logger
import java.io.File

internal class IpfsCacheProvider(
    private val manifestCacheHelper: ManifestCacheHelper,
    private val ipfsReader: IpfsReader,
    private val ipfsConnectedSession: IpfsConnectedSession,
    private val logger: Logger,
) : CacheProvider {

    private val remoteManifest: Map<String, String> by lazy {
        logger.log("IpfsCacheProvider", "Fetching peer manifests...")
        manifestCacheHelper.fetchCacheEntries()
    }

    override fun get(key: String): ByteArray? = runCatching {
        val ipfsHash = remoteManifest[key]

        if (ipfsHash == null) {
            logger.log("get", "Key $key not found in P2P manifest.")
            return null
        }

        val readerKeyType = IpfsReaderKey.IpfsPath(ipfsHash)

        logger.log("get", "Downloading $key from IPFS ($ipfsHash)")
        return ipfsReader.read(readerKeyType).use { it.readBytes() }

    }.onFailure {
        logger.log("get", "Failed to get value from IPFS for key: $key, error: ${it.message}")
    }.getOrNull()

    override fun put(key: CacheKeyType, value: ByteArray): CacheKeyType = runCatching {
        when (key) {
            is CacheKeyType.FilePath -> {
                val result = ipfsConnectedSession.client.add.file(File(key.fullpath))
                ipfsConnectedSession.client.pins.add(result.Hash)

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
        logger.log("remove", "Cannot remove immutable IPFS content for key: $key")
    }
}