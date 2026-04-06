package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.HomeApi;
import net.godlycow.org.essc.api.home.HomeEntry;
import net.godlycow.org.essc.home.Home;
import net.godlycow.org.essc.home.HomeManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HomeApiImpl implements HomeApi {

    private final HomeManager manager;

    public HomeApiImpl(HomeManager manager) {
        this.manager = manager;
    }

    @Override
    public int getMaxHomes(Player player) {
        return manager.getMaxHomes(player);
    }

    @Override
    public CompletableFuture<Integer> getHomeCount(UUID uuid) {
        return manager.getHomeCount(uuid);
    }

    @Override
    public CompletableFuture<Boolean> homeExists(UUID uuid, String name) {
        return manager.homeExists(uuid, name);
    }

    @Override
    public CompletableFuture<Boolean> setHome(Player player, String name, Location location) {
        return manager.setHome(player, name, location);
    }

    @Override
    public CompletableFuture<Boolean> setHome(UUID uuid, String name, Location location) {
        return manager.setHome(uuid, name, location);
    }

    @Override
    public CompletableFuture<Boolean> deleteHome(UUID uuid, String name) {
        return manager.deleteHome(uuid, name);
    }

    @Override
    public CompletableFuture<HomeEntry> getHome(UUID uuid, String name) {
        return manager.getHome(uuid, name).thenApply(h -> h != null ? mapToEntry(h) : null);
    }

    @Override
    public CompletableFuture<List<HomeEntry>> getHomes(UUID uuid) {
        return manager.getHomes(uuid).thenApply(list ->
                list.stream().map(this::mapToEntry).collect(Collectors.toList())
        );
    }

    @Override
    public CompletableFuture<Set<UUID>> getAllHomeOwners() {
        return manager.getAllHomeOwners();
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return manager.isOnCooldown(player);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        return manager.getRemainingCooldown(player);
    }

    @Override
    public boolean hasPendingTeleport(Player player) {
        return manager.hasPendingTeleport(player);
    }

    @Override
    public void cancelTeleport(Player player) {
        manager.cancelTeleport(player);
    }

    @Override
    public void startTeleport(Player player, HomeEntry home) {
        Home internal = new Home(home.owner(), home.name(), home.world(),
                home.x(), home.y(), home.z(), home.yaw(), home.pitch(), home.createdAt());
        manager.startTeleport(player, internal);
    }

    @Override
    public void openGui(Player player) {
        if (manager.getClass().getDeclaredMethods().length > 0) {
            try {
                var guiManager = manager.getClass().getDeclaredField("guiManager").get(manager);
                if (guiManager != null) {
                    guiManager.getClass().getMethod("openHomeList", Player.class).invoke(guiManager, player);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean isGuiMode() {
        try {
            var guiManager = manager.getClass().getDeclaredField("guiManager").get(manager);
            if (guiManager != null) {
                return (boolean) guiManager.getClass().getMethod("isGuiMode").invoke(guiManager);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void reload() {
        manager.reload();
    }

    private HomeEntry mapToEntry(Home h) {
        return new HomeEntry(
                h.getOwner(),
                h.getName(),
                h.getWorld(),
                h.getX(),
                h.getY(),
                h.getZ(),
                h.getYaw(),
                h.getPitch(),
                h.getCreatedAt()
        );
    }
}