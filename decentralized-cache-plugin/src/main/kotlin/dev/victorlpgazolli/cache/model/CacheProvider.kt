package dev.victorlpgazolli.cache.model

interface CacheProvider {
    fun get(key: String): ByteArray?
    fun put(key: String, value: ByteArray)
    fun remove(key: String)
}