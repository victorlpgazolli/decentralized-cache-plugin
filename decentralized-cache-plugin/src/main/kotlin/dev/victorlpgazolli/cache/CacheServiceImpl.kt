package dev.victorlpgazolli.cache

import dev.victorlpgazolli.DecentralizedConfiguration
import dev.victorlpgazolli.DecentralizedConfigurationHolder
import dev.victorlpgazolli.appDiModule
import dev.victorlpgazolli.cache.model.CacheKeyType
import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.ipfs.ProvideHashToNetworkUseCase
import dev.victorlpgazolli.utils.Logger
import org.gradle.caching.BuildCacheEntryReader
import org.gradle.caching.BuildCacheEntryWriter
import org.gradle.caching.BuildCacheKey
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance
import javax.inject.Inject




internal class CacheServiceImpl(
    override var di: DI
) : DIAware,
    BuildCacheServiceFactory<DecentralizedConfiguration>,
    BuildCacheService {


    private val logger by instance<Logger>()

    val providers: List<CacheProvider>
        get() {
            return listOf(
                direct.instance<CacheProvider>(tag = MEMORY_CACHE_PROVIDER_TAG),
                direct.instance<CacheProvider>(tag = LOCAL_CACHE_PROVIDER_TAG),
                direct.instance<CacheProvider>(tag = REMOTE_CACHE_PROVIDER_TAG),
            )
        }

    @Inject
    constructor() : this(di = appDiModule)

    override fun createBuildCacheService(
        configuration: DecentralizedConfiguration,
        describer: BuildCacheServiceFactory.Describer
    ): BuildCacheService {
        val logger by di.instance<Logger>()
        logger.log("createBuildCacheService", "Creating build cache service with configuration: $configuration")
        val configHolder by di.instance<DecentralizedConfigurationHolder>()
        configHolder.configuration = configuration
        return this
    }

    override fun load(
        cacheKey: BuildCacheKey,
        cacheEntryReader: BuildCacheEntryReader,
    ): Boolean = get(cacheKey.hashCode)
        ?.inputStream()
        ?.let {
            logger.log("load", "Loading cache entry for key: ${cacheKey.hashCode}")
            cacheEntryReader.readFrom(it)
            logger.log("load", "Successfully loaded cache entry for key: ${cacheKey.hashCode}")
            true
        }
        ?: false


    override fun store(
        cacheKey: BuildCacheKey,
        cacheEntryWriter: BuildCacheEntryWriter,
    ) {
        logger.log("store", "Storing cache entry for key: ${cacheKey.hashCode}")
        put(
            key = CacheKeyType.Object(
                name = cacheKey.hashCode,
            ),
            value = cacheEntryWriter.inputStream.readAllBytes()
        )
        logger.log("store", "Successfully stored cache entry for key: ${cacheKey.hashCode}")
    }

    override fun close() {
        logger.log("close", "Closing cache service")
    }


    fun get(key: String): ByteArray? {
        logger.log("get", "Getting value for key: $key with providers=${providers.map { it::class.simpleName }}")
        for (provider in providers) {
            logger.log(provider::class.simpleName ?: "Unknown", "getting $key content from provider")
            val value = provider.get(key)
            if (value != null) {
                return value
            }
            logger.log(provider::class.simpleName ?: "Unknown", "key not found")
        }
        return null
    }

    fun put(key: CacheKeyType, value: ByteArray) {
        val result = providers.fold(key) { acc, provider ->
            logger.log(provider::class.simpleName ?: "Unknown", "putting key=$acc content to provider")
            provider.put(acc, value)
        }
        val filepath = result as? CacheKeyType.FilePath

        val hash = filepath?.ipfsHash ?: return

        logger.log("put", "providing $key to network with hash=$hash")
        val provideHashToNetworkUseCase = di.direct.instance<ProvideHashToNetworkUseCase>()

        provideHashToNetworkUseCase(hash)
    }

    fun remove(key: String) {
        for (provider in providers) {
            provider.remove(key)
        }
    }


}