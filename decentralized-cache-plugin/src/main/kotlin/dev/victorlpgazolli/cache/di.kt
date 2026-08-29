package dev.victorlpgazolli.cache

import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.cache.model.CacheService
import dev.victorlpgazolli.cache.providers.local.LocalCacheProvider
import dev.victorlpgazolli.cache.providers.memory.MemoryCacheProvider
import dev.victorlpgazolli.cache.providers.remote.RemoteCacheProvider
import org.gradle.internal.cc.base.logger
import org.kodein.di.bindProvider
import org.kodein.di.bindSet
import org.kodein.di.bindSingleton
import org.kodein.di.instance

internal const val MEMORY_CACHE_PROVIDER_TAG = "memory"
internal const val LOCAL_CACHE_PROVIDER_TAG = "local"
internal const val REMOTE_CACHE_PROVIDER_TAG = "remote"

val cacheDiModule = org.kodein.di.DI.Module("cache") {
    bindSet<CacheProvider> {
        bindSingleton(MEMORY_CACHE_PROVIDER_TAG) {
            MemoryCacheProvider()
        }
        bindSingleton(LOCAL_CACHE_PROVIDER_TAG) {
            LocalCacheProvider(
                ipfsConnectedSession = instance(),
                ipfsWriter = instance(),
                logger = instance(),
            )
        }
        bindSingleton(REMOTE_CACHE_PROVIDER_TAG) {
            RemoteCacheProvider(
                ipfsReader = instance(),
                ipfsConnectedSession = instance(),
                logger = instance(),
                manifestCacheHelper = instance(),
            )
        }
    }
    bindProvider<ManifestCacheHelper> {
        ManifestCacheHelper(
            configurationHolder = instance(),
            ipfsConnectedSession = instance(),
            localCacheProvider = instance<CacheProvider>(LOCAL_CACHE_PROVIDER_TAG),
            logger = instance(),
        )
    }
}