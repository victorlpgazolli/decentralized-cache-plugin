package dev.victorlpgazolli.cache.providers.remote

import dev.victorlpgazolli.cache.ManifestCacheHelper
import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.ipfs.IpfsConnectedSession
import dev.victorlpgazolli.ipfs.IpfsReader
import dev.victorlpgazolli.utils.Logger

internal class RemoteCacheProvider(
    private val ipfsReader: IpfsReader,
    private val ipfsConnectedSession: IpfsConnectedSession,
    private val logger: Logger,
    private val manifestCacheHelper: ManifestCacheHelper,
) : CacheProvider by IpfsCacheProvider(
    ipfsReader = ipfsReader,
    ipfsConnectedSession = ipfsConnectedSession,
    logger = logger,
    manifestCacheHelper = manifestCacheHelper,
)