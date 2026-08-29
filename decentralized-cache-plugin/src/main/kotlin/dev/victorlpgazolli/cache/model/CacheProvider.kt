package dev.victorlpgazolli.cache.model

sealed class CacheKeyType(val value: String) {
    data class Object(val name: String) : CacheKeyType(value = name)

    data class FilePath(val fullpath: String, val md5sum: String, val ipfsHash: String?) :
        CacheKeyType(value = fullpath)
}

interface CacheProvider {
    fun get(key: String): ByteArray?

    fun put(key: CacheKeyType, value: ByteArray): CacheKeyType

    fun remove(key: String)
}
