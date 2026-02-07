plugins {
    java
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