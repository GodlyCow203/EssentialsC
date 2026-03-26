package net.godlycow.org.essc.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.database.Database;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HomeManager {

    private final EssentialsC plugin;
    private final HomeDatabase repository;
    private final TeleportHandler teleportHandler;

    public HomeManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.repository = new HomeDatabase(plugin);
        this.teleportHandler = new TeleportHandler(plugin, repository);
        plugin.debug("HomeManager initialized.");
    }

    public int getMaxHomes(Player player) {
        if (player.hasPermission("essentialsc.sethome.admin")
                || player.hasPermission("essentialsc.sethome.unlimited")) {
            return Integer.MAX_VALUE;
        }

        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("essentialsc.sethome." + i)) {
                return i;
            }
        }

        return plugin.getConfigManager().getMaxHomes();
    }

    public CompletableFuture<Integer> getHomeCount(UUID uuid) {
        return repository.getHomeCount(uuid);
    }

    public CompletableFuture<Boolean> homeExists(UUID uuid, String name) {
        return repository.homeExists(uuid, name);
    }

    public CompletableFuture<Boolean> setHome(Player player, String name, Location location) {
        return repository.save(player.getUniqueId(), name, location);
    }

    public CompletableFuture<Boolean> setHome(UUID uuid, String name, Location location) {
        return repository.save(uuid, name, location);
    }

    public CompletableFuture<Boolean> deleteHome(UUID uuid, String name) {
        return repository.delete(uuid, name);
    }

    public CompletableFuture<Home> getHome(UUID uuid, String name) {
        return repository.findOne(uuid, name);
    }

    public CompletableFuture<List<Home>> getHomes(UUID uuid) {
        return repository.findAll(uuid);
    }

    public CompletableFuture<Set<UUID>> getAllHomeOwners() {
        return repository.findAllOwners();
    }

    public boolean isOnCooldown(Player player) {
        return teleportHandler.isOnCooldown(player);
    }

    public long getRemainingCooldown(Player player) {
        return teleportHandler.getRemainingCooldown(player);
    }

    public boolean hasPendingTeleport(Player player) {
        return teleportHandler.hasPendingTeleport(player);
    }

    public void cancelTeleport(Player player) {
        teleportHandler.cancelTeleport(player);
    }

    public void startTeleport(Player player, Home home) {
        teleportHandler.startTeleport(player, home);
    }

    public Database getDatabase() {
        return repository.getDatabase();
    }

    public void reload() {
        plugin.debug("HomeManager configuration reloaded.");
    }

    public void shutdown() {
        teleportHandler.shutdown();
        repository.shutdown();
        plugin.debug("HomeManager shutdown complete.");
    }
}