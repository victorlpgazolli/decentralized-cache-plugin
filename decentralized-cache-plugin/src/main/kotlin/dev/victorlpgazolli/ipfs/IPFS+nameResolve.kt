package dev.victorlpgazolli.ipfs

import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

inline fun <reified T>IpfsConnectedSession.nameResolve(hash: String): T {

    val fullUrl = URL("$baseUrl/api/v0/name/resolve?arg=$hash&recursive=true")
    val resolveConnection = fullUrl.openConnection() as HttpURLConnection
    resolveConnection.requestMethod = "POST"
    resolveConnection.connectTimeout = 5000

    resolveConnection.readTimeout = 10000

    return Json.decodeFromString<T>(
        resolveConnection.inputStream.bufferedReader().use { it.readText() }
    )
}