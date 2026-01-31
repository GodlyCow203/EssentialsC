package net.godlycow.org.essc.back;

import net.godlycow.org.essc.EssentialsC;
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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BackManager implements Listener {
    private final EssentialsC plugin;

    private final Map<UUID, Location> backLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> warmupTasks = new ConcurrentHashMap<>();
    private final Set<UUID> isTeleporting = Collections.newSetFromMap(new ConcurrentHashMap<>());

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
        var config = plugin.getConfig();
        this.warmup = config.getLong("back.warmup", 0);
        this.cooldown = config.getLong("back.cooldown", 0);
        this.particles = config.getBoolean("back.particles", true);
        this.sounds = config.getBoolean("back.sounds", true);
        this.cancelOnMovement = config.getBoolean("back.cancel-on-movement", true);
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

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            completeTeleport(player, target);
        }, warmup * 20L);

        warmupTasks.put(player.getUniqueId(), task);
    }

    private void completeTeleport(Player player, Location location) {
        warmupTasks.remove(player.getUniqueId());

        if (!player.isOnline()) return;

        isTeleporting.add(player.getUniqueId());

        player.teleport(location);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isTeleporting.remove(player.getUniqueId());
        }, 1L);

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
    }

    public void cancelTeleport(Player player, String reason) {
        BukkitTask task = warmupTasks.remove(player.getUniqueId());
        if (task == null) return;

        task.cancel();

        String msgKey = switch(reason) {
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        setBackLocation(player, player.getLocation());
        plugin.debug("Stored death location for " + player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (isTeleporting.contains(player.getUniqueId())) {
            return;
        }

        setBackLocation(player, event.getFrom());
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

        BukkitTask task = warmupTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();

        backLocations.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());
        isTeleporting.remove(player.getUniqueId());

        plugin.debug("Cleared back data for " + player.getName());
    }

    public void shutdown() {
        warmupTasks.values().forEach(BukkitTask::cancel);
        warmupTasks.clear();
        backLocations.clear();
        cooldowns.clear();
        isTeleporting.clear();
    }
}