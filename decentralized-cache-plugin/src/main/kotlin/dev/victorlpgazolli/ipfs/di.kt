package dev.victorlpgazolli.ipfs

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val ipfsDiModule: DI.Module = DI.Module("ipfs") {
    bindSingleton<IpfsConnectedSession> {
        instance<IpfsConnector>().invoke(instance())
    }
    bindProvider<IpfsConnector> {
        IpfsConnector()
    }
    bindProvider<IpfsReader> {
        IpfsReader(
            ipfsConnectedSession = instance()
        )
    }

}