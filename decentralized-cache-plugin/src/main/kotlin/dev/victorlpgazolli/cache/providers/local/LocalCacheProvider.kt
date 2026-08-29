package dev.victorlpgazolli.cache.providers.local

import dev.victorlpgazolli.cache.model.CacheKeyType
import dev.victorlpgazolli.cache.model.CacheProvider
import dev.victorlpgazolli.ipfs.IpfsConnectedSession
import dev.victorlpgazolli.ipfs.IpfsWriter
import dev.victorlpgazolli.ipfs.addIpfsPrefix
import dev.victorlpgazolli.ipfs.removeIpfsPrefix
import dev.victorlpgazolli.utils.Logger
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

internal class LocalCacheProvider(
    private val ipfsConnectedSession: IpfsConnectedSession,
    private val ipfsWriter: IpfsWriter,
    private val logger: Logger,
) : CacheProvider {

    private val rootFolder = "/local-ipfs-gradle-cache"

    override fun get(key: String): ByteArray? =
        runCatching {
                val filepath = "$rootFolder/$key"

                ipfsConnectedSession.connection.callCmd("files/read?arg=$filepath").use {
                    responseBody ->
                    val bytes = responseBody.bytes()

                    val isFetchingManifest = key == "manifest.json"
                    if (isFetchingManifest) {
                        return bytes
                    }

                    // validating gzip magic number, if not valid, return null to indicate cache
                    // miss
                    if (bytes.size >= 2 && bytes[0] == 0x1F.toByte() && bytes[1] == 0x8B.toByte()) {
                        bytes
                    } else {
                        null
                    }
                }
            }
            .onFailure {
                logger.log("get", "Failed to get value for key: $key, error: ${it.message}")
            }
            .getOrNull()

    override fun put(key: CacheKeyType, value: ByteArray): CacheKeyType =
        runCatching {
                when (key) {
                    is CacheKeyType.Object -> {
                        val md5sum = key.value
                        val path = "/tmp/ipfs-cache-$md5sum.gz"
                        val compressedFile =
                            File(path).apply {
                                createNewFile()
                                deleteOnExit()
                            }
                        val outputStream = FileOutputStream(compressedFile)

                        GZIPOutputStream(outputStream).use { gzipOutputStream ->
                            gzipOutputStream.write(value)
                            gzipOutputStream.flush()
                        }

                        return CacheKeyType.FilePath(
                            fullpath = compressedFile.absolutePath,
                            md5sum = md5sum,
                            ipfsHash = null,
                        )
                    }
                    is CacheKeyType.FilePath -> {
                        val filename = key.md5sum.substringAfterLast("/")

                        runCatching {
                            ipfsConnectedSession.connection
                                .callCmd("files/mkdir?arg=$rootFolder")
                                .close()
                        }

                        val hash =
                            key.ipfsHash?.addIpfsPrefix() ?: ipfsWriter.write(File(key.fullpath))

                        val to = "$rootFolder/$filename"

                        copyFileFromIpfsToMfs(hash, to)

                        return key.copy(ipfsHash = hash.removeIpfsPrefix())
                    }
                }
            }
            .onFailure {
                logger.log("put", "Failed to put value for key: $key, error: ${it.message}")
            }
            .getOrThrow()

    override fun remove(key: String) {
        logger.log("remove", "Removing value for key: $key")
        ipfsConnectedSession.connection.callCmd("files/rm?arg=$rootFolder/$key").close()
    }

    private fun copyFileFromIpfsToMfs(ipfsHash: String, mfsPath: String) {
        val from = ipfsHash.addIpfsPrefix()
        logger.log("copyFileFromIpfsToMfs", "Copying file from IPFS ($from) to MFS ($mfsPath)")
        ipfsConnectedSession.connection.callCmd("files/cp?arg=$from&arg=$mfsPath").use {
            responseBody ->
            logger.log("copyFileFromIpfsToMfs", "Response from files/cp: ${responseBody.string()}")
        }
    }
}
