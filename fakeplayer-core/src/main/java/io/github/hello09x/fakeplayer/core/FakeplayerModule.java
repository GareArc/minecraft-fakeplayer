package io.github.hello09x.fakeplayer.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.api.spi.NMSBridge;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerList;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import io.github.hello09x.fakeplayer.core.manager.invsee.InvseeManager;
import io.github.hello09x.fakeplayer.core.manager.invsee.OpenInvInvseeManagerImpl;
import io.github.hello09x.fakeplayer.core.manager.invsee.SimpleInvseeManagerImpl;
import io.github.hello09x.fakeplayer.core.placeholder.FakeplayerPlaceholderExpansion;
import io.github.hello09x.fakeplayer.core.placeholder.FakeplayerPlaceholderExpansionImpl;
import io.github.hello09x.fakeplayer.core.util.ClassUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.logging.Logger;

public class FakeplayerModule extends AbstractModule {

    private final static Logger log = Main.getInstance().getLogger();

    @Override
    protected void configure() {
        super.bind(Plugin.class).toInstance(Main.getInstance());
    }

    @Provides
    @Singleton
    public @NotNull InvseeManager invseeManager(FakeplayerConfig config, FakeplayerManager fakeplayerManager, FakeplayerList fakeplayerList) {
        return switch (config.getInvseeImplement()) {
            case SIMPLE -> new SimpleInvseeManagerImpl(fakeplayerManager, fakeplayerList);
            case AUTO -> {
                if (Bukkit.getPluginManager().isPluginEnabled("OpenInv") && ClassUtils.isClassExists("com.lishid.openinv.IOpenInv")) {
                    log.info("Using OpenInv as invsee implement");
                    yield new OpenInvInvseeManagerImpl(fakeplayerManager, fakeplayerList);
                }
                log.info("Using simple invsee implement");
                yield new SimpleInvseeManagerImpl(fakeplayerManager, fakeplayerList);
            }
        };
    }

    @Provides
    @Singleton
    private @NotNull NMSBridge nmsBridge() {
        List<NMSBridge> bridges = ServiceLoader
                .load(NMSBridge.class, NMSBridge.class.getClassLoader())
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();

        // 1. Try exact version match first
        for (var bridge : bridges) {
            if (bridge.isSupported()) {
                return bridge;
            }
        }

        // 2. Best-effort fallback: find bridge with highest minCompatibleVersion that is <= current version.
        //    This allows future minor versions that share the same NMS revision to work automatically.
        var currentVersion = Bukkit.getMinecraftVersion();
        Comparator<NMSBridge> byMinVersion = Comparator.comparing(
                b -> new SemanticVersion(Objects.requireNonNull(b.getMinCompatibleVersion()))
        );
        var fallback = bridges.stream()
                .filter(b -> {
                    var min = b.getMinCompatibleVersion();
                    return min != null && compareVersions(min, currentVersion) <= 0;
                })
                .max(byMinVersion)
                .orElse(null);

        if (fallback != null) {
            var fallbackMin = fallback.getMinCompatibleVersion();
            var sameSeries = isSameMajorMinor(fallbackMin, currentVersion);
            log.warning("====================================================");
            log.warning("FakePlayer: No exact NMS bridge for Minecraft " + currentVersion + ".");
            if (sameSeries) {
                log.warning("Using fallback bridge (>= " + fallbackMin + ") — same minor version series,");
                log.warning("this is likely to work if the NMS revision has not changed.");
            } else {
                log.warning("Using fallback bridge (>= " + fallbackMin + ") — DIFFERENT minor version series.");
                log.warning("This will probably FAIL. Please check for a plugin update that supports " + currentVersion + ".");
            }
            log.warning("====================================================");
            return fallback;
        }

        throw new ExceptionInInitializerError("Unsupported Minecraft version: " + currentVersion
                + ". Please check for a plugin update at https://github.com/garearc/minecraft-fakeplayer");
    }

    private static int compareVersions(@NotNull String v1, @NotNull String v2) {
        return new SemanticVersion(v1).compareTo(new SemanticVersion(v2));
    }

    /** Returns true if both versions share the same major.minor (e.g. both start with "1.21."). */
    private static boolean isSameMajorMinor(@Nullable String v1, @NotNull String v2) {
        if (v1 == null) return false;
        var p1 = v1.split("\\.");
        var p2 = v2.split("\\.");
        if (p1.length < 2 || p2.length < 2) return false;
        return p1[0].equals(p2[0]) && p1[1].equals(p2[1]);
    }

    private record SemanticVersion(@NotNull String raw) implements Comparable<SemanticVersion> {
        @Override
        public int compareTo(@NotNull SemanticVersion other) {
            var parts1 = raw.split("\\.");
            var parts2 = other.raw.split("\\.");
            int len = Math.max(parts1.length, parts2.length);
            for (int i = 0; i < len; i++) {
                int n1 = i < parts1.length ? parseIntSafe(parts1[i]) : 0;
                int n2 = i < parts2.length ? parseIntSafe(parts2[i]) : 0;
                if (n1 != n2) return Integer.compare(n1, n2);
            }
            return 0;
        }

        private static int parseIntSafe(String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    @Singleton
    @Provides
    private @Nullable FakeplayerPlaceholderExpansion fakeplayerPlaceholderExpansion(FakeplayerManager fakeplayerManager, ActionManager actionManager) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") || !ClassUtils.isClassExists("me.clip.placeholderapi.expansion.PlaceholderExpansion")) {
            return null;
        }
        return new FakeplayerPlaceholderExpansionImpl(fakeplayerManager, actionManager);
    }

}
