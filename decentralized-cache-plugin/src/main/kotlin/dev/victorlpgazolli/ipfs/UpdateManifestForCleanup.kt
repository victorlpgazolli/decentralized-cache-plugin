package dev.victorlpgazolli.ipfs

import dev.victorlpgazolli.cache.ManifestCacheHelper
import dev.victorlpgazolli.utils.Logger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.internal.cc.impl.models.BuildTreeModel.NullModel.result

fun interface UpdateManifestForCleanup {
    operator fun invoke()
}

@Serializable
data class FileEntry(
    @SerialName("Name") val name: String,
    @SerialName("Type") val type: Int,
    @SerialName("Size") val size: Long,
    @SerialName("Hash") val hash: String,
)

@Serializable data class FilesEntries(@SerialName("Entries") val entries: List<FileEntry>)

internal class UpdateManifestForCleanupImpl(
    private val ipfsConnectedSession: IpfsConnectedSession,
    private val manifestCacheHelper: ManifestCacheHelper,
    private val provideHashToNetworkUseCase: ProvideHashToNetworkUseCase,
    private val logger: Logger,
) : UpdateManifestForCleanup {
    override fun invoke() {
        logger.log("UpdateManifestForCleanup", "Updating manifest for cleanup")

        val allFiles =
            runCatching {
                    ipfsConnectedSession.connection
                        .callCmd("files/ls?arg=/local-ipfs-gradle-cache&long=true")
                        .use { responseBody ->
                            Json.decodeFromString<FilesEntries>(responseBody.string())
                        }
                }
                .getOrNull()

        val manifestHashes = allFiles?.entries?.associate { entry -> entry.name to entry.hash }

        val result = manifestCacheHelper.writeToManifest(manifestHashes ?: emptyMap())

        result?.ipfsHash?.let { manifestHash ->
            provideHashToNetworkUseCase(cacheKey = "manifest.json", ipfsHash = manifestHash)
        }
    }
}
