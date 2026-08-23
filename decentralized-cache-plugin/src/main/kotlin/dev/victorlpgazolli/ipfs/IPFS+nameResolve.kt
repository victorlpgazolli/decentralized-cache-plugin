package dev.victorlpgazolli.ipfs

import java.net.HttpURLConnection
import java.net.URL

fun IpfsConnectedSession.nameResolve(hash: String): String {

    val fullUrl = URL("$baseUrl/api/v0/name/resolve?arg=$hash&recursive=true")
    val resolveConnection = fullUrl.openConnection() as HttpURLConnection
    resolveConnection.requestMethod = "POST"
    resolveConnection.connectTimeout = 5000

    resolveConnection.readTimeout = 10000

    return resolveConnection.inputStream.bufferedReader().use { it.readText() }
}