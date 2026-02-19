package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.event.afk.AFKManager;
import net.godlycow.org.essc.api.event.afk.change.AFKChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class AFKManagerImpl implements AFKManager {
    private final EssentialsC plugin;
    private final net.godlycow.org.essc.afk.AFKManager internal;

    public AFKManagerImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.internal = plugin.getAfkManager();
    }

    @Override
    public void setAFK(@NotNull Player player, boolean afk, boolean broadcast) {
        if (internal == null) return;

        boolean wasAFK = internal.isAFK(player);

        if (wasAFK != afk) {
            AFKChangeEvent event = new AFKChangeEvent(player, afk, wasAFK, broadcast);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            broadcast = event.isBroadcast();
        }

        internal.setAFK(player, afk, broadcast);
    }

    @Override
    public void toggleAFK(@NotNull Player player) {
        if (internal == null) return;
        internal.toggleAFK(player);
    }

    @Override
    public boolean isAFK(@NotNull Player player) {
        return internal != null && internal.isAFK(player);
    }

    @Override
    public boolean isAFK(@NotNull UUID uuid) {
        return internal != null && internal.isAFK(uuid);
    }

    @Override
    public Instant getAFKStartTime(@NotNull Player player) {
        if (internal == null) return null;
        return internal.getAFKStartTime(player);
    }

    @Override
    public long getAFKDurationSeconds(@NotNull Player player) {
        if (internal == null) return 0;
        return internal.getAFKDurationSeconds(player);
    }

    @Override
    @NotNull
    public String getAFKDurationFormatted(@NotNull Player player) {
        if (internal == null) return "0s";
        return internal.getAFKDurationFormatted(player);
    }

    @Override
    @NotNull
    public Set<Player> getAFKPlayers() {
        if (internal == null) return Collections.emptySet();
        return internal.getAFKPlayers();
    }

    @Override
    public int getAFKCount() {
        if (internal == null) return 0;
        return internal.getAFKCount();
    }

    @Override
    public int getOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    @Override
    public void updateActivity(@NotNull Player player) {
        if (internal == null) return;
        internal.updateActivity(player);
    }

    @Override
    public Instant getLastActivity(@NotNull Player player) {
        if (internal == null) return null;
        return Instant.now();
    }

    @Override
    public long getInactiveSeconds(@NotNull Player player) {
        Instant last = getLastActivity(player);
        if (last == null) return 0;
        return Duration.between(last, Instant.now()).getSeconds();
    }

    @Override
    public boolean isEnabled() {
        return internal != null && plugin.getConfigManager().isAfkEnabled();
    }

    @Override
    public boolean isFrozen(@NotNull Player player) {
        if (internal == null) return false;
        return isAFK(player) && plugin.getConfigManager().isAfkFreezePlayer();
    }

    @Override
    public boolean isCommandBlocked(@NotNull String command) {
        if (internal == null) return false;
        return internal.isCommandBlocked(command);
    }

    @Override
    public void updatePlayerListName(@NotNull Player player) {
        if (internal == null) return;
        internal.updatePlayerListName(player);
    }

    @Override
    public void clearAllAFK() {
        if (internal == null) return;

        for (Player player : new HashSet<>(getAFKPlayers())) {
            setAFK(player, false, false);
        }
    }

    @Override
    public void reload() {
        if (internal == null) return;
        internal.reload();
    }
}