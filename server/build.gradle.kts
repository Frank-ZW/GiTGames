import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("io.papermc.paperweight.userdev")
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

group = "net.gtminecraft.gitgames"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    compileOnly("io.netty:netty-all:4.1.73.Final")
    compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("org.projectlombok:lombok:1.18.22")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.luben:zstd-jni:1.5.2-1")

    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testCompileOnly("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    compileOnly(project(":compatability"))
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

bukkit {
    name = "GiTGames"
    main = "net.gtminecraft.gitgames.server.CorePlugin"
    apiVersion = "1.18"
    author = "Frank"
    depend = listOf("Vault")
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}