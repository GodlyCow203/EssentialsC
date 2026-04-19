package net.godlycow.org.essc.scoreboard;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.softwares.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class ScoreboardManager implements Listener {
    private final EssentialsC plugin;
    private final PlaceholderProcessor processor;
    private final Map<UUID, PlayerScoreboard> boards = new HashMap<>();
    private final Set<UUID> disabledPlayers = new HashSet<>();

    private ScoreboardConfig config;
    private SchedulerTask updateTask;
    private File dataFile;
    private YamlConfiguration dataConfig;
    private final AtomicBoolean reloading = new AtomicBoolean(false);
    private final Object lock = new Object();

    private final Map<UUID, ProcessedData> placeholderCache = new HashMap<>();
    private static final long CACHE_TTL_MS = 1000L;

    private final List<Player> stalePlayers = new ArrayList<>(64);

    private record ProcessedData(String title, String[] lines, long timestamp) {}

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
        updateTask = plugin.getEssScheduler().runGlobalTimer(this::updateAll,
                10L, config.getUpdateInterval());
    }

    private void addPlayer(Player player) {
        if (disabledPlayers.contains(player.getUniqueId())) return;
        if (!player.isOnline()) return;

        plugin.getEssScheduler().runForLocation(player.getLocation(), () -> {
            if (!player.isOnline()) return;
            try {
                PlayerScoreboard existing;
                synchronized (boards) {
                    existing = boards.remove(player.getUniqueId());
                }
                if (existing != null) {
                    existing.hide(player);
                    existing.destroy();
                }

                PlayerScoreboard board = new PlayerScoreboard(player, config);
                synchronized (boards) {
                    boards.put(player.getUniqueId(), board);
                }
                board.show(player);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to add scoreboard for " + player.getName(), e);
            }
        });
    }

    public void stop() {
        synchronized (lock) {
            if (updateTask != null) {
                updateTask.cancel();
                updateTask = null;
            }

            synchronized (boards) {
                Iterator<Map.Entry<UUID, PlayerScoreboard>> it = boards.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, PlayerScoreboard> entry = it.next();
                    try {
                        Player player = Bukkit.getPlayer(entry.getKey());
                        PlayerScoreboard sb = entry.getValue();
                        if (player != null && player.isOnline()) {
                            plugin.getEssScheduler().runForLocation(player.getLocation(), () -> {
                                try { sb.hide(player); } catch (Exception ignored) {}
                            });
                        }
                        sb.destroy();
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error stopping scoreboard: " + e.getMessage());
                    }
                    it.remove();
                }
            }
            placeholderCache.clear();
        }
    }

    private void updateAll() {
        if (!config.isEnabled() || reloading.get()) return;

        final long now = System.currentTimeMillis();
        stalePlayers.clear();

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        synchronized (boards) {
            for (Player player : onlinePlayers) {
                PlayerScoreboard board = boards.get(player.getUniqueId());
                if (board == null || !board.isActive()) continue;

                UUID uuid = player.getUniqueId();
                ProcessedData cached = placeholderCache.get(uuid);

                if (cached != null && (now - cached.timestamp) < CACHE_TTL_MS) {
                    final PlayerScoreboard cachedBoard = board;
                    final ProcessedData cachedData = cached;
                    final Player cachedPlayer = player;
                    plugin.getEssScheduler().runForLocation(player.getLocation(), () -> {
                        if (!cachedPlayer.isOnline() || !cachedBoard.isActive()) return;
                        try {
                            cachedBoard.updateProcessed(cachedPlayer, cachedData.title, Arrays.asList(cachedData.lines));
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error updating scoreboard from cache for " + cachedPlayer.getName());
                        }
                    });
                } else {
                    stalePlayers.add(player);
                }
            }
        }

        if (!stalePlayers.isEmpty()) {
            final List<Player> toProcess = new ArrayList<>(stalePlayers);

            plugin.getEssScheduler().runAsync(() -> {
                List<ProcessedResult> results = new ArrayList<>(toProcess.size());

                for (Player player : toProcess) {
                    try {
                        String title = translateColorCodes(processor.processString(player, config.getTitleRaw()));
                        List<String> lineList = config.getLines();
                        String[] lines = new String[lineList.size()];

                        for (int i = 0; i < lineList.size(); i++) {
                            lines[i] = translateColorCodes(processor.processString(player, lineList.get(i)));
                        }

                        results.add(new ProcessedResult(player.getUniqueId(), title, lines));
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Error processing placeholders for " + player.getName(), e);
                    }
                }

                if (!results.isEmpty()) {
                    final long applyTime = System.currentTimeMillis();

                    for (ProcessedResult result : results) {
                        Player player = Bukkit.getPlayer(result.uuid);
                        if (player == null || !player.isOnline()) continue;

                        PlayerScoreboard board;
                        synchronized (boards) {
                            board = boards.get(result.uuid);
                        }
                        if (board == null || !board.isActive()) continue;

                        placeholderCache.put(result.uuid,
                                new ProcessedData(result.title, result.lines, applyTime));

                        final PlayerScoreboard finalBoard = board;
                        final ProcessedResult finalResult = result;
                        final Player finalPlayer = player;

                        plugin.getEssScheduler().runForLocation(player.getLocation(), () -> {
                            if (!finalPlayer.isOnline() || !finalBoard.isActive()) return;
                            try {
                                finalBoard.updateProcessed(finalPlayer, finalResult.title, Arrays.asList(finalResult.lines));
                            } catch (Exception e) {
                                plugin.getLogger().warning("Error updating scoreboard for " + finalPlayer.getName() + ": " + e.getMessage());
                            }
                        });
                    }
                }
            });
        }

        if ((now & 0x1F) == 0) {
            placeholderCache.entrySet().removeIf(e -> (now - e.getValue().timestamp) > CACHE_TTL_MS * 2);
        }
    }

    private record ProcessedResult(UUID uuid, String title, String[] lines) {}

    private String translateColorCodes(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder sb = new StringBuilder(text.length() + 16);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                String replacement = switch (code) {
                    case '0' -> "<black>";
                    case '1' -> "<dark_blue>";
                    case '2' -> "<dark_green>";
                    case '3' -> "<dark_aqua>";
                    case '4' -> "<dark_red>";
                    case '5' -> "<dark_purple>";
                    case '6' -> "<gold>";
                    case '7' -> "<gray>";
                    case '8' -> "<dark_gray>";
                    case '9' -> "<blue>";
                    case 'a' -> "<green>";
                    case 'b' -> "<aqua>";
                    case 'c' -> "<red>";
                    case 'd' -> "<light_purple>";
                    case 'e' -> "<yellow>";
                    case 'f' -> "<white>";
                    case 'k' -> "<obfuscated>";
                    case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>";
                    case 'n' -> "<underlined>";
                    case 'o' -> "<italic>";
                    case 'r' -> "<reset>";
                    default -> null;
                };

                if (replacement != null) {
                    sb.append(replacement);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }

        return sb.toString();
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
        PlayerScoreboard board;
        synchronized (boards) {
            board = boards.remove(player.getUniqueId());
        }
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

        Player joining = event.getPlayer();
        plugin.getEssScheduler().runForLocationLater(joining.getLocation(), () -> {
            if (joining.isOnline() && !reloading.get()) {
                addPlayer(joining);
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