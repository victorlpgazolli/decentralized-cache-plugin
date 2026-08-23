package dev.victorlpgazolli.cache.providers.remote

import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.ipfs.IpfsConnectedSession
import dev.victorlpgazolli.ipfs.IpfsReader

internal class RemoteCacheProvider(
    private val ipfsReader: IpfsReader
) : CacheProvider by IpfsCacheProvider(ipfsReader)