package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.WarpApi;
import net.godlycow.org.essc.api.warp.WarpEntry;
import net.godlycow.org.essc.warp.Warp;
import net.godlycow.org.essc.warp.WarpManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WarpApiImpl implements WarpApi {

    private final WarpManager manager;

    public WarpApiImpl(WarpManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean isSystemEnabled() {
        return manager.isSystemEnabled();
    }

    @Override
    public Optional<WarpEntry> getWarp(String name) {
        return Optional.ofNullable(manager.getWarp(name)).map(this::mapToEntry);
    }

    @Override
    public List<WarpEntry> getAllWarps() {
        return manager.getAllWarps().stream()
                .map(this::mapToEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<WarpEntry> getVisibleWarps() {
        return manager.getVisibleWarps().stream()
                .map(this::mapToEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<WarpEntry> getWarpsByCategory(String category) {
        return manager.getWarpsByCategory(category).stream()
                .map(this::mapToEntry)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getCategories() {
        return manager.getCategories();
    }

    @Override
    public boolean warpExists(String name) {
        return manager.warpExists(name);
    }

    @Override
    public CompletableFuture<Boolean> createWarp(String name, Location location) {
        return CompletableFuture.supplyAsync(() -> manager.createWarp(name, location));
    }

    @Override
    public CompletableFuture<Boolean> deleteWarp(String name) {
        return CompletableFuture.supplyAsync(() -> manager.deleteWarp(name));
    }

    @Override
    public CompletableFuture<Boolean> updateWarp(WarpEntry warp) {
        return CompletableFuture.supplyAsync(() -> {
            Warp existing = manager.getWarp(warp.name());
            if (existing == null) return false;

            existing.setLocation(warp.location());
            existing.setPermission(warp.permission());
            existing.setCost(warp.cost());
            existing.setHidden(warp.hidden());
            existing.setDescription(warp.description());
            existing.setCategory(warp.category());

            return manager.updateWarp(existing);
        });
    }

    @Override
    public long getRemainingCooldown(UUID uuid) {
        return manager.getRemainingCooldown(uuid);
    }

    @Override
    public CompletableFuture<Integer> getWarpUsage(UUID uuid, String warpName) {
        return CompletableFuture.supplyAsync(() -> manager.getWarpUsage(uuid, warpName));
    }

    @Override
    public void reload() {
        manager.reload();
    }

    private WarpEntry mapToEntry(Warp w) {
        return new WarpEntry(
                w.getName(),
                w.getLocation(),
                w.getPermission(),
                w.getCost(),
                w.isHidden(),
                w.getDescription(),
                w.getCategory()
        );
    }
}