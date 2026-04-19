package net.godlycow.org.essc.back;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.softwares.SchedulerTask;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BackManager implements Listener {
    private final EssentialsC plugin;

    private final Map<UUID, Location> backLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, SchedulerTask> warmupTasks = new ConcurrentHashMap<>();

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
        plugin.debug("Set back location for " + player.getName() + ": " +
                location.getWorld().getName() + " (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");
    }

    public boolean hasBackLocation(Player player) {
        return backLocations.containsKey(player.getUniqueId());
    }

    public void removeBackLocation(Player player) {
        backLocations.remove(player.getUniqueId());
    }

    public void teleportBack(Player player) {
        if (warmupTasks.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "back.already_pending"));
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
            completeTeleport(player, target);
            return;
        }

        player.sendMessage(plugin.getLanguageManager().get(player, "back.pending",
                Map.of("seconds", String.valueOf(warmup))));

        SchedulerTask task = plugin.getEssScheduler().runForEntityLater(player, () -> {
            completeTeleport(player, target);
        }, warmup * 20L);

        warmupTasks.put(player.getUniqueId(), task);
    }

    private void completeTeleport(Player player, Location location) {
        warmupTasks.remove(player.getUniqueId());

        if (!player.isOnline()) return;

        plugin.getEssScheduler().teleportAsync(player, location, false).thenAccept(success -> {
            if (!success) return;

            if (cooldown > 0) {
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }

            if (particles) {
                location.getWorld().spawnParticle(Particle.PORTAL, location, 50, 0.5, 1, 0.5);
            }
            if (sounds) {
                player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }

            player.sendMessage(plugin.getLanguageManager().get(player, "back.success"));
            plugin.debug("Teleported " + player.getName() + " to back location");
        });
    }

    public void cancelTeleport(Player player, String reason) {
        SchedulerTask task = warmupTasks.remove(player.getUniqueId());
        if (task == null) return;

        task.cancel();

        String msgKey = switch (reason) {
            case "move" -> "back.cancelled.move";
            default -> "back.cancelled";
        };

        player.sendMessage(plugin.getLanguageManager().get(player, msgKey));
        plugin.debug("Cancelled back teleport for " + player.getName() + " (" + reason + ")");
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

    public boolean hasBackLocation(UUID uuid) {
        return backLocations.containsKey(uuid);
    }

    public Optional<Location> getBackLocation(UUID uuid) {
        Location loc = backLocations.get(uuid);
        return Optional.ofNullable(loc != null ? loc.clone() : null);
    }

    public boolean hasPendingTeleport(UUID uuid) {
        return warmupTasks.containsKey(uuid);
    }

    public boolean isOnCooldown(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.hasPermission("essentialsc.back.admin")) return false;
        if (cooldown <= 0) return false;
        Long last = cooldowns.get(uuid);
        if (last == null) return false;
        return System.currentTimeMillis() - last < (cooldown * 1000);
    }

    public long getRemainingCooldown(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.hasPermission("essentialsc.back.admin")) return 0;
        if (cooldown <= 0) return 0;
        long last = cooldowns.getOrDefault(uuid, 0L);
        return Math.max(0, (last + (cooldown * 1000) - System.currentTimeMillis()) / 1000);
    }

    public long getWarmupSeconds() {
        return warmup;
    }

    public long getCooldownSeconds() {
        return cooldown;
    }

    public boolean isParticlesEnabled() {
        return particles;
    }

    public boolean isSoundsEnabled() {
        return sounds;
    }

    public boolean isCancelOnMovementEnabled() {
        return cancelOnMovement;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        setBackLocation(player, player.getLocation());
        plugin.debug("Stored death location for " + player.getName());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!cancelOnMovement) return;

        Player player = event.getPlayer();
        if (!warmupTasks.containsKey(player.getUniqueId())) return;

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

        SchedulerTask task = warmupTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();

        backLocations.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());

        plugin.debug("Cleared back data for " + player.getName());
    }

    public void shutdown() {
        warmupTasks.values().forEach(SchedulerTask::cancel);
        warmupTasks.clear();
        backLocations.clear();
        cooldowns.clear();
    }
}