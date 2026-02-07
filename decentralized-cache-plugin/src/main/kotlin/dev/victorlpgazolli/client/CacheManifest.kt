@file:OptIn(ExperimentalSerializationApi::class)

package dev.victorlpgazolli.client

import dev.victorlpgazolli.utils.Logger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File


@Serializable
data class Manifest(
    val publishKeyName: String,
    val hashs: Map<String, String>,
)

internal class CacheManifest(
    val logger: Logger
){

    public fun setup(client: IpfsClient) {
        this.client = client
        this.manifest = fetch()
    }

    private lateinit var client: IpfsClient
    private lateinit var manifest: Manifest

    private val manifestFileName = "manifest.json"
    private val emptyManifest = Manifest(
        publishKeyName = "cache",
        hashs = emptyMap(),
    )

    public fun translateToIpfsHash(objectName: String): String? {
        return if(this::manifest.isInitialized) { manifest.hashs[objectName] }
        else { null }
    }
    private fun fetch(): Manifest {
        val manifestIpnsPath = "${client.baseIpns}/$manifestFileName"

        logger.log(LOG_TAG, "fetch", "fetching manifest at $manifestIpnsPath")
        val localManifest: Manifest? = client.mfs.read(manifestFileName)?.let { decodeManifest(it.readAllBytes().decodeToString()) }

        if(localManifest != null) {
            logger.log(LOG_TAG, "fetch", "local manifest: $localManifest")
            return localManifest
        }

        val remoteManifest: Manifest? = client.getObject(manifestIpnsPath).let { inputStream ->
            val result: String = inputStream?.readAllBytes()?.decodeToString() ?: ""
            if (result.isEmpty()) { return@let null }
            logger.log(LOG_TAG, "fetch", "manifest: $result")
            return decodeManifest(result)
        }

        if (remoteManifest != null) {
            logger.log(LOG_TAG, "fetch", "remote manifest is null")
            return remoteManifest
        }

        return saveEmptyManifest()
    }

    private fun saveEmptyManifest(): Manifest {
        logger.log(LOG_TAG, "saveEmptyManifest", "manifest is empty")



        val tmpManifestPath = "/tmp/local-ipfs-gradle-cache"

        File(tmpManifestPath).run {
            delete()
            mkdir()
        }
        val newManifestFile = File("$tmpManifestPath/$manifestFileName")

        newManifestFile.createNewFile()
        newManifestFile.writeText(emptyManifest.encodeManifest())
        logger.log(LOG_TAG, "saveEmptyManifest", "created empty manifest at $tmpManifestPath")

        client.putObject(
            filePath = newManifestFile.absolutePath,
            objectName = manifestFileName,
        )

        return emptyManifest
    }

    private fun decodeManifest(manifest: String): Manifest {
        runCatching {
            Json.decodeFromString<Manifest>(manifest)?.let {
                return it
            }
        }.onFailure {
            logger.log(LOG_TAG, "decodeManifest", "failed to decode manifest")
            println(it.message)
        }.onSuccess {
            logger.log(LOG_TAG, "decodeManifest", "decoded manifest: $it")
        }
        return emptyManifest
    }
    private fun Manifest.encodeManifest(): String {
        return Json.encodeToString(Manifest.serializer(), this)
    }

}
