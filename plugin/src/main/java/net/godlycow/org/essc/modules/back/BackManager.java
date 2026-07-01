package net.godlycow.org.essc.modules.back;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.util.SafeLocationFinder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BackManager implements Listener {
    private final EssentialsC plugin;

    private final Map<UUID, Location> backLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> deathCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> warmupTasks = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> deathWarmupTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Location> pendingUnsafeBack = new ConcurrentHashMap<>();
    private final Map<UUID, Location> pendingUnsafeDeathBack = new ConcurrentHashMap<>();

    private long warmup;
    private long cooldown;
    private boolean particles;
    private boolean sounds;
    private boolean cancelOnMovement;

    public BackManager(EssentialsC plugin) {
        this.plugin = plugin;
        loadConfig();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.debug("BackManager initialized");
    }

    public void reload() {
        loadConfig();
        plugin.debug("Back configuration reloaded");
    }

    private void loadConfig() {
        var cfg = plugin.getConfigManager();
        this.warmup = cfg.getBackWarmup();
        this.cooldown = cfg.getBackCooldown();
        this.particles = cfg.isBackParticles();
        this.sounds = cfg.isBackSounds();
        this.cancelOnMovement = cfg.isBackCancelOnMovement();
    }

    public void setBackLocation(Player player, Location location) {
        if (location == null || location.getWorld() == null) return;
        backLocations.put(player.getUniqueId(), location.clone());

        if (plugin.getUserManager() != null) {
            plugin.getUserManager().getLocationManager().setBackLocation(player.getUniqueId(), location);
        }

        plugin.debug("Set back location for " + player.getName() + ": " +
                location.getWorld().getName() + " (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");
    }

    public boolean hasBackLocation(Player player) {
        return backLocations.containsKey(player.getUniqueId());
    }

    public void removeBackLocation(Player player) {
        backLocations.remove(player.getUniqueId());
    }

    public void teleportBack(Player player, boolean confirm) {
        if (warmupTasks.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "back.already_pending"));
            return;
        }

        if (confirm) {
            Location pending = pendingUnsafeBack.remove(player.getUniqueId());
            if (pending == null) {
                player.sendMessage(plugin.getLanguageManager().get(player, "back.no_pending_confirmation"));
                return;
            }

            if (pending.getWorld() == null) {
                player.sendMessage(plugin.getLanguageManager().get(player, "back.world_unloaded"));
                return;
            }

            completeUnsafeTeleport(player, pending, false);
            return;
        }

        if (!hasBackLocation(player)) {
            player.sendMessage(plugin.getLanguageManager().get(player, "back.no_location"));
            return;
        }

        if (isOnCooldown(player)) {
            player.sendMessage(plugin.getLanguageManager().get(player, "back.cooldown",
                    Map.of("seconds", String.valueOf(getRemainingCooldown(player)))));
            return;
        }

        Location target = backLocations.get(player.getUniqueId());
        if (target.getWorld() == null) {
            player.sendMessage(plugin.getLanguageManager().get(player, "back.world_unloaded"));
            return;
        }

        if (warmup <= 0 || player.hasPermission("essentialsc.back.admin")) {
            completeTeleport(player, target, false);
            return;
        }

        player.sendMessage(plugin.getLanguageManager().get(player, "back.pending",
                Map.of("seconds", String.valueOf(warmup))));

        ScheduledTask task = player.getScheduler().runDelayed(plugin, task1 -> {
            completeTeleport(player, target, false);
        }, null, warmup * 20L);

        warmupTasks.put(player.getUniqueId(), task);
    }

    public void teleportDeathBack(Player player, boolean confirm) {
        if (deathWarmupTasks.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "dback.already_pending"));
            return;
        }

        if (confirm) {
            Location pending = pendingUnsafeDeathBack.remove(player.getUniqueId());
            if (pending == null) {
                player.sendMessage(plugin.getLanguageManager().get(player, "dback.no_pending_confirmation"));
                return;
            }

            if (pending.getWorld() == null) {
                player.sendMessage(plugin.getLanguageManager().get(player, "dback.world_unloaded"));
                return;
            }

            completeUnsafeTeleport(player, pending, true);
            return;
        }

        Location target = deathLocations.get(player.getUniqueId());
        if (target == null) {
            player.sendMessage(plugin.getLanguageManager().get(player, "dback.no_location"));
            return;
        }

        if (isOnDeathCooldown(player)) {
            player.sendMessage(plugin.getLanguageManager().get(player, "dback.cooldown",
                    Map.of("seconds", String.valueOf(getRemainingDeathCooldown(player)))));
            return;
        }

        if (target.getWorld() == null) {
            player.sendMessage(plugin.getLanguageManager().get(player, "dback.world_unloaded"));
            return;
        }

        if (warmup <= 0 || player.hasPermission("essentialsc.dback.admin")) {
            completeTeleport(player, target, true);
            return;
        }

        player.sendMessage(plugin.getLanguageManager().get(player, "dback.pending",
                Map.of("seconds", String.valueOf(warmup))));

        ScheduledTask task = player.getScheduler().runDelayed(plugin, task1 -> {
            completeTeleport(player, target, true);
        }, null, warmup * 20L);

        deathWarmupTasks.put(player.getUniqueId(), task);
    }

    private void completeTeleport(Player player, Location location, boolean isDeath) {
        if (isDeath) {
            deathWarmupTasks.remove(player.getUniqueId());
        } else {
            warmupTasks.remove(player.getUniqueId());
        }

        if (!player.isOnline()) return;

        Location safeLocation = SafeLocationFinder.findSafe(location);

        if (safeLocation == null) {
            String key = isDeath ? "dback.unsafe_location" : "back.unsafe_location";
            if (isDeath) {
                pendingUnsafeDeathBack.put(player.getUniqueId(), location.clone());
            } else {
                pendingUnsafeBack.put(player.getUniqueId(), location.clone());
            }
            player.sendMessage(plugin.getLanguageManager().get(player, key));
            plugin.debug("No safe location found for " + player.getName() + " near stored location");
            return;
        }

        boolean adjusted = !safeLocation.getBlock().equals(location.getBlock());
        Location preBackLocation = player.getLocation().clone();

        plugin.teleportHelper().teleportAsync(player, safeLocation, false).thenAccept(success -> {
            if (!success) return;

            if (isDeath) {
                if (cooldown > 0) {
                    deathCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                }
                String key = adjusted ? "dback.success_adjusted" : "dback.success";
                player.sendMessage(plugin.getLanguageManager().get(player, key));
                plugin.debug("Teleported " + player.getName() + " to death location" + (adjusted ? " (adjusted for safety)" : ""));
            } else {
                backLocations.put(player.getUniqueId(), preBackLocation);
                if (cooldown > 0) {
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                }
                String key = adjusted ? "back.success_adjusted" : "back.success";
                player.sendMessage(plugin.getLanguageManager().get(player, key));
                plugin.debug("Teleported " + player.getName() + " to back location" + (adjusted ? " (adjusted for safety)" : ""));
            }

            if (particles) {
                safeLocation.getWorld().spawnParticle(Particle.PORTAL, safeLocation, 50, 0.5, 1, 0.5);
            }
            if (sounds) {
                player.playSound(safeLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        });
    }

    private void completeUnsafeTeleport(Player player, Location location, boolean isDeath) {
        Location preBackLocation = player.getLocation().clone();

        plugin.teleportHelper().teleportAsync(player, location, false).thenAccept(success -> {
            if (!success) return;

            if (isDeath) {
                if (cooldown > 0) {
                    deathCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                }
                player.sendMessage(plugin.getLanguageManager().get(player, "dback.success_unsafe"));
                plugin.debug("Teleported " + player.getName() + " to death location (unsafe, confirmed)");
            } else {
                backLocations.put(player.getUniqueId(), preBackLocation);
                if (cooldown > 0) {
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                }
                player.sendMessage(plugin.getLanguageManager().get(player, "back.success_unsafe"));
                plugin.debug("Teleported " + player.getName() + " to back location (unsafe, confirmed)");
            }

            if (particles) {
                location.getWorld().spawnParticle(Particle.PORTAL, location, 50, 0.5, 1, 0.5);
            }
            if (sounds) {
                player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        });
    }

    public void cancelTeleport(Player player, String reason) {
        String msgKey;

        ScheduledTask task = warmupTasks.remove(player.getUniqueId());
        ScheduledTask deathTask = deathWarmupTasks.remove(player.getUniqueId());

        if (task != null) task.cancel();
        if (deathTask != null) deathTask.cancel();

        if (task == null && deathTask == null) return;

        msgKey = switch (reason) {
            case "move" -> "back.cancelled.move";
            default -> "back.cancelled";
        };

        player.sendMessage(plugin.getLanguageManager().get(player, msgKey));
        plugin.debug("Cancelled back/dback teleport for " + player.getName() + " (" + reason + ")");
    }

    private boolean isOnCooldown(Player player) {
        if (player.hasPermission("essentialsc.back.admin")) return false;
        if (cooldown <= 0) return false;
        Long last = cooldowns.get(player.getUniqueId());
        if (last == null) return false;
        return System.currentTimeMillis() - last < (cooldown * 1000);
    }

    private long getRemainingCooldown(Player player) {
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        return Math.max(0, (last + (cooldown * 1000) - System.currentTimeMillis()) / 1000);
    }

    private boolean isOnDeathCooldown(Player player) {
        if (player.hasPermission("essentialsc.dback.admin")) return false;
        if (cooldown <= 0) return false;
        Long last = deathCooldowns.get(player.getUniqueId());
        if (last == null) return false;
        return System.currentTimeMillis() - last < (cooldown * 1000);
    }

    private long getRemainingDeathCooldown(Player player) {
        long last = deathCooldowns.getOrDefault(player.getUniqueId(), 0L);
        return Math.max(0, (last + (cooldown * 1000) - System.currentTimeMillis()) / 1000);
    }

//    public boolean hasBackLocation(UUID uuid) {
//        return backLocations.containsKey(uuid);
//    }
//
//    public boolean hasDeathLocation(UUID uuid) {
//        return deathLocations.containsKey(uuid);
//    }
//
//    public Optional<Location> getBackLocation(UUID uuid) {
//        Location loc = backLocations.get(uuid);
//        return Optional.ofNullable(loc != null ? loc.clone() : null);
//    }
//
//    public Optional<Location> getDeathLocation(UUID uuid) {
//        Location loc = deathLocations.get(uuid);
//        return Optional.ofNullable(loc != null ? loc.clone() : null);
//    }
//
//    public boolean hasPendingTeleport(UUID uuid) {
//        return warmupTasks.containsKey(uuid) || deathWarmupTasks.containsKey(uuid);
//    }
//
//    public boolean isOnCooldown(UUID uuid) {
//        Player player = plugin.getServer().getPlayer(uuid);
//        if (player != null && player.hasPermission("essentialsc.back.admin")) return false;
//        if (cooldown <= 0) return false;
//        Long last = cooldowns.get(uuid);
//        if (last == null) return false;
//        return System.currentTimeMillis() - last < (cooldown * 1000);
//    }
//
//    public long getRemainingCooldown(UUID uuid) {
//        Player player = plugin.getServer().getPlayer(uuid);
//        if (player != null && player.hasPermission("essentialsc.back.admin")) return 0;
//        if (cooldown <= 0) return 0;
//        long last = cooldowns.getOrDefault(uuid, 0L);
//        return Math.max(0, (last + (cooldown * 1000) - System.currentTimeMillis()) / 1000);
//    }
//
//    public long getWarmupSeconds() {
//        return warmup;
//    }
//
//    public long getCooldownSeconds() {
//        return cooldown;
//    }
//
//    public boolean isParticlesEnabled() {
//        return particles;
//    }
//
//    public boolean isSoundsEnabled() {
//        return sounds;
//    }
//
//    public boolean isCancelOnMovementEnabled() {
//        return cancelOnMovement;
//    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLoc = player.getLocation().clone();
        deathLocations.put(player.getUniqueId(), deathLoc);
        setBackLocation(player, deathLoc);

        if (plugin.getUserManager() != null) {
            plugin.getUserManager().getLocationManager().setDeathLocation(player.getUniqueId(), deathLoc);
        }

        plugin.debug("Stored death location for " + player.getName());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!cancelOnMovement) return;

        Player player = event.getPlayer();
        boolean hasBack = warmupTasks.containsKey(player.getUniqueId());
        boolean hasDBack = deathWarmupTasks.containsKey(player.getUniqueId());
        if (!hasBack && !hasDBack) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;
        if (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        cancelTeleport(player, "move");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getUserManager() != null) {
            plugin.getUserManager().getLocationManager().setLogoutLocation(player.getUniqueId(), player.getLocation());
        }

        ScheduledTask task = warmupTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();

        ScheduledTask deathTask = deathWarmupTasks.remove(player.getUniqueId());
        if (deathTask != null) deathTask.cancel();

        backLocations.remove(player.getUniqueId());
        deathLocations.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());
        deathCooldowns.remove(player.getUniqueId());
        pendingUnsafeBack.remove(player.getUniqueId());
        pendingUnsafeDeathBack.remove(player.getUniqueId());

        plugin.debug("Cleared back data for " + player.getName());
    }

    public void shutdown() {
        warmupTasks.values().forEach(ScheduledTask::cancel);
        deathWarmupTasks.values().forEach(ScheduledTask::cancel);
        warmupTasks.clear();
        deathWarmupTasks.clear();
        backLocations.clear();
        deathLocations.clear();
        cooldowns.clear();
        deathCooldowns.clear();
        pendingUnsafeBack.clear();
        pendingUnsafeDeathBack.clear();
    }
}