package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.HomeApi;
import net.godlycow.org.essc.api.home.Home;
import net.godlycow.org.essc.home.HomeManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HomeApiImpl implements HomeApi {

    private final HomeManager manager;

    public HomeApiImpl(HomeManager manager) {
        this.manager = manager;
    }

    @Override
    public CompletableFuture<Home> getHome(UUID uuid, String name) {
        return manager.getHome(uuid, name).thenApply(h -> {
            if (h == null) return null;
            return toApiHome(h);
        });
    }

    @Override
    public CompletableFuture<List<Home>> getHomes(UUID uuid) {
        return manager.getHomes(uuid).thenApply(list ->
                list.stream().map(this::toApiHome).collect(Collectors.toList()));
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
    public int getMaxHomes(Player player) {
        return manager.getMaxHomes(player);
    }

    @Override
    public CompletableFuture<Boolean> setHome(Player player, String name, Location location) {
        return manager.setHome(player, name, location);
    }

    @Override
    public CompletableFuture<Boolean> deleteHome(UUID uuid, String name) {
        return manager.deleteHome(uuid, name);
    }

    @Override
    public void startTeleport(Player player, Home home) {
        manager.startTeleport(player, toManagerHome(home));
    }

    @Override
    public void cancelTeleport(Player player) {
        manager.cancelTeleport(player);
    }

    @Override
    public boolean hasPendingTeleport(Player player) {
        return manager.hasPendingTeleport(player);
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return manager.isOnCooldown(player);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        return manager.getRemainingCooldown(player);
    }


    private Home toApiHome(net.godlycow.org.essc.home.Home h) {
        return new Home(
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

    private net.godlycow.org.essc.home.Home toManagerHome(Home h) {
        return new net.godlycow.org.essc.home.Home(
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