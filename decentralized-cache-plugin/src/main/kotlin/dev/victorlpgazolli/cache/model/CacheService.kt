package dev.victorlpgazolli.cache.model

interface CacheService : CacheProvider {
    val providers: List<CacheProvider>

    fun registerProvider(provider: CacheProvider)
}
