package dev.victorlpgazolli.client

import com.squareup.moshi.JsonAdapter
import dev.victorlpgazolli.DecentralizedConfiguration
import dev.victorlpgazolli.utils.Logger
import io.github.novacrypto.base58.Base58
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration
import io.ipfs.kotlin.IPFSConnection
import okhttp3.ResponseBody
import java.io.File
import java.io.InputStream

private const val LOG_TAG = "[decentralized-cache]"

internal class IpfsClient (
    val configuration: DecentralizedConfiguration,
    val cacheManifest: CacheManifest,
    val logger: Logger
) {

    val hostBaseUrl: String? = configuration.hostBaseUrl

    val client: IPFS by lazy {
        hostBaseUrl
            ?.let { IPFS(IPFSConfiguration(it)) }
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
    ): String{
        mfs.stat(filePath).let {
            it?.Hash?.let { hash ->
                logger.log(LOG_TAG, objectName, "object already exists in local cache: $hash")
                return hash
            }
        }

        logger.log(LOG_TAG, objectName, "adding from $filePath")

        val result = client.add.file(File(filePath))

        logger.log(LOG_TAG, objectName, "client.add.file - size: ${result.Hash}")

        logger.log(LOG_TAG, objectName, "client.pins.add ${result.Hash}")

        client.pins.add(result.Hash)

        logger.log(LOG_TAG, objectName, "done pinning")
        logger.log(LOG_TAG, objectName, "writing hash to MFS")
        val from = "/ipfs/${result.Hash}"
        val to = "/local-ipfs-gradle-cache/$objectName"
        val mfsResult: Boolean = mfs.copy(from, to).let {
            mfs.stat(to)?.Hash.isNullOrEmpty().not()
        }

        logger.log(LOG_TAG, objectName, "coping file from $from to $to = $mfsResult")
        logger.log(LOG_TAG, objectName, "announcing hash to network using routing provider")

        Routing(client.info.ipfs).provide(result.Hash)
        logger.log(LOG_TAG, objectName, "routing.provide completed")
        return result.Hash
    }

    private fun getIpfsHashFromObjectHash(objectHash: String): String? {

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
        val peerObjectHash: String? = Base58
            .base58Encode(objectHash.toByteArray())
            .let { cacheManifest.translateToIpfsHash(it) }

        logger.log(LOG_TAG, objectHash, "peerObjectHash: $peerObjectHash")
        peerObjectHash?.let {
            logger.log(LOG_TAG, objectHash, "found at peer: $it")
            return it
        } ?: logger.log(LOG_TAG, objectHash, "not found at peer")

        return null
    }

    fun getObject(
        objectName: String,
    ): InputStream? {
        logger.log(LOG_TAG, objectName, "searching for $objectName ipfs hash")

        val ipfsHash: String? = getIpfsHashFromObjectHash(objectName)

        if(ipfsHash == null) {
            logger.log(LOG_TAG, objectName, "ipfsHash is null")
            return null
        }
        logger.log(LOG_TAG, objectName, "$objectName translate to $ipfsHash")

        val hashPath = "/ipfs/$ipfsHash"

        return mfs.read(objectName)
            ?: client.get.catBytes(hashPath).let {
                mfs.copy(from = hashPath,to = "/local-ipfs-gradle-cache/$objectName")
                it.inputStream()
            }
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
