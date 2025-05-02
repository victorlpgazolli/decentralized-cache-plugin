package dev.victorlpgazolli.client

import com.squareup.moshi.JsonAdapter
import io.github.novacrypto.base58.Base58
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration
import io.ipfs.kotlin.IPFSConnection
import okhttp3.ResponseBody
import java.io.File
import java.io.InputStream


val LOG_TAG = "[decentralized-cache]"
internal class IpfsClient (
//    configuration: IPFSConfig
) {

//    val hostBaseUrl = configuration.hostBaseUrl
//    val publishKeyName = configuration.publishKeyName
//    val baseIpns = configuration.baseIpns

    private val client = IPFS(
        IPFSConfiguration()
    )

    val version: String?
        get() = client.info.version()?.Version

    fun putObject(
        directoryPath: String,
        objectName: String,
    ) {

        val base58Hash = Base58.base58Encode(objectName.toByteArray())

        println("$LOG_TAG adding $objectName ($base58Hash) from $directoryPath")

        val result = client.add.directory(File(directoryPath), objectName)

        println("$LOG_TAG client.add.directory - size: ${result.size} ${result}")
        val directory = result.last()

        println("$LOG_TAG client.pins.add ${directory.Hash}")

        client.pins.add(directory.Hash)


        println("$LOG_TAG done pinning")
        println("$LOG_TAG writing hash to MFS")
        val mfsResult = Mfs(client.info.ipfs)
            .copy(
                "/ipfs/${directory.Hash}",
                "/local-ipfs-gradle-cache/$objectName",
            )

        println("$LOG_TAG mfsResult: $mfsResult")
        println("$LOG_TAG announcing hash to network using routing provider")

        val announcement = Routing(client.info.ipfs).provide(directory.Hash)
        println("$LOG_TAG routing.provide ${announcement}")
    }

    fun getObject(
        objectName: String,
    ): InputStream {


        println("$LOG_TAG fetching $objectName hash from MFS")

        val localObjectHash: String? = Mfs(client.info.ipfs)
            .stat("/local-ipfs-gradle-cache/$objectName")
            ?.Hash

        if(localObjectHash == null) {
            println("$LOG_TAG localObjectHash is null")
            return InputStream.nullInputStream()
        }

        println("$LOG_TAG localObjectHash: $localObjectHash")

        val result = client.get.cat("/ipfs/$localObjectHash")

        println("$LOG_TAG client.get.cat /ipfs/$localObjectHash ${result.length} $result")

        val hasStrangeLength = result.length < 50
        val hasInvalidResult = result.toString().contains("405 - Method Not Allowed")
        if(hasStrangeLength && hasInvalidResult) {
            return InputStream.nullInputStream()
        }

        return result
            .byteInputStream()
    }
}

data class Hash(val Hash: String)
class Mfs(val ipfs: IPFSConnection) {

    private val adapter: JsonAdapter<Hash> by lazy {
        ipfs.config.moshi.adapter(Hash::class.java)
    }

    fun stat(path: String): Hash = ipfs.callCmd("files/stat?arg=$path&hash=true").use { responseBody ->
        return adapter.fromJson(responseBody.charStream().readText())
    }

    fun copy(from: String, to: String): String {
        ipfs.callCmd("files/rm?arg=$from&recursive=true&force=true")
        return ipfs.callCmd("files/cp?arg=$from&arg=$to&parents=true").use(ResponseBody::string)
    }

}


class Routing(val ipfs: IPFSConnection) {

    fun provide(hash: String): String = ipfs.callCmd("routing/provide?arg=$hash").use(ResponseBody::string)

}
