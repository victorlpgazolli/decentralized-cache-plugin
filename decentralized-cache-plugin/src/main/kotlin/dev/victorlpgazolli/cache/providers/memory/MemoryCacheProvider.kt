package dev.victorlpgazolli.cache.providers.memory

import dev.victorlpgazolli.cache.model.CacheProvider

class MemoryCacheProvider: CacheProvider {
    private val cache = mutableMapOf<String, ByteArray>()

    override fun get(key: String): ByteArray? {
        return cache[key]
    }

    override fun put(key: String, value: ByteArray) {
        cache[key] = value
    }

    override fun remove(key: String) {
        cache.remove(key)
    }
}