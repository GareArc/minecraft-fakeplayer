plugins {
    id("io.papermc.paperweight.userdev")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-core"))
}
