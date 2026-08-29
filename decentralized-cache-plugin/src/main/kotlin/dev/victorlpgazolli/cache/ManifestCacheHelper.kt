package dev.victorlpgazolli.cache

import dev.victorlpgazolli.DecentralizedConfigurationHolder
import dev.victorlpgazolli.cache.model.CacheKeyType
import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.ipfs.IpfsConnectedSession
import dev.victorlpgazolli.ipfs.catText
import dev.victorlpgazolli.ipfs.getPeers
import dev.victorlpgazolli.ipfs.json
import dev.victorlpgazolli.ipfs.nameResolve
import dev.victorlpgazolli.utils.Logger
import java.io.File
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

internal interface ManifestCacheHelper {

    fun fetchCacheEntries(): Map<String, String>

    fun writeToManifest(cacheEntries: Map<String, String>): CacheKeyType.FilePath?
}

internal fun ManifestCacheHelper(
    configurationHolder: DecentralizedConfigurationHolder,
    ipfsConnectedSession: IpfsConnectedSession,
    localCacheProvider: CacheProvider,
    logger: Logger,
): ManifestCacheHelper {
    return ManifestCacheHelperImpl(
        configurationHolder = configurationHolder,
        ipfsConnectedSession = ipfsConnectedSession,
        localCacheProvider = localCacheProvider,
        logger = logger,
    )
}

@Serializable data class ManifestFromPeer(@SerialName("Path") val path: String)

object CacheManifestResultSerializer :
    JsonContentPolymorphicSerializer<CacheManifestResult>(CacheManifestResult::class) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<CacheManifestResult> {
        val jsonObject = element.jsonObject
        return when {
            "Message" in jsonObject -> CacheManifestResult.Failure.serializer()
            else -> CacheManifestResult.Success.serializer()
        }
    }
}

@Serializable(with = CacheManifestResultSerializer::class)
sealed class CacheManifestResult {

    @Serializable
    data class Failure(
        @SerialName("Message") val message: String,
        @SerialName("Code") val code: Int,
        @SerialName("Type") val type: String,
    ) : CacheManifestResult()

    @Serializable
    data class Success(val publishKeyName: String, val hashs: Map<String, String>) :
        CacheManifestResult()
}

private class ManifestCacheHelperImpl(
    private val configurationHolder: DecentralizedConfigurationHolder,
    private val ipfsConnectedSession: IpfsConnectedSession,
    private val localCacheProvider: CacheProvider,
    private val logger: Logger,
) : ManifestCacheHelper {

    override fun fetchCacheEntries(): Map<String, String> {
        val peersIds = ipfsConnectedSession.getPeers()

        val configDefinedPeerIds = configurationHolder.configuration.peerIpnsList

        val peers = peersIds + configDefinedPeerIds

        val merged =
            peers.take(2).fold(emptyMap<String, String>()) { acc, peerIpns ->
                val remoteManifest = fetchManifestFromPeer(from = peerIpns)

                return@fold acc + remoteManifest
            }
        logger.log(
            "fetchCacheEntries",
            "Fetched cache entries from peers (${peers.size}): ${merged.keys}",
        )
        return merged
    }

    override fun writeToManifest(cacheEntries: Map<String, String>): CacheKeyType.FilePath? {
        val manifest =
            runCatching {
                    Json.decodeFromString<CacheManifestResult.Success>(
                        localCacheProvider.get("manifest.json").contentToString()
                    )
                }
                .getOrNull()

        val newManifest =
            CacheManifestResult.Success(
                publishKeyName = manifest?.publishKeyName ?: "self",
                hashs = cacheEntries,
            )

        val manifestFile =
            File("/tmp/manifest.json").apply {
                createNewFile()
                writeText(Json.encodeToString(newManifest))
            }

        localCacheProvider.remove("manifest.json")

        val result =
            localCacheProvider.put(
                CacheKeyType.FilePath(
                    fullpath = manifestFile.absolutePath,
                    md5sum = manifestFile.absolutePath,
                    ipfsHash = null,
                ),
                Json.encodeToString(newManifest).toByteArray(),
            )
        return result as? CacheKeyType.FilePath
    }

    fun fetchManifestFromPeer(from: String): Map<String, String> {
        val manifestContent =
            runCatching {
                    val manifestIpfsPath =
                        ipfsConnectedSession.nameResolve<ManifestFromPeer>(from).path

                    ipfsConnectedSession.catText(manifestIpfsPath)
                }
                .onFailure {
                    logger.log(
                        "fetchManifest",
                        "Peer $from failed to provide a manifest (IPNS unresolvable or timeout): ${it.message}",
                    )
                }
                .getOrDefault("")

        return manifestContent.decodeManifest()
    }

    private fun String.decodeManifest(): Map<String, String> {
        if (isBlank()) {
            return emptyMap()
        }
        runCatching {
                return when (
                    val manifestResult = json.decodeFromString<CacheManifestResult>(this)
                ) {
                    is CacheManifestResult.Failure -> {
                        emptyMap()
                    }
                    is CacheManifestResult.Success -> {
                        manifestResult.hashs
                    }
                }
            }
            .onFailure {
                logger.log(
                    "decodeManifest",
                    "failed to decode manifest $this; cause -> ${it.message}",
                )
            }
            .onSuccess { logger.log("decodeManifest", "decoded manifest: $it") }
        return emptyMap()
    }
}
