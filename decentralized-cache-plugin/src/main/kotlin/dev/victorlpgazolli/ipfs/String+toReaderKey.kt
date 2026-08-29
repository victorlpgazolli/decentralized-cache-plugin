package dev.victorlpgazolli.ipfs

fun String.toReaderKey(): IpfsReaderKey {
    val normalizedStartOfKey = removePrefix("/")
    return when (normalizedStartOfKey.take(4)) {
        "ipfs" -> IpfsReaderKey.IpfsPath(normalizedStartOfKey.removePrefix("ipfs/"))
        "ipns" -> IpfsReaderKey.IpnsPath(normalizedStartOfKey.removePrefix("ipns/"))
        "bafy" -> IpfsReaderKey.IpfsPath(normalizedStartOfKey)
        else -> when(normalizedStartOfKey.take(2)) {
            "Qm" -> IpfsReaderKey.IpfsPath(normalizedStartOfKey)
            "k5" -> IpfsReaderKey.IpnsPath(normalizedStartOfKey)
            else -> throw IpfsReaderUnknownKeyException(normalizedStartOfKey)
        }
    }
}