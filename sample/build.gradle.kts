plugins {
    alias(libs.plugins.kotlinJvm)
}

repositories {
    mavenLocal()
    mavenCentral()
}
tasks.register("testing") {
    doLast {
        println("Testing sample project")
    }
}