package dev.victorlpgazolli.utils

import org.kodein.di.DI
import org.kodein.di.bindSingleton

val utilsDiModule: DI.Module = DI.Module("utils") { bindSingleton<Logger> { SimpleLogger() } }
