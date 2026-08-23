package dev.victorlpgazolli.ipfs

import java.net.URL

fun IpfsConnectedSession.cat(hash: String): String {

    val catUrl = URL("$baseUrl/api/v0/cat?arg=$hash")
    val catConnection = catUrl.openConnection() as java.net.HttpURLConnection
    catConnection.requestMethod = "POST"
    catConnection.connectTimeout = 5000

    catConnection.readTimeout = 10000

    return catConnection.inputStream.bufferedReader().use { it.readText() }
}