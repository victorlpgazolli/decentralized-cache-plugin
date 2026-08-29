package dev.victorlpgazolli.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal interface HttpClientProvider {
    fun getClient(): HttpClient
}

internal fun HttpClientProvider(): HttpClientProvider {
    return object : HttpClientProvider {
        override fun getClient(): HttpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        }
                    )
                }

                install(Logging) { level = LogLevel.BODY }
            }
    }
}
