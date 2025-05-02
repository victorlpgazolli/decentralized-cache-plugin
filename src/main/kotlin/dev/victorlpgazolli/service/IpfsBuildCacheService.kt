package dev.victorlpgazolli.service

import dev.victorlpgazolli.client.IpfsClient
import org.gradle.caching.BuildCacheEntryReader
import org.gradle.caching.BuildCacheEntryWriter
import org.gradle.caching.BuildCacheException
import org.gradle.caching.BuildCacheKey
import org.gradle.caching.BuildCacheService
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream

private val LOG_TAG = "[decentralized-cache]"
fun extractTar(tarFile: File, outputDir: File) {
    val blockSize = 512
    FileInputStream(tarFile).use { input ->
        val buffer = ByteArray(blockSize)
        while (true) {
            val read = input.readNBytes(buffer, 0, blockSize)
            if (read < blockSize || buffer.all { it == 0.toByte() }) break // EOF

            val name = buffer.copyOfRange(0, 100).toString(StandardCharsets.UTF_8).trim('\u0000', ' ')
            val sizeOctal = buffer.copyOfRange(124, 136).toString(StandardCharsets.UTF_8).trim('\u0000', ' ')
            val size = sizeOctal.toIntOrNull(8) ?: 0

            if (name.isBlank() || size == 0) {
                // Skip empty/invalid entries
                input.skip((size + 511) / 512 * 512L)
                continue
            }

            val outFile = File(outputDir, name)
            outFile.parentFile?.mkdirs()

            FileOutputStream(outFile).use { out ->
                val data = input.readNBytes(size)
                out.write(data)
            }

            // Skip padding to next 512-byte block
            val padding = (512 - (size % 512)) % 512
            if (padding > 0) input.skip(padding.toLong())
        }
    }
}

internal class IpfsBuildCacheService(
    private val ipfsClient: IpfsClient,
) : BuildCacheService {
    override fun store(
        cacheKey: BuildCacheKey,
        cacheEntryWriter: BuildCacheEntryWriter,
    ) {
        println(
            "$LOG_TAG Storing cache entry with key: ${cacheKey.hashCode}"
        )
        val compressedFilePath = File("/tmp/ipfs-cache-${cacheKey.hashCode}.tar.gz")
        val outputStream = FileOutputStream(compressedFilePath)

        cacheEntryWriter.writeTo(outputStream).also {
            outputStream.flush()
            outputStream.close()
        }

        val decompressedFile = File("/tmp/ipfs-cache-${cacheKey.hashCode}")
        decompressedFile.mkdirs()

        val tarBytes = GZIPInputStream(FileInputStream(compressedFilePath)).readAllBytes()
        val tarFile = File("/tmp/ipfs-cache-${cacheKey.hashCode}.tar")
        FileOutputStream(tarFile).use { it.write(tarBytes) }

        extractTar(tarFile, decompressedFile)

        runCatching {
            ipfsClient.putObject(
                directoryPath = decompressedFile.absolutePath,
                objectName = cacheKey.hashCode.toString(),
            )

        }.onSuccess {
            println(
                "$LOG_TAG Successfully stored cache entry with key: ${cacheKey.hashCode} at ${decompressedFile.absolutePath}"
            )
        }.onFailure { exception ->
            println(
                "$LOG_TAG Failed to store cache entry with key: ${cacheKey.hashCode}, error: ${exception.message}"
            )
        }

        compressedFilePath.delete()
        decompressedFile.delete()
        tarFile.delete()
    }

    override fun load(
        cacheKey: BuildCacheKey,
        cacheEntryReader: BuildCacheEntryReader,
    ): Boolean {
        println(
            "$LOG_TAG Loading cache entry with key: ${cacheKey.hashCode}"
        )
        runCatching {
            val inputStream = ipfsClient.getObject(cacheKey.hashCode)

            val byteArray = ByteArrayOutputStream()
                .apply { inputStream.copyTo(this) }
                .toByteArray()

            if(byteArray.isEmpty()) {
                val message = "not found"
                throw BuildCacheException(message)
            }
            println("$LOG_TAG Key ${cacheKey.hashCode} has size ${byteArray.size} bytes")
            println("$LOG_TAG Key ${cacheKey.hashCode} has value: $byteArray")

            cacheEntryReader.readFrom(inputStream).also {
                inputStream.close()
            }

            println("$LOG_TAG Successfully loaded cache entry with key: ${cacheKey.hashCode}")
        }.onFailure {
            val message = "$LOG_TAG Failed to load cache entry with key: ${cacheKey.hashCode}, error: ${it.message}"
            println(message)
            return false
        }
        return true
    }

    override fun close() {}
}