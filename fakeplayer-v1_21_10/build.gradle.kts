// Thin wrapper: 1.21.10 shares CraftBukkit revision v1_21_R6 with 1.21.9.
// Delegates all NMS work to the fakeplayer-v1_21_9 implementation.
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-core"))
    compileOnly(project(":fakeplayer-v1_21_9"))
}
