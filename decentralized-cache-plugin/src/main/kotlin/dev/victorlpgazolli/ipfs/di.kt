package dev.victorlpgazolli.ipfs

import dev.victorlpgazolli.DecentralizedConfigurationHolder
import org.gradle.internal.cc.base.logger
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val ipfsDiModule: DI.Module = DI.Module("ipfs") {

    bindSingleton<IpfsConnectedSession> {
        instance<IpfsConnector>().invoke(
            configuration = instance<DecentralizedConfigurationHolder>().configuration,
        )
    }

    bindProvider<IpfsConnector> {
        IpfsConnector(
            logger = instance(),
        )
    }

    bindProvider<IpfsReader> {
        IpfsReader(
            ipfsConnectedSession = instance(),
            logger = instance()
        )
    }

    bindProvider<IpfsWriter> {
        IpfsWriter(
            ipfsConnectedSession = instance(),
            logger = instance()
        )
    }

    bindProvider<ProvideHashToNetworkUseCase> {
        ProvideHashToNetworkUseCaseImpl(
            ipfsConnectedSession = instance(),
            logger = instance(),
        )
    }
    bindProvider<UpdateManifestForCleanup> {
        UpdateManifestForCleanupImpl(
            ipfsConnectedSession = instance(),
            manifestCacheHelper = instance(),
            provideHashToNetworkUseCase = instance(),
            logger = instance(),
        )
    }
}