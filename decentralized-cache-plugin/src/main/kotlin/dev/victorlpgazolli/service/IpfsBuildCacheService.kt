package dev.victorlpgazolli.service

import dev.victorlpgazolli.client.IpfsClient
import dev.victorlpgazolli.utils.Logger
import org.gradle.caching.BuildCacheEntryReader
import org.gradle.caching.BuildCacheEntryWriter
import org.gradle.caching.BuildCacheKey
import org.gradle.caching.BuildCacheService
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private const val LOG_TAG = "[decentralized-cache]"
internal class IpfsBuildCacheService(
    private val ipfsClient: IpfsClient,
    private val logger: Logger,
) : BuildCacheService {
    override fun store(
        cacheKey: BuildCacheKey,
        cacheEntryWriter: BuildCacheEntryWriter,
    ) {
        val key = cacheKey.toString()
        val path = "/tmp/ipfs-cache-$key.gz"
        logger.log(LOG_TAG, key, "Storing cache entry at $path")

        val compressedFile = File(path).apply {
            createNewFile()
            deleteOnExit()
        }

        logger.log(LOG_TAG, key, "Writing to $path")
        GZIPOutputStream(FileOutputStream(compressedFile)).use { gzipOutputStream ->
            cacheEntryWriter.writeTo(gzipOutputStream)
            gzipOutputStream.flush()
        }


        runCatching {
            ipfsClient.putObject(
                filePath = compressedFile.absolutePath,
                objectName = key,
            )
        }.onSuccess { generatedHash ->
            ipfsClient.cacheManifest.addCacheEntry(key, generatedHash)
            logger.log(
                LOG_TAG,
                key,
                "Successfully stored cache at ${compressedFile.absolutePath}"
            )
        }.onFailure { exception ->
            logger.log(
                LOG_TAG,
                key,
                "Failed to store cache, error: ${exception.message}"
            )
        }
    }
    override fun load(
        cacheKey: BuildCacheKey,
        cacheEntryReader: BuildCacheEntryReader,
    ): Boolean {
        val key = cacheKey.toString()
        logger.log(LOG_TAG, key, "Loading cache entry")
        val inputStream = ipfsClient.getObject(key)

        if (inputStream == null) {
            logger.log(LOG_TAG, key, "Cache entry not found")
            return false
        }

        GZIPInputStream(inputStream).use { gis ->
            cacheEntryReader.readFrom(gis)
        }
        logger.log(LOG_TAG, key, "cache LOADED")
        return true
    }


    override fun close() {
        logger.log(LOG_TAG, "close", "Gradle build finished. Closing cache service...")

        ipfsClient.cacheManifest.flush()
    }
}