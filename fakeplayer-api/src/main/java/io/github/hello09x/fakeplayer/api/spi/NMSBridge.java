package io.github.hello09x.fakeplayer.api.spi;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;

public interface NMSBridge {

    @NotNull NMSEntity fromEntity(@NotNull Entity entity);

    @NotNull NMSServer fromServer(@NotNull Server server);

    @NotNull NMSServerLevel fromWorld(@NotNull World world);

    @NotNull NMSServerPlayer fromPlayer(@NotNull Player player);

    @NotNull NMSNetwork createNetwork(@NotNull InetAddress address);

    boolean isSupported();

    /**
     * The minimum Minecraft version this bridge is compatible with as a fallback.
     * When no exact match is found via {@link #isSupported()}, the bridge with the
     * highest {@code minCompatibleVersion} that is still <= the running server version
     * will be used as a best-effort fallback.
     *
     * <p>Return {@code null} to opt out of fallback selection (default).</p>
     */
    default @Nullable String getMinCompatibleVersion() {
        return null;
    }

    @NotNull ActionTicker createAction(@NotNull Player player, @NotNull ActionType action, @NotNull ActionSetting setting);

}
