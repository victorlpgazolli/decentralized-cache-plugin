package dev.victorlpgazolli.ipfs

public fun String.addIpfsPrefix(): String {
    return "/ipfs/$this"
}