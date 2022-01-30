plugins {
    id("io.papermc.paperweight.userdev")
}

group = "net.gtminecraft.gitgames"
version = "1.0.0"

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    compileOnly(project(":service"))
    compileOnly(project(":compatability"))
}