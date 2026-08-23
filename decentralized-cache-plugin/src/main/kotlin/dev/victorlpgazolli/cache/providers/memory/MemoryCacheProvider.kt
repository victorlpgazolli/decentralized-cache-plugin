package dev.victorlpgazolli.cache.providers.memory

import dev.victorlpgazolli.cache.model.CacheKeyType
import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.utils.Logger

class MemoryCacheProvider(
    private val logger: Logger,
): CacheProvider {
    private val cache = mutableMapOf<String, ByteArray>()

    override fun get(key: String): ByteArray? {
        return cache[key]
    }

    override fun put(key: CacheKeyType, value: ByteArray): CacheKeyType {
        cache[key.value] = value
        return key
    }

    override fun remove(key: String) {
        cache.remove(key)
    }
}