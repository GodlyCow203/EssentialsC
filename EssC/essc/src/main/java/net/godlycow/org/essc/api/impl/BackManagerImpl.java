package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.event.back.Back;
import net.godlycow.org.essc.api.event.back.BackManager;
import net.godlycow.org.essc.api.event.back.start.BackWarmupStartEvent;
import net.godlycow.org.essc.api.event.back.teleport.BackTeleportEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BackManagerImpl implements BackManager {
    private final EssentialsC plugin;
    private final net.godlycow.org.essc.back.BackManager internal;

    private Field backLocationsField;
    private Field cooldownsField;
    private Field warmupTasksField;
    private Field isTeleportingField;
    private Field warmupField;
    private Field cooldownField;

    public BackManagerImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.internal = plugin.getBackManager();
        initReflection();
    }

    private void initReflection() {
        try {
            Class<?> internalClass = internal.getClass();

            backLocationsField = internalClass.getDeclaredField("backLocations");
            backLocationsField.setAccessible(true);

            cooldownsField = internalClass.getDeclaredField("cooldowns");
            cooldownsField.setAccessible(true);

            warmupTasksField = internalClass.getDeclaredField("warmupTasks");
            warmupTasksField.setAccessible(true);

            isTeleportingField = internalClass.getDeclaredField("isTeleporting");
            isTeleportingField.setAccessible(true);

            warmupField = internalClass.getDeclaredField("warmup");
            warmupField.setAccessible(true);

            cooldownField = internalClass.getDeclaredField("cooldown");
            cooldownField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            plugin.getLogger().severe("Failed to initialize BackManager reflection: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Location> getBackLocationsMap() {
        try {
            return (Map<UUID, Location>) backLocationsField.get(internal);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Long> getCooldownsMap() {
        try {
            return (Map<UUID, Long>) cooldownsField.get(internal);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, ?> getWarmupTasksMap() {
        try {
            return (Map<UUID, ?>) warmupTasksField.get(internal);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Set<UUID> getIsTeleportingSet() {
        try {
            return (Set<UUID>) isTeleportingField.get(internal);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private long getInternalWarmup() {
        try {
            return warmupField.getLong(internal);
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    private long getInternalCooldown() {
        try {
            return cooldownField.getLong(internal);
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    @Override
    public void setBackLocation(@NotNull Player player, @NotNull Location location) {
        if (internal == null) return;
        internal.setBackLocation(player, location);
    }

    @Override
    public @NotNull Optional<Back> getBackLocation(@NotNull Player player) {
        return getBackLocation(player.getUniqueId());
    }

    @Override
    public @NotNull Optional<Back> getBackLocation(@NotNull UUID uuid) {
        Map<UUID, Location> locations = getBackLocationsMap();
        if (locations == null) return Optional.empty();

        Location loc = locations.get(uuid);
        if (loc == null) return Optional.empty();

        return Optional.of(BackImpl.fromInternal(uuid, loc));
    }

    @Override
    public boolean hasBackLocation(@NotNull Player player) {
        return hasBackLocation(player.getUniqueId());
    }

    @Override
    public boolean hasBackLocation(@NotNull UUID uuid) {
        Map<UUID, Location> locations = getBackLocationsMap();
        if (locations == null) return false;
        return locations.containsKey(uuid);
    }

    @Override
    public void removeBackLocation(@NotNull Player player) {
        removeBackLocation(player.getUniqueId());
    }

    @Override
    public void removeBackLocation(@NotNull UUID uuid) {
        Map<UUID, Location> locations = getBackLocationsMap();
        if (locations != null) {
            locations.remove(uuid);
        }
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleport(@NotNull Player player) {
        if (internal == null) {
            return CompletableFuture.completedFuture(false);
        }

        if (!hasBackLocation(player)) {
            return CompletableFuture.completedFuture(false);
        }

        Optional<Back> backOpt = getBackLocation(player);
        if (backOpt.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        Back back = backOpt.get();

        BackTeleportEvent event = new BackTeleportEvent(player, back, BackTeleportEvent.TeleportCause.API);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            if (event.getCancelReason() != null) {
                player.sendMessage(event.getCancelReason());
            }
            return CompletableFuture.completedFuture(false);
        }

        long warmup = getWarmupSeconds();

        if (warmup > 0 && !player.hasPermission("essentialsc.back.admin")) {
            BackWarmupStartEvent warmupEvent = new BackWarmupStartEvent(player, back, warmup);
            Bukkit.getPluginManager().callEvent(warmupEvent);

            if (warmupEvent.isCancelled()) {
                return teleportInstantly(player);
            }

            warmup = warmupEvent.getWarmupSeconds();
        }

        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<Boolean> result = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    internal.teleportBack(player);
                    result.complete(true);
                } catch (Exception e) {
                    result.complete(false);
                }
            });

            return result.join();
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportInstantly(@NotNull Player player) {
        if (internal == null) {
            return CompletableFuture.completedFuture(false);
        }

        Optional<Back> backOpt = getBackLocation(player);
        if (backOpt.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        Back back = backOpt.get();
        Location target = back.toLocation();
        if (target == null || target.getWorld() == null) {
            return CompletableFuture.completedFuture(false);
        }

        cancelTeleport(player);

        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<Boolean> result = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    player.teleport(target);
                    result.complete(true);
                } catch (Exception e) {
                    result.complete(false);
                }
            });

            return result.join();
        });
    }

    @Override
    public boolean cancelTeleport(@NotNull Player player) {
        if (internal == null) return false;
        internal.cancelTeleport(player, "api");
        return true;
    }

    @Override
    public boolean hasPendingTeleport(@NotNull Player player) {
        Map<UUID, ?> warmupTasks = getWarmupTasksMap();
        if (warmupTasks == null) return false;
        return warmupTasks.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isOnCooldown(@NotNull Player player) {
        if (player.hasPermission("essentialsc.back.admin")) return false;

        long cooldown = getInternalCooldown();
        if (cooldown <= 0) return false;

        Map<UUID, Long> cooldowns = getCooldownsMap();
        if (cooldowns == null) return false;

        Long last = cooldowns.get(player.getUniqueId());
        if (last == null) return false;

        return System.currentTimeMillis() - last < (cooldown * 1000);
    }

    @Override
    public long getRemainingCooldown(@NotNull Player player) {
        if (player.hasPermission("essentialsc.back.admin")) return 0;

        long cooldown = getInternalCooldown();
        if (cooldown <= 0) return 0;

        Map<UUID, Long> cooldowns = getCooldownsMap();
        if (cooldowns == null) return 0;

        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = (last + (cooldown * 1000) - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    @Override
    public long getWarmupSeconds() {
        return getInternalWarmup();
    }

    @Override
    public long getCooldownSeconds() {
        return getInternalCooldown();
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("back.enabled", true);
    }

    @Override
    public void reload() {
        if (internal != null) {
            internal.reload();
        }
    }
}