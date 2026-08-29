package dev.victorlpgazolli.ipfs

import java.net.URL

fun IpfsConnectedSession.catText(hash: String): String {
    return catStream(hash).bufferedReader().use { it.readText() }
}

fun IpfsConnectedSession.catStream(hash: String): java.io.InputStream {
    val catUrl = URL("$baseUrl/api/v0/cat?arg=$hash")
    val catConnection = catUrl.openConnection() as java.net.HttpURLConnection
    catConnection.requestMethod = "POST"
    catConnection.connectTimeout = 5000

    catConnection.readTimeout = 10000
    return catConnection.inputStream
}
