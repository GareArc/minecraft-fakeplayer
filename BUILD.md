# Build Introduction

## Build System

This project uses **Gradle** with [paperweight-userdev](https://github.com/PaperMC/paperweight) for NMS
access. The Gradle wrapper (`./gradlew`) is included; no separate Gradle installation is required.

> **Legacy**: Maven build files (`pom.xml`) are kept for reference but are no longer the primary
> build system. The Gradle build supersedes them.

## Build NMS Dependencies (Paper Dev Bundle)

Mojang does not allow redistribution of the remapped server JARs, so Paper downloads them
through the `paperweight.paperDevBundle(...)` dependency on first build.

**Requirement**: Internet access to Paper's Maven repository:
```
https://repo.papermc.io/repository/maven-public/
```

The bundles are downloaded into the Gradle cache (`~/.gradle/caches/`) on first build and
reused thereafter.

> If you previously used the Maven + SpigotBuildTools workflow, those cached Spigot JARs are no
> longer needed. paperweight-userdev fetches everything automatically.

## Local JAR Dependencies

Two JARs must be placed in the `lib/` directory before building:

```
lib/OpenInv.jar
lib/PlaceholderAPI-2.11.6.jar
```

These are optional plugin integrations. The build compiles against them but they are not
required at runtime (the plugin detects them dynamically).

## Build the Plugin

```bash
# Full build (produces target/fakeplayer-<version>.jar via the Shadow JAR)
./gradlew :fakeplayer-dist:shadowJar

# The output is at:
# build/libs/fakeplayer-<version>.jar
```

## Build a Specific Version Module

```bash
# Build only the 1.21.9 NMS module
./gradlew :fakeplayer-v1_21_9:build
```

## Module Map

| Module | Type | NMS version | Notes |
|--------|------|-------------|-------|
| fakeplayer-api | library | — | SPI interfaces only |
| fakeplayer-core | library | — | Plugin logic, no NMS |
| fakeplayer-dist | fat JAR | — | Shadow bundles everything |
| fakeplayer-v1_20_1/2 | full NMS | 1.20.1/2 | REOBF_PRODUCTION |
| fakeplayer-v1_20_3 | thin wrapper | — | wraps v1_20_4 |
| fakeplayer-v1_20_4 | full NMS | 1.20.4 | REOBF_PRODUCTION |
| fakeplayer-v1_20_5 | thin wrapper | — | wraps v1_20_6 |
| fakeplayer-v1_20_6 | full NMS | 1.20.6 | REOBF_PRODUCTION |
| fakeplayer-v1_21 | full NMS | 1.21 | REOBF_PRODUCTION |
| fakeplayer-v1_21_1 | thin wrapper | — | wraps v1_21 |
| fakeplayer-v1_21_3/4/5/6 | full NMS | 1.21.3–6 | REOBF_PRODUCTION |
| fakeplayer-v1_21_7/8 | thin wrapper | — | wrap v1_21_6 |
| fakeplayer-v1_21_9 | full NMS | 1.21.9 | REOBF_PRODUCTION |
| fakeplayer-v1_21_10 | thin wrapper | — | wraps v1_21_9 |
| fakeplayer-v1_21_11 | full NMS | 1.21.11 | **MOJANG_PRODUCTION** |

## Adding a New Minecraft Version

See [CONTRIBUTING.md](CONTRIBUTING.md) for the step-by-step guide covering:
- **Case 1** (thin wrapper): same NMS revision, just add a new wrapper module
- **Case 2** (new NMS revision): new CraftBukkit revision, full implementation needed
- **Case 3** (Mojang-mapped, 1.21.11+): Paper dropped Spigot reobf; use `MOJANG_PRODUCTION`
