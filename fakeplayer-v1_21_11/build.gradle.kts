import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("io.papermc.paperweight.userdev")
}

// Paper 1.21.11+ ships with Mojang-mapped class names natively.
// The CraftBukkit "vX_XX_RX" package version prefix was removed.
// Source code uses org.bukkit.craftbukkit.* (no revision prefix).
// The plugin JAR is loaded directly with Mojang mappings — no Spigot reobf needed.
paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-core"))
}
