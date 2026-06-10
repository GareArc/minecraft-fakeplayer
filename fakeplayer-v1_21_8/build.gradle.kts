// Thin wrapper: 1.21.8 shares CraftBukkit revision v1_21_R5 with 1.21.6.
// Delegates all NMS work to the fakeplayer-v1_21_6 implementation.
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-core"))
    compileOnly(project(":fakeplayer-v1_21_6"))
}
