package dev.victorlpgazolli.cache

import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.cache.model.CacheService

class CacheServiceImpl: CacheService {
    override var providers: List<CacheProvider> = emptyList()

    override fun registerProvider(provider: CacheProvider) {
        providers += provider
    }

    override fun get(key: String): ByteArray? {
        for (provider in providers) {
            val value = provider.get(key)
            if (value != null) {
                return value
            }
        }
        return null
    }

    override fun put(key: String, value: ByteArray) {
        for (provider in providers) {
            provider.put(key, value)
        }
    }

    override fun remove(key: String) {
        for (provider in providers) {
            provider.remove(key)
        }
    }
}