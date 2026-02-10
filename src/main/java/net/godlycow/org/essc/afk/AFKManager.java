package net.godlycow.org.essc.afk;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AFKManager implements Listener {

    private final EssentialsC plugin;
    private final ConfigManager config;
    private final MiniMessage miniMessage;

    private final Map<UUID, Instant> lastActivity = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> afkStatus = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> afkStartTime = new ConcurrentHashMap<>();
    private final Map<UUID, org.bukkit.Location> afkLocations = new ConcurrentHashMap<>();

    private BukkitTask checkTask;
    private BukkitTask kickTask;

    public AFKManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.miniMessage = plugin.getMiniMessage();

        if (!config.isAfkEnabled()) {
            plugin.getLogger().info("AFK system is disabled in config");
            return;
        }

        startTasks();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.debug("AFK Manager initialized");
    }

    public void shutdown() {
        if (checkTask != null) {
            checkTask.cancel();
        }
        if (kickTask != null) {
            kickTask.cancel();
        }

        for (UUID uuid : new HashSet<>(afkStatus.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                setAFK(player, false, false);
            }
        }

        lastActivity.clear();
        afkStatus.clear();
        afkStartTime.clear();
        afkLocations.clear();
    }

    public void reload() {
        shutdown();
        if (config.isAfkEnabled()) {
            startTasks();
            plugin.debug("AFK Manager reloaded");
        }
    }

    private void startTasks() {
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkAFKStatus();
            }
        }.runTaskTimer(plugin, 100L, 100L);

        if (config.isAfkKickEnabled()) {
            kickTask = new BukkitRunnable() {
                @Override
                public void run() {
                    checkAFKKick();
                }
            }.runTaskTimer(plugin, 1200L, 1200L);
        }
    }

    private void checkAFKStatus() {
        long timeoutSeconds = config.getAfkTimeout();
        Instant now = Instant.now();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("essentialsc.afk.bypass.auto")) {
                continue;
            }

            UUID uuid = player.getUniqueId();
            Instant lastActive = lastActivity.getOrDefault(uuid, now);
            long secondsInactive = Duration.between(lastActive, now).getSeconds();

            boolean currentlyAFK = afkStatus.getOrDefault(uuid, false);

            if (!currentlyAFK && secondsInactive >= timeoutSeconds) {
                setAFK(player, true, true);
            } else if (currentlyAFK && secondsInactive < timeoutSeconds) {
                setAFK(player, false, true);
            }
        }
    }

    private void checkAFKKick() {
        if (!config.isAfkKickEnabled()) return;

        long kickTimeoutSeconds = config.getAfkKickTimeout();
        Instant now = Instant.now();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("essentialsc.afk.bypass.kick")) {
                continue;
            }

            UUID uuid = player.getUniqueId();
            if (!afkStatus.getOrDefault(uuid, false)) continue;

            Instant afkStart = afkStartTime.get(uuid);
            if (afkStart == null) continue;

            long afkDuration = Duration.between(afkStart, now).getSeconds();

            if (afkDuration >= kickTimeoutSeconds) {
                Component kickMessage = plugin.getLanguageManager().get(player, "afk.kick.message");
                player.kick(kickMessage);

                if (config.isAfkBroadcastEnabled()) {
                    broadcastKick(player);
                }

                plugin.debug("Kicked " + player.getName() + " for being AFK too long (" + afkDuration + "s)");
            }
        }
    }

    public void setAFK(Player player, boolean afk, boolean broadcast) {
        UUID uuid = player.getUniqueId();
        boolean wasAFK = afkStatus.getOrDefault(uuid, false);

        if (wasAFK == afk) return;

        afkStatus.put(uuid, afk);

        if (afk) {
            afkStartTime.put(uuid, Instant.now());

            if (config.isAfkFreezePlayer()) {
                afkLocations.put(uuid, player.getLocation().clone());
            }

            if (config.isAfkTitleEnabled()) {
                Title.Times times = Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofDays(1),
                        Duration.ofMillis(500)
                );

                Title title = Title.title(
                        miniMessage.deserialize(config.getAfkTitle()),
                        miniMessage.deserialize(config.getAfkSubtitle()),
                        times
                );
                player.showTitle(title);
            }

            if (broadcast && config.isAfkBroadcastEnabled()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", player.getName());

                Component message = plugin.getLanguageManager().get(player, "afk.broadcast.enter", placeholders);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.getUniqueId().equals(uuid)) {
                        online.sendMessage(message);
                    }
                }
            }

            player.sendMessage(plugin.getLanguageManager().get(player, "afk.self.enter"));
            plugin.debug(player.getName() + " is now AFK");

        } else {
            afkStartTime.remove(uuid);
            afkLocations.remove(uuid);

            player.clearTitle();

            if (broadcast && config.isAfkBroadcastEnabled()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", player.getName());

                Component message = plugin.getLanguageManager().get(player, "afk.broadcast.leave", placeholders);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.getUniqueId().equals(uuid)) {
                        online.sendMessage(message);
                    }
                }
            }

            player.sendMessage(plugin.getLanguageManager().get(player, "afk.self.leave"));
            plugin.debug(player.getName() + " is no longer AFK");
        }

        if (config.isAfkTabPlaceholderEnabled()) {
            updatePlayerListName(player);
        }
    }

    public void toggleAFK(Player player) {
        UUID uuid = player.getUniqueId();
        boolean currentlyAFK = afkStatus.getOrDefault(uuid, false);
        setAFK(player, !currentlyAFK, true);
        updateActivity(player);
    }

    public boolean isAFK(Player player) {
        return afkStatus.getOrDefault(player.getUniqueId(), false);
    }

    public boolean isAFK(UUID uuid) {
        return afkStatus.getOrDefault(uuid, false);
    }

    public Instant getAFKStartTime(Player player) {
        return afkStartTime.get(player.getUniqueId());
    }

    public long getAFKDurationSeconds(Player player) {
        Instant start = afkStartTime.get(player.getUniqueId());
        if (start == null) return 0;
        return Duration.between(start, Instant.now()).getSeconds();
    }

    public String getAFKDurationFormatted(Player player) {
        long seconds = getAFKDurationSeconds(player);
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    public Set<Player> getAFKPlayers() {
        Set<Player> afkPlayers = new HashSet<>();
        for (UUID uuid : afkStatus.keySet()) {
            if (afkStatus.get(uuid)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    afkPlayers.add(player);
                }
            }
        }
        return afkPlayers;
    }

    public int getAFKCount() {
        return getAFKPlayers().size();
    }

    public void updateActivity(Player player) {
        if (player == null || !player.isOnline()) return;

        UUID uuid = player.getUniqueId();
        lastActivity.put(uuid, Instant.now());

        if (afkStatus.getOrDefault(uuid, false)) {
            setAFK(player, false, true);
        }
    }

    public void updatePlayerListName(Player player) {
        if (!config.isAfkTabPlaceholderEnabled()) return;

        UUID uuid = player.getUniqueId();
        boolean afk = afkStatus.getOrDefault(uuid, false);

        String prefix = afk ? config.getAfkTabPlaceholder() : "";
        String name = player.getName();

        Component listName = miniMessage.deserialize(prefix + name);
        player.playerListName(listName);
    }

    public boolean isCommandBlocked(String command) {
        String lowerCmd = command.toLowerCase();
        for (String blocked : config.getAfkBlockedCommands()) {
            if (lowerCmd.startsWith(blocked.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void broadcastKick(Player player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("duration", String.valueOf(config.getAfkKickTimeout() / 60));

        Component message = plugin.getLanguageManager().get(player, "afk.kick.broadcast", placeholders);

        Bukkit.broadcast(message);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (isAFK(player) && config.isAfkFreezePlayer()) {
            org.bukkit.Location afkLoc = afkLocations.get(player.getUniqueId());
            if (afkLoc != null) {
                if (event.getTo() != null &&
                        (event.getFrom().getX() != event.getTo().getX() ||
                                event.getFrom().getY() != event.getTo().getY() ||
                                event.getFrom().getZ() != event.getTo().getZ())) {

                    event.setTo(afkLoc);
                    return;
                }
            }
        }

        if (event.getFrom().distanceSquared(event.getTo()) > 0.01) {
            updateActivity(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0];

        if (isAFK(player) && isCommandBlocked(command)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageManager().get(player, "afk.error.blocked_command"));
            return;
        }

        updateActivity(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();

        if (isAFK(player) && config.isAfkPreventPickup()) {
            event.setCancelled(true);
            return;
        }

        updateActivity(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (isAFK(player) && config.isAfkPreventDamage()) {
            event.setCancelled(true);
            return;
        }

        updateActivity(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            updateActivity(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        if (isAFK(player) && config.isAfkPreventMobTarget()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        lastActivity.put(player.getUniqueId(), Instant.now());
        afkStatus.put(player.getUniqueId(), false);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                updatePlayerListName(player);
            }
        }, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        lastActivity.remove(uuid);
        afkStatus.remove(uuid);
        afkStartTime.remove(uuid);
        afkLocations.remove(uuid);
    }
}