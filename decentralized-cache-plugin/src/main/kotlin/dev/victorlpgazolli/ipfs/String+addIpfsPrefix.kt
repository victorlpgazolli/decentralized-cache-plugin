package dev.victorlpgazolli.ipfs

public fun String.addIpfsPrefix(): String {
    return "/ipfs/${this.removeIpfsPrefix()}"
}
public fun String.removeIpfsPrefix(): String {
    return this.removePrefix("/ipfs/")
}
