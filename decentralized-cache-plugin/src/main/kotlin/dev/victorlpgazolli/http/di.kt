package dev.victorlpgazolli.http

import io.ktor.client.HttpClient
import org.kodein.di.DI
import org.kodein.di.bindSingleton

val httpDiModule: DI.Module =
    DI.Module("http") { bindSingleton<HttpClient> { HttpClientProvider().getClient() } }
