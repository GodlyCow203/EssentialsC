package net.godlycow.org.essc.scoreboard;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class ScoreboardManager implements Listener {
    private final EssentialsC plugin;
    private final PlaceholderProcessor processor;
    private final Map<UUID, PlayerScoreboard> boards = new ConcurrentHashMap<>();
    private final Set<UUID> disabledPlayers = ConcurrentHashMap.newKeySet();

    private ScoreboardConfig config;
    private BukkitTask updateTask;
    private File dataFile;
    private YamlConfiguration dataConfig;
    private final AtomicBoolean reloading = new AtomicBoolean(false);
    private final Object lock = new Object();

    private final Map<UUID, ProcessedData> placeholderCache = new ConcurrentHashMap<>();
    private final long CACHE_TTL_MS = 1000;

    private record ProcessedData(String title, List<String> lines, long timestamp) {}

    public ScoreboardManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.processor = new PlaceholderProcessor();
        this.dataFile = new File(plugin.getDataFolder(), "scoreboards/data.yml");

        loadConfig();
        loadDisabled();

        if (config.isEnabled()) {
            start();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void loadConfig() {
        this.config = new ScoreboardConfig(plugin.getConfig().getConfigurationSection("scoreboard"));
    }

    public void reload() {
        if (!reloading.compareAndSet(false, true)) {
            plugin.getLogger().warning("Scoreboard reload already in progress!");
            return;
        }

        try {
            synchronized (lock) {
                stop();

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                placeholderCache.clear();
                loadConfig();

                Iterator<Map.Entry<UUID, PlayerScoreboard>> it = boards.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, PlayerScoreboard> entry = it.next();
                    try {
                        entry.getValue().destroy();
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error destroying scoreboard: " + e.getMessage());
                    }
                    it.remove();
                }

                if (config.isEnabled()) {
                    Bukkit.getOnlinePlayers().forEach(this::addPlayer);
                    start();
                    plugin.getLogger().info("Scoreboard reloaded successfully");
                } else {
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        try {
                            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                        } catch (Exception ignored) {}
                    });
                    plugin.getLogger().info("Scoreboard disabled in config");
                }
            }
        } finally {
            reloading.set(false);
        }
    }

    private void start() {
        Bukkit.getOnlinePlayers().forEach(this::addPlayer);
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll,
                10L, config.getUpdateInterval());
    }

    private void addPlayer(Player player) {
        if (disabledPlayers.contains(player.getUniqueId())) return;
        if (!player.isOnline()) return;

        try {
            PlayerScoreboard existing = boards.remove(player.getUniqueId());
            if (existing != null) {
                existing.hide(player);
                existing.destroy();
            }

            PlayerScoreboard board = new PlayerScoreboard(player, config);
            boards.put(player.getUniqueId(), board);
            board.show(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add scoreboard for " + player.getName(), e);
        }
    }

    public void stop() {
        synchronized (lock) {
            if (updateTask != null) {
                updateTask.cancel();
                updateTask = null;
            }

            Iterator<Map.Entry<UUID, PlayerScoreboard>> it = boards.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, PlayerScoreboard> entry = it.next();
                try {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        entry.getValue().hide(player);
                        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                    }
                    entry.getValue().destroy();
                } catch (Exception e) {
                    plugin.getLogger().warning("Error stopping scoreboard: " + e.getMessage());
                }
                it.remove();
            }
            placeholderCache.clear();
        }
    }


    private void updateAll() {
        if (!config.isEnabled() || reloading.get()) return;

        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerScoreboard board = boards.get(player.getUniqueId());
            if (board == null || !board.isActive()) continue;

            UUID uuid = player.getUniqueId();

            ProcessedData cached = placeholderCache.get(uuid);
            if (cached != null && (now - cached.timestamp) < CACHE_TTL_MS) {
                try {
                    board.updateProcessed(player, cached.title, cached.lines);
                } catch (Exception e) {
                    plugin.getLogger().warning("Error updating scoreboard from cache for " + player.getName());
                }
                continue;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    String title = translateColorCodes(processor.processString(player, config.getTitleRaw()));
                    List<String> lines = new ArrayList<>();
                    for (String line : config.getLines()) {
                        lines.add(translateColorCodes(processor.processString(player, line)));
                    }

                    placeholderCache.put(uuid, new ProcessedData(title, lines, System.currentTimeMillis()));

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (player.isOnline() && board.isActive()) {
                            try {
                                board.updateProcessed(player, title, lines);
                            } catch (Exception e) {
                                plugin.getLogger().warning("Error updating scoreboard for " + player.getName() + ": " + e.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error processing placeholders for " + player.getName(), e);
                }
            });
        }

        if (now % 20 == 0) {
            placeholderCache.entrySet().removeIf(e -> (now - e.getValue().timestamp) > CACHE_TTL_MS * 2);
        }
    }

    private String translateColorCodes(String text) {
        if (text == null || text.isEmpty()) return text;

        String result = text;

        result = result.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>");

        result = result.replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");

        return result;
    }

    public void toggle(Player player) {
        UUID uuid = player.getUniqueId();

        if (disabledPlayers.contains(uuid)) {
            disabledPlayers.remove(uuid);
            addPlayer(player);
            player.sendMessage(plugin.getLanguageManager().get(player, "scoreboard.enabled"));
        } else {
            disabledPlayers.add(uuid);
            removePlayer(player);
            placeholderCache.remove(uuid);
            try {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            } catch (Exception e) {
                plugin.getLogger().warning("Error resetting scoreboard: " + e.getMessage());
            }
            player.sendMessage(plugin.getLanguageManager().get(player, "scoreboard.disabled"));
        }

        if (config.isPersistent()) {
            saveDisabled();
        }
    }

    public boolean isEnabled(Player player) {
        return !disabledPlayers.contains(player.getUniqueId());
    }

    public boolean isGloballyEnabled() {
        return config != null && config.isEnabled();
    }

    private void removePlayer(Player player) {
        PlayerScoreboard board = boards.remove(player.getUniqueId());
        if (board != null) {
            try {
                board.hide(player);
                board.destroy();
            } catch (Exception e) {
                plugin.getLogger().warning("Error removing scoreboard: " + e.getMessage());
            }
        }
        placeholderCache.remove(player.getUniqueId());
    }

    private void loadDisabled() {
        if (!config.isPersistent()) return;

        File oldFile = new File(plugin.getDataFolder(), "scoreboards/disabled.txt");
        if (oldFile.exists()) {
            migrateFromOldFormat(oldFile);
        }

        if (!dataFile.exists()) return;

        try {
            dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            List<String> uuidList = dataConfig.getStringList("disabled-players");
            for (String uuidStr : uuidList) {
                try {
                    disabledPlayers.add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load scoreboard data", e);
        }
    }

    private void migrateFromOldFormat(File oldFile) {
        try {
            java.nio.file.Files.readAllLines(oldFile.toPath()).forEach(line -> {
                try {
                    disabledPlayers.add(UUID.fromString(line.trim()));
                } catch (IllegalArgumentException ignored) {}
            });
            saveDisabled();
            oldFile.renameTo(new File(plugin.getDataFolder(), "scoreboards/disabled.txt.backup"));
            plugin.getLogger().info("Migrated scoreboard data to YAML format");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to migrate old scoreboard data", e);
        }
    }

    private void saveDisabled() {
        if (!config.isPersistent()) return;

        try {
            dataFile.getParentFile().mkdirs();
            dataConfig = new YamlConfiguration();
            List<String> uuidList = new ArrayList<>();
            for (UUID uuid : disabledPlayers) {
                uuidList.add(uuid.toString());
            }
            dataConfig.set("disabled-players", uuidList);
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save scoreboard data", e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!config.isEnabled() || reloading.get()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getPlayer();
            if (player.isOnline() && !reloading.get()) {
                addPlayer(player);
            }
        }, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    public void shutdown() {
        reloading.set(true);
        stop();
        saveDisabled();
    }
}