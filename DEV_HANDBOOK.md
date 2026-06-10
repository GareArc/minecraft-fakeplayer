# Developer Handbook

A practical guide to building, modifying, and extending fakeplayer.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [First-time Setup](#first-time-setup)
3. [Daily Workflow](#daily-workflow)
4. [Project Structure](#project-structure)
5. [Build System Deep-dive](#build-system-deep-dive)
6. [Adding a New Minecraft Version](#adding-a-new-minecraft-version)
7. [Version Numbering and Releases](#version-numbering-and-releases)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

| Requirement | Minimum | Notes |
|---|---|---|
| Java JDK | **21** | Compile and run target. All NMS modules ≥ 1.21 require JDK 21. Modules 1.20.x compile to Java 17 source level but the JDK itself must still be 21+. |
| Internet (build-time only) | — | First build downloads Paper dev bundles from `https://repo.papermc.io/repository/maven-public/`. Cached locally after that. |
| `lib/OpenInv.jar` | optional | Enables the OpenInv integration. Without it, the integration is silently disabled at runtime. |
| `lib/PlaceholderAPI-2.11.6.jar` | optional | Enables PlaceholderAPI support. |

The Gradle wrapper (`./gradlew`) is included — no separate Gradle installation is needed.

---

## First-time Setup

```bash
# Clone
git clone https://github.com/<your-fork>/minecraft-fakeplayer.git
cd minecraft-fakeplayer

# Run the setup script (checks prerequisites, installs git hooks)
make setup
# or directly:
./scripts/setup.sh
```

The setup script will:
- Verify Java 21+ is on your PATH
- Ensure `gradlew` is executable
- Install git hooks from `scripts/hooks/` into `.git/hooks/`
- Warn about any missing `lib/` JARs

To re-verify without making any changes:
```bash
make verify-env
```

---

## Daily Workflow

### Build

```bash
make build
# → build/libs/fakeplayer-<version>.jar
```

Under the hood this runs `./gradlew :fakeplayer-dist:shadowJar`, which compiles all
version modules in parallel and bundles them into one fat JAR.

### Clean

```bash
make clean
```

### Build and deploy to a local test server

```bash
# One-time (or add to shell profile):
export SERVER_DIR=~/servers/test/plugins

make deploy
# Compiles if needed, then copies the JAR to $SERVER_DIR
```

Or inline:
```bash
make deploy SERVER_DIR=~/servers/test/plugins
```

### List all available targets

```bash
make help
```

---

## Project Structure

```
minecraft-fakeplayer/
├── fakeplayer-api/          # SPI interfaces only (NMSBridge, etc.)
├── fakeplayer-core/         # All plugin logic — no direct NMS, no version deps
├── fakeplayer-dist/         # Shadow-fat JAR assembler; owns the ServiceLoader registry
├── fakeplayer-v1_20_1/      # Full NMS implementation for MC 1.20.1
├── fakeplayer-v1_20_2/      # Full NMS for 1.20.2
├── fakeplayer-v1_20_3/      # Thin wrapper → v1_20_4
├── fakeplayer-v1_20_4/      # Full NMS for 1.20.4
│   ...
├── fakeplayer-v1_21_9/      # Full NMS for 1.21.9  (last Spigot-remapped)
├── fakeplayer-v1_21_10/     # Thin wrapper → v1_21_9
├── fakeplayer-v1_21_11/     # Full NMS for 1.21.11 (first Mojang-mapped)
├── build.gradle.kts         # Root build — shared config, plugin declarations
├── settings.gradle.kts      # Module list
├── gradle.properties        # revision= (the canonical version string)
├── Makefile                 # Developer shortcuts
├── scripts/
│   ├── setup.sh             # First-time setup
│   ├── bump-version.sh      # Version bumper
│   └── hooks/
│       └── pre-commit       # Git pre-commit checks
└── lib/                     # Local JARs for optional integrations (not committed)
```

### The SPI pattern

Every NMS version module exposes a single class:
```
io.github.hello09x.fakeplayer.v<VERSION>.spi.NMSBridgeImpl implements NMSBridge
```

`fakeplayer-dist` lists every bridge in:
```
src/main/resources/META-INF/services/io.github.hello09x.fakeplayer.api.spi.NMSBridge
```

At runtime, `FakeplayerModule.nmsBridge()` (in `fakeplayer-core`) uses `ServiceLoader` to
load all bridges, then picks the right one:

1. **Exact match** — `bridge.isSupported(serverVersion)` returns `true`
2. **Best-effort fallback** — if no exact match, find the bridge whose
   `getMinCompatibleVersion()` is the highest value ≤ the current server version.
   This means a server running MC 1.21.12 (not yet explicitly supported) will
   automatically fall back to the `v1_21_11` bridge.

---

## Build System Deep-dive

### Gradle modules

| Module | paperweight | Shadow | NMS |
|---|---|---|---|
| `fakeplayer-api` | ✗ | ✗ | none |
| `fakeplayer-core` | ✗ | ✗ | none (uses paper-api only) |
| `fakeplayer-dist` | ✗ | ✓ | bundler only |
| `fakeplayer-v1_20_x` (full) | ✓ (REOBF_PRODUCTION) | ✗ | 1.20.x |
| `fakeplayer-v1_21_x` (full, ≤1.21.10) | ✓ (REOBF_PRODUCTION) | ✗ | 1.21.x |
| `fakeplayer-v1_21_11` (full) | ✓ (MOJANG_PRODUCTION) | ✗ | 1.21.11+ |
| Thin wrapper modules | ✗ | ✗ | delegated to full module |

### REOBF vs MOJANG production

- **REOBF_PRODUCTION** (default): paperweight remaps compiled bytecode from Mojang-mapped
  class names back to the Spigot-obfuscated names that the server runtime expects.
  Used for all versions up to 1.21.10.

- **MOJANG_PRODUCTION**: no remapping. Paper 1.21.11+ ships with Mojang-mapped class names
  natively, so the JAR can be loaded directly.

Set in each full NMS module's `build.gradle.kts`:
```kotlin
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
```
REOBF modules omit this line (it's the default).

### Paper dev bundles

`paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")` triggers paperweight to:
1. Download the server JAR from `repo.papermc.io`
2. Generate Mojang→Spigot remapping tables
3. Make the remapped classes available as `compileOnly` during compilation

First build takes ~2 minutes per new bundle (downloaded once, cached in `~/.gradle/caches/`).

### Shadow fat JAR

`fakeplayer-dist` uses the Shadow plugin to merge every module into one distributable JAR.
Output: `build/libs/fakeplayer-<version>.jar`

The fat JAR contains REOBF-remapped classes from older modules *and* Mojang-mapped classes
from v1_21_11. This is safe because:
- ServiceLoader instantiates bridges lazily
- Only the bridge matching the running server version is ever constructed
- NMS classes from non-matching bridges are never loaded, so ClassNotFoundException never triggers

---

## Adding a New Minecraft Version

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full step-by-step procedure. Quick decision tree:

```
New MC version released
        │
        ▼
Does the CraftBukkit R-number change?
  (check: do OBC imports like v1_21_R6 change to v1_22_R0?)
        │
   No ──┤── Yes ──►  Case 2: full NMS implementation
        │                     (copy from nearest full module, fix compile errors)
        ▼
Is it 1.21.11+ era (Mojang-mapped)?
        │
   Yes ─┤── No ───►  Case 1: thin wrapper
        │                     (NMSBridgeImpl delegates to previous full module)
        ▼
   Case 3: Mojang-mapped full NMS
     (copy from fakeplayer-v1_21_11, use MOJANG_PRODUCTION)
```

Regardless of case, always:
1. Add the new module to `settings.gradle.kts` (`include(...)`)
2. Add it as `implementation(project(...))` in `fakeplayer-dist/build.gradle.kts`
3. Register `NMSBridgeImpl` in `META-INF/services/io.github.hello09x.fakeplayer.api.spi.NMSBridge`

---

## Version Numbering and Releases

The plugin version lives in `gradle.properties`:
```
revision=0.3.20
```

All modules inherit this version via `allprojects { version = revision }` in the root build.

### Bump the version

```bash
make bump-patch   # 0.3.20 → 0.3.21
make bump-minor   # 0.3.20 → 0.4.0
make bump-major   # 0.3.20 → 1.0.0
```

These run `scripts/bump-version.sh` which edits `gradle.properties` in-place.

### Release checklist

1. `make verify-env` — confirm no environment issues
2. `make rebuild` — full clean build, confirm no errors
3. `make bump-patch` (or minor/major as appropriate)
4. `git add gradle.properties && git commit -m "chore: bump version to $(make version)"`
5. `git tag v<version>`
6. `git push && git push --tags`
7. Attach `build/libs/fakeplayer-<version>.jar` to the GitHub release

---

## Troubleshooting

### `Could not resolve io.papermc.paper:dev-bundle:X.Y.Z-R0.1-SNAPSHOT`

paperweight needs internet access to `https://repo.papermc.io/repository/maven-public/`.
The first build for any new MC version requires a network connection.
Subsequent builds use the local Gradle cache (`~/.gradle/caches/`).

### `Unresolved reference: ReobfArtifactConfiguration`

The correct import is:
```kotlin
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
```
Not `io.papermc.paperweight.tasks.*`.

### `ClassNotFoundException` at server startup

The SPI service file is missing or has a typo. Check:
```
fakeplayer-dist/src/main/resources/META-INF/services/
    io.github.hello09x.fakeplayer.api.spi.NMSBridge
```
Each line must be the fully-qualified class name of a concrete `NMSBridgeImpl`.

### `No compatible NMSBridge found for version X`

The server is running a version with no exact match and no fallback bridge covers it.
Add explicit support following [CONTRIBUTING.md](CONTRIBUTING.md), or verify that the
correct module's `getMinCompatibleVersion()` covers the new version.

### Build succeeds but `make deploy` copies nothing

Check that `gradle.properties` has the right `revision` and that
`build/libs/fakeplayer-<version>.jar` exists after the build.

### gradlew permission denied

```bash
chmod +x gradlew
# or:
make setup
```

### `.gradle/` directory appears as untracked in `git status`

This is the local Gradle daemon/cache directory. It is listed in `.gitignore`.
If it keeps appearing, verify the `.gitignore` entry is not overridden by a global ignore file:
```bash
git check-ignore -v .gradle
```
