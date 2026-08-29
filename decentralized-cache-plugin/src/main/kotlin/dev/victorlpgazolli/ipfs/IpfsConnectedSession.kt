package dev.victorlpgazolli.ipfs

import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConnection

data class IpfsConnectedSession(val baseUrl: String, val client: IPFS) {
    val connection: IPFSConnection
        get() = client.info.ipfs
}
