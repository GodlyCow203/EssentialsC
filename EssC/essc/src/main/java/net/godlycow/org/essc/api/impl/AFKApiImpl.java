package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.afk.AFKManager;
import net.godlycow.org.essc.api.AFKApi;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class AFKApiImpl implements AFKApi {

    private final AFKManager manager;

    public AFKApiImpl(AFKManager manager) {
        this.manager = manager;
    }

    @Override
    public void setAFK(@NotNull Player player, boolean afk, boolean broadcast) {
        manager.setAFK(player, afk, broadcast);
    }

    @Override
    public void toggleAFK(@NotNull Player player) {
        manager.toggleAFK(player);
    }

    @Override
    public boolean isAFK(@NotNull Player player) {
        return manager.isAFK(player);
    }

    @Override
    public boolean isAFK(@NotNull UUID uuid) {
        return manager.isAFK(uuid);
    }

    @Override
    public @Nullable Instant getAFKStartTime(@NotNull Player player) {
        return manager.getAFKStartTime(player);
    }

    @Override
    public long getAFKDurationSeconds(@NotNull Player player) {
        return manager.getAFKDurationSeconds(player);
    }

    @Override
    public @NotNull String getAFKDurationFormatted(@NotNull Player player) {
        return manager.getAFKDurationFormatted(player);
    }

    @Override
    public @NotNull Set<Player> getAFKPlayers() {
        return manager.getAFKPlayers();
    }

    @Override
    public int getAFKCount() {
        return manager.getAFKCount();
    }

    @Override
    public void updateActivity(@Nullable Player player) {
        manager.updateActivity(player);
    }
}