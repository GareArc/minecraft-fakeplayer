// Thin wrapper: 1.20.3 shares CraftBukkit revision with 1.20.4.
// Delegates all NMS work to the fakeplayer-v1_20_4 implementation.
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-core"))
    compileOnly(project(":fakeplayer-v1_20_4"))
}
