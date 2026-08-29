package dev.victorlpgazolli.ipfs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

val json = Json {
    ignoreUnknownKeys = true
}

@Serializable
data class SwarmPeersResponse(
    @SerialName("Peers")
    val peers: List<Peer>? = emptyList()
)

@Serializable
data class Peer(
    @SerialName("Peer")
    val id: String
)

fun IpfsConnectedSession.getPeers(): List<String> = runCatching {
    val swarmUrl = URL("$baseUrl/api/v0/swarm/peers")
    val swarmConnection = swarmUrl.openConnection() as java.net.HttpURLConnection
    swarmConnection.requestMethod = "POST"
    swarmConnection.connectTimeout = 5000
    swarmConnection.readTimeout = 10000

    val content = swarmConnection.inputStream.bufferedReader().use { it.readText() }

    val response = json.decodeFromString<SwarmPeersResponse>(content)
    return response.peers?.map { it.id } ?: emptyList()
}.onFailure {
    println("[IpfsConnectedSession] Error to fetch peers: ${it.message}")
}.getOrNull() ?: emptyList()