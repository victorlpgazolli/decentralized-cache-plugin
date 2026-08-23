package dev.victorlpgazolli.cache.providers.remote

import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.ipfs.IpfsReader
import dev.victorlpgazolli.ipfs.toReaderKey

internal class IpfsCacheProvider(
    private val ipfsReader: IpfsReader
) : CacheProvider {
    override fun get(key: String): ByteArray? {
        val readerKeyType = key.toReaderKey()
        return ipfsReader.read(readerKeyType)?.toByteArray()
    }

    override fun put(key: String, value: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun remove(key: String) {
        TODO("Not yet implemented")
    }
}