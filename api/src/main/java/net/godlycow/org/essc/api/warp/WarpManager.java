package net.godlycow.org.essc.api.warp;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface WarpManager {
    boolean isWarpSystemEnabled();

    Warp getWarp(String name);
    boolean warpExists(String name);

    Collection<Warp> getAllWarps();
    Collection<Warp> getVisibleWarps();
    Collection<Warp> getWarpsByCategory(String category);
    Set<String> getCategories();

    CompletableFuture<Boolean> createWarp(String name, Location location);
    CompletableFuture<Boolean> deleteWarp(String name);
    CompletableFuture<Boolean> updateWarp(Warp warp);

    CompletableFuture<Integer> getWarpUsage(UUID playerId, String warpName);

    boolean isOnCooldown(Player player);
    long getRemainingCooldownSeconds(Player player);
    boolean hasPendingWarp(Player player);
    void cancelWarp(Player player);

    void reload();
}