package dev.victorlpgazolli.utils

interface Logger {
    fun log(context: String, message: String)
}

class SimpleLogger : Logger {
    override fun log(context: String, message: String) {
        val callerName = Throwable().stackTrace[1].className.substringAfterLast(".")
        println("[${callerName}] [$context] $message")
    }
}

class QuietLogger : Logger {
    override fun log(context: String, message: String) {}
}
