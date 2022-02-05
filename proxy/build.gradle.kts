plugins {
    `java-library`
    id("net.minecrell.plugin-yml.bungee") version "0.5.1"
}

group = "net.gtminecraft.gitgames"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.waterfallmc:waterfall-api:1.18-R0.1-SNAPSHOT")

    compileOnly("io.netty:netty-all:4.1.73.Final")
    compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testCompileOnly("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    compileOnly(project(":compatability"))
}

bungee {
    name = "GiTGames"
    main = "net.gtminecraft.gitgames.proxy.CoreProxyPlugin"
    author = "Frank"
    version = "1.0.0"
}