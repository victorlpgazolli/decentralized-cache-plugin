package dev.victorlpgazolli

import dev.victorlpgazolli.cache.cacheDiModule
import dev.victorlpgazolli.http.httpDiModule
import dev.victorlpgazolli.ipfs.ipfsDiModule
import dev.victorlpgazolli.utils.utilsDiModule
import org.kodein.di.DI
import org.kodein.di.bindSingleton

val appDiModule = DI.lazy {
    importOnce(cacheDiModule)
    importOnce(ipfsDiModule)
    importOnce(httpDiModule)
    importOnce(utilsDiModule)
    bindSingleton { DecentralizedConfigurationHolder() }
}