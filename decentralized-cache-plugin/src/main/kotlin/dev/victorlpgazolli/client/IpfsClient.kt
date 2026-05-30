package dev.victorlpgazolli.client

import com.squareup.moshi.JsonAdapter
import dev.victorlpgazolli.DecentralizedConfiguration
import dev.victorlpgazolli.utils.Logger
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration
import io.ipfs.kotlin.IPFSConnection
import okhttp3.ResponseBody
import java.io.File
import java.io.InputStream
import java.util.concurrent.CompletableFuture

private const val LOG_TAG = "[decentralized-cache]"

internal class IpfsClient (
    val configuration: DecentralizedConfiguration,
    val cacheManifest: CacheManifest,
    val logger: Logger
) {

    val hostBaseUrl: String? = configuration.hostBaseUrl

    val client: IPFS by lazy {
        hostBaseUrl
            ?.let { IPFS(IPFSConfiguration("$it/api/v0/")) }
            ?: IPFS(IPFSConfiguration())
    }
    val mfs: Mfs by lazy {
        Mfs(
            ipfs = client.info.ipfs,
            logger = logger
        )
    }
    init {
        cacheManifest.setup(this)
    }


    val version: String?
        get() = client.info.version()?.Version

    fun putObject(
        filePath: String,
        objectName: String,
    ): String {
        logger.log(LOG_TAG, objectName, "adding from $filePath")

        val result = client.add.file(File(filePath))

        logger.log(LOG_TAG, objectName, "client.add.file - size: ${result.Hash}")

        logger.log(LOG_TAG, objectName, "writing hash to MFS")
        val from = "/ipfs/${result.Hash}"
        val to = "/local-ipfs-gradle-cache/$objectName"
        val mfsResult: Boolean = mfs.copy(from, to).let {
            mfs.stat(to)?.Hash.isNullOrEmpty().not()
        }

        logger.log(LOG_TAG, objectName, "coping file from $from to $to = $mfsResult")
        logger.log(LOG_TAG, objectName, "announcing hash to network using routing provider")

        logger.log(LOG_TAG, objectName, "announcing hash to network in background...")

        runCatching {
            Routing(client.info.ipfs).provide(result.Hash)
            logger.log(LOG_TAG, objectName, "routing.provide completed")
        }
        return result.Hash
    }

    private fun getIpfsHashFromObjectHash(objectHash: String?): String? {
        objectHash ?: return null
        val localPath = "/local-ipfs-gradle-cache/$objectHash"
        val localObjectHash: String? = mfs
            .stat(localPath)
            ?.Hash
        logger.log(LOG_TAG, objectHash, "searching MFS")
        localObjectHash?.let {
            logger.log(LOG_TAG, objectHash, "found at MFS: $it")
            return it
        } ?: logger.log(LOG_TAG, objectHash, "not found at MFS")

        logger.log(LOG_TAG, objectHash, "searching peers")
        val peerObjectHash = cacheManifest.translateToIpfsHash(objectHash)

        logger.log(LOG_TAG, objectHash, "peerObjectHash: $peerObjectHash")
        peerObjectHash?.let {
            logger.log(LOG_TAG, objectHash, "found at peer: $it")
            return it
        } ?: logger.log(LOG_TAG, objectHash, "not found at peer")

        return null
    }

    fun getObject(
        objectName: String,
        basePath: String = "/ipfs",
    ): InputStream? {
        logger.log(LOG_TAG, objectName, "Fetching object: $objectName")

        // SCENARIO 1: It is an absolute network path (e.g., fetching a manifest from a peer)
        if (objectName.startsWith("/ipns/") || objectName.startsWith("/ipfs/")) {
            return runCatching {
                var targetPath = objectName
                val baseUrl = System.getenv("IPFS_NODE_URL") ?: "http://127.0.0.1:5001"

                // 1. Explicit IPNS resolution via POST
                if (objectName.startsWith("/ipns/")) {
                    val ipnsKey = objectName.removePrefix("/ipns/")

                    logger.log(LOG_TAG, objectName, "Resolving IPNS explicitly on API...")
                    val resolveUrl = java.net.URL("$baseUrl/api/v0/name/resolve?arg=$ipnsKey")
                    val connection = resolveUrl.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "POST"

                    connection.connectTimeout = 5000
                    connection.readTimeout = 10000

                    if (connection.responseCode in 200..299) {
                        val jsonResponse = connection.inputStream.bufferedReader().readText()
                        val resolvedPath = "\"Path\":\"(.*?)\"".toRegex().find(jsonResponse)?.groups?.get(1)?.value

                        if (resolvedPath != null) {
                            logger.log(LOG_TAG, objectName, "IPNS translated to: $resolvedPath")
                            targetPath = resolvedPath
                        }
                    } else {
                        logger.log(LOG_TAG, objectName, "HTTP error resolving IPNS: ${connection.responseCode}")
                        return@runCatching null
                    }
                }

                logger.log(LOG_TAG, objectName, "Downloading data from network via native API: $targetPath")
                val catUrl = java.net.URL("$baseUrl/api/v0/cat?arg=$targetPath")
                val catConnection = catUrl.openConnection() as java.net.HttpURLConnection
                catConnection.requestMethod = "POST"
                catConnection.connectTimeout = 5000

                catConnection.readTimeout = 10000

                if (catConnection.responseCode in 200..299) {
                    val bytes = catConnection.inputStream.readBytes()

                    if (bytes.isEmpty()) {
                        logger.log(LOG_TAG, objectName, "Warning: The API returned 0 bytes for $targetPath.")
                        return@runCatching null
                    }

                    return@runCatching bytes.inputStream()
                } else {
                    logger.log(LOG_TAG, objectName, "HTTP error in cat: ${catConnection.responseCode}")
                    return@runCatching null
                }
            }.onFailure {
                logger.log(LOG_TAG, objectName, "Exception while fetching from network: ${it.message}")
            }.getOrNull()
        }

        // SCENARIO 2: It is a Gradle Build Cache key (e.g., 3b9db69fb8...)
        val ipfsHash = getIpfsHashFromObjectHash(objectName)
        if (ipfsHash == null) {
            logger.log(LOG_TAG, objectName, "ipfsHash not found for $objectName")
            return null
        }

        val hashPath = "$basePath/$ipfsHash"
        val baseUrl = System.getenv("IPFS_NODE_URL") ?: "http://127.0.0.1:5001"

        // 1. Attempts to download the binary GZIP via RPC API (Ensures raw bytes)
        return runCatching {
            logger.log(LOG_TAG, objectName, "Downloading binary GZIP via POST RPC API: $hashPath")

            // We use the API CAT endpoint via POST to avoid gateway issues
            val catUrl = java.net.URL("$baseUrl/api/v0/cat?arg=$hashPath")
            val catConnection = catUrl.openConnection() as java.net.HttpURLConnection
            catConnection.requestMethod = "POST"
            catConnection.connectTimeout = 5000
            // Gives the daemon more time to download blocks from the P2P network
            catConnection.readTimeout = 30000

            if (catConnection.responseCode in 200..299) {
                // (Optional) Copy to MFS in background so next access is local
                CompletableFuture.runAsync {
                    runCatching {
                        mfs.copy(from = hashPath, to = "/local-ipfs-gradle-cache/$objectName")
                    }
                }

                return@runCatching catConnection.inputStream.buffered()
            } else {
                logger.log(LOG_TAG, objectName, "HTTP error in cat: ${catConnection.responseCode}")
                return@runCatching null
            }
        }.onFailure {
            logger.log(LOG_TAG, objectName, "Exception while downloading GZIP from P2P network: ${it.message}")
        }.getOrNull()
    }

}

data class Hash(val Hash: String)
class Mfs(val ipfs: IPFSConnection, val logger: Logger) {

    init {
        createCacheIfNotExists()
    }

    private fun createCacheIfNotExists() {
        val localIpfsCache = "/local-ipfs-gradle-cache"
        logger.log(LOG_TAG, "mfs", "creating $localIpfsCache")
        ipfs.callCmd("files/mkdir?arg=$localIpfsCache").use {
            logger.log(LOG_TAG, "mfs", "$localIpfsCache created")
        }
    }
    private val adapter: JsonAdapter<Hash> by lazy {
        ipfs.config.moshi.adapter(Hash::class.java)
    }

    fun stat(path: String): Hash? = runCatching {
        ipfs.callCmd("files/stat?arg=$path&hash=true").use { responseBody ->
            adapter.fromJson(responseBody.charStream().readText())
        }
    }.getOrNull()

    fun copy(from: String, to: String): String? {
        runCatching {
            ipfs.callCmd("files/cp?arg=$from&arg=$to")
                .use(ResponseBody::string)?.let { return it }
        }.onFailure {
            logger.log(LOG_TAG, "copy", " failed to copy $from to $to")
        }.onSuccess {
            logger.log(LOG_TAG, "copy", " copied $from to $to")
        }
        return null
    }

    fun read(objectHash: String): InputStream? {
        val path = "/local-ipfs-gradle-cache/$objectHash"

        stat(path) ?: return null

        return ipfs
            .callCmd("files/read?arg=$path")
            .byteStream()
    }

    fun delete(path: String) {
        runCatching {
            ipfs.callCmd("files/rm?arg=$path").use {
                logger.log(LOG_TAG, "delete", " deleted $path")
            }
        }.onFailure {
            logger.log(LOG_TAG, "delete", " failed to delete $path")
        }
    }

}

class Routing(val ipfs: IPFSConnection) {

    fun provide(hash: String): String = ipfs.callCmd("routing/provide?arg=$hash")
        .use(ResponseBody::string)

}