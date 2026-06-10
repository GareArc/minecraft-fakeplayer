# Contributing: Adding New Minecraft Version Support

When a new Minecraft version is released, adding explicit support follows a predictable pattern.
There are two cases depending on whether the NMS (CraftBukkit) revision changed.

## Case 1: Same NMS revision (thin wrapper)

If the new version shares the same CraftBukkit R-number as the previous one (e.g. 1.21.7/1.21.8
both use `v1_21_R5`, and 1.21.9/1.21.10 both use `v1_21_R6`), you only need a thin wrapper.

How to check: run `grep -r "craftbukkit\." fakeplayer-v1_21_9/src` and compare to the previous version.
If the R-number is the same (e.g. both `v1_21_R6`), it is a thin wrapper.

### Steps

1. **Create the module directory** following the naming convention:
   ```
   mkdir -p fakeplayer-v1_21_11/src/main/java/io/github/hello09x/fakeplayer/v1_21_11/spi/
   ```

2. **Create `pom.xml`** (copy from `fakeplayer-v1_21_10/pom.xml`, update `artifactId` and `nms.version`):
   ```xml
   <artifactId>fakeplayer-v1_21_11</artifactId>
   <nms.version>1.21.11-R0.1-SNAPSHOT</nms.version>
   <!-- dependency: change fakeplayer-v1_21_9 to fakeplayer-v1_21_10 if needed -->
   ```

3. **Create `NMSBridgeImpl.java`** (copy from `fakeplayer-v1_21_10`, update package and SUPPORTS):
   ```java
   package io.github.hello09x.fakeplayer.v1_21_11.spi;
   // import from previous module's spi package
   import io.github.hello09x.fakeplayer.v1_21_9.spi.*;
   // ...
   private final static Set<String> SUPPORTS = Set.of("1.21.11");
   ```

4. **Register in the ServiceLoader file** (`fakeplayer-dist/src/main/resources/META-INF/services/io.github.hello09x.fakeplayer.api.spi.NMSBridge`):
   ```
   io.github.hello09x.fakeplayer.v1_21_11.spi.NMSBridgeImpl
   ```

5. **Add the module to the parent `pom.xml`** modules list:
   ```xml
   <module>fakeplayer-v1_21_11</module>
   ```

6. **Add the dependency to `fakeplayer-dist/pom.xml`**:
   ```xml
   <dependency>
       <groupId>io.github.hello09x.fakeplayer</groupId>
       <artifactId>fakeplayer-v1_21_11</artifactId>
       <version>${revision}</version>
   </dependency>
   ```

> **Note**: For the thin wrapper, you need `spigot:1.21.11-R0.1-SNAPSHOT:remapped-mojang` in your
> local Maven repository. Build it with SpigotBuildTools for the target version.

## Case 2: New NMS revision (full implementation)

If the CraftBukkit R-number changed (e.g. moving from `v1_21_R6` to `v1_22_R0`), a full
implementation is needed. This happens when Mojang makes breaking internal API changes.

### Steps

1. Follow steps 1-6 from Case 1, but set `nms.version` to the new version.

2. Copy all source files from the previous "full implementation" module (e.g. `fakeplayer-v1_21_9`)
   into your new module.

3. Update all `import org.bukkit.craftbukkit.v1_21_R6.*` → `import org.bukkit.craftbukkit.v1_22_R0.*`
   (adjust the R-number to match the new version).

4. Fix any compilation errors caused by NMS API changes:
   - `NMSServerPlayerImpl.java`: check `absSnapTo`, `ValueInputContextHelper`, `ClientInformation`
   - `NMSNetworkImpl.java`: check `CommonListenerCookie`, `placeNewPlayer`
   - `FakeServerGamePacketListenerImpl.java`: check packet class names and methods
   - Action classes: check `handleBlockBreakAction`, `getMaxY`, `mayInteract`, `gameMode`

5. **Update `getMinCompatibleVersion()`** on the new bridge to point to the new minimum version,
   so it becomes the new fallback for future patch releases.

## NMS revision history (1.21.x)

The CraftBukkit *revision* (`v1_21_R5`, `v1_21_R6`, …) is separate from the Spigot Maven
artifact version (`1.21.7-R0.1-SNAPSHOT`). The revision only bumps when Bukkit breaks its
API contract; multiple patch versions share the same revision.

> **Paper 1.21.11 breaking change**: Paper dropped Spigot reobf mappings entirely starting
> with 1.21.11. CraftBukkit (`org.bukkit.craftbukkit`) classes no longer carry the version
> prefix (e.g. `v1_21_R6`). All OBC imports become `org.bukkit.craftbukkit.*` with no
> revision prefix. The plugin JAR is loaded with Mojang-mapped class names directly — no
> specialsource remapping step needed.

| Minecraft | CraftBukkit package | Module(s) |
|---|---|---|
| 1.21 | v1_21_R1 | fakeplayer-v1_21 |
| 1.21.1 | v1_21_R1 | fakeplayer-v1_21_1 (thin wrapper over v1_21) |
| 1.21.3 | v1_21_R2 | fakeplayer-v1_21_3 |
| 1.21.4 | v1_21_R3 | fakeplayer-v1_21_4 |
| 1.21.5 | v1_21_R4 | fakeplayer-v1_21_5 |
| 1.21.6 | v1_21_R5 | fakeplayer-v1_21_6 |
| 1.21.7, 1.21.8 | v1_21_R5 | fakeplayer-v1_21_7, _8 (thin wrappers over v1_21_6) |
| 1.21.9, 1.21.10 | v1_21_R6 | fakeplayer-v1_21_9 (full), _10 (thin wrapper) |
| 1.21.11+ | no prefix (Mojang) | fakeplayer-v1_21_11 (full); future patches auto-fallback |

## Case 3: Paper dropped Spigot reobf (1.21.11+)

Starting with 1.21.11, Paper no longer ships with Spigot-obfuscated class names.
This is a **full implementation** (like Case 2), but with different import style and no remapping.

1. Follow steps 1-6 from Case 1, but **omit the specialsource plugin** from `pom.xml`.
   The compiled output stays Mojang-mapped and Paper loads it directly.

2. Copy all source files from `fakeplayer-v1_21_11` (the reference implementation).

3. Change **all OBC imports**: `org.bukkit.craftbukkit.v1_21_R6.*` → `org.bukkit.craftbukkit.*`
   (remove the `v1_21_R6` segment entirely).

4. NMS imports (`net.minecraft.*`) remain the same — they were already Mojang-mapped.

5. Fix any compilation errors from NMS API changes as in Case 2 step 4.

6. In `NMSBridgeImpl`, set `SUPPORTS = Set.of("1.21.X")` and
   `getMinCompatibleVersion() = "1.21.X"` (the new fallback anchor).
