package net.godlycow.org.essc.api.home;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface HomeManager {
    boolean isHomeSystemEnabled();

    CompletableFuture<Home> fetchHome(UUID owner, String name);
    CompletableFuture<List<Home>> fetchHomes(UUID owner);
    CompletableFuture<Boolean> homeExists(UUID owner, String name);
    CompletableFuture<Integer> getHomeCount(UUID owner);
    CompletableFuture<Boolean> setHome(Player player, String name, Location location);
    CompletableFuture<Boolean> setHome(UUID owner, String name, Location location);
    CompletableFuture<Boolean> deleteHome(UUID owner, String name);
    int getMaxHomes(Player player);

    Collection<String> getCachedHomeNames(UUID owner);
    void clearCache(UUID owner);

    boolean isOnCooldown(Player player);
    long getRemainingCooldownSeconds(Player player);
    boolean hasPendingTeleport(Player player);
    void cancelTeleport(Player player);
    void startTeleport(Player player, Home home);

    void reload();
}
