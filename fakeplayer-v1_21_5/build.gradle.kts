plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-core"))
}
