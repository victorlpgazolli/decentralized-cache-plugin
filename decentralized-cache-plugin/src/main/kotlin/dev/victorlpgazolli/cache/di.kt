package dev.victorlpgazolli.cache

import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.cache.model.CacheService
import dev.victorlpgazolli.cache.providers.local.LocalCacheProvider
import dev.victorlpgazolli.cache.providers.memory.MemoryCacheProvider
import dev.victorlpgazolli.cache.providers.remote.RemoteCacheProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val cacheDiModule = org.kodein.di.DI.Module("cache") {
    bindSingleton<CacheService> {
        CacheServiceImpl().apply {
            registerProvider(
                MemoryCacheProvider()
            )
            registerProvider(
                LocalCacheProvider(
                    ipfsConnectedSession = instance()
                )
            )
            registerProvider(
                RemoteCacheProvider(
                    ipfsReader = instance()
                )
            )
        }
    }
}