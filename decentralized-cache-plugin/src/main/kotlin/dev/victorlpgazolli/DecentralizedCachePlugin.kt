package dev.victorlpgazolli

import dev.victorlpgazolli.service.IpfsBuildCacheServiceFactory
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class DecentralizedCachePlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val buildCacheConfiguration = settings.buildCache

        buildCacheConfiguration.registerBuildCacheService(
            DecentralizedConfiguration::class.java,
            IpfsBuildCacheServiceFactory::class.java,
        )
    }
}
