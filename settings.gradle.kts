plugins {
    id "com.atkinsondev.object-store-cache" version "2.1.0"
}

buildCache {
    local {
        enabled = !isCI
    }
    remote(com.atkinsondev.cache.ObjectStoreBuildCache) {
        endpoint = 'sfo2.digitaloceanspaces.com'
        accessKey = cacheAccessKey
        secretKey = cacheSecretKey
        bucket = 'cacheplugin'
        autoCreateBucket = true
        expirationInDays = 10
        push = isCI
    }
}
