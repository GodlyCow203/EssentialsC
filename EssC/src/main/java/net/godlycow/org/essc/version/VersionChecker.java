package net.godlycow.org.essc.version;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionChecker implements Listener {

    private static final String MODRINTH_PROJECT_ID = "K7HZMVgx";
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/" + MODRINTH_PROJECT_ID + "/version";
    private static String latestVersion = "unknown";
    private static String latestMajor = "unknown";
    private static boolean updateAvailable = false;
    private static boolean breakingChange = false;
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Pattern VERSION_PATTERN = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");

    public VersionChecker(EssentialsC plugin) {
        checkVersion(plugin);
    }

    private void checkVersion(EssentialsC plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(MODRINTH_API_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("User-Agent", "EssentialsC/" + plugin.getDescription().getVersion());

                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        throw new Exception("HTTP " + responseCode);
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String json = response.toString();
                    Matcher matcher = VERSION_PATTERN.matcher(json);

                    if (matcher.find()) {
                        latestVersion = matcher.group(1);
                        String[] versionParts = latestVersion.split("\\.");
                        latestMajor = versionParts.length > 0 ? versionParts[0].replaceAll("[^0-9]", "") : "0";

                        String currentVersion = plugin.getDescription().getVersion();
                        String[] currentParts = currentVersion.split("\\.");
                        String currentMajorRaw = currentParts.length > 0 ? currentParts[0] : "0";
                        String currentMajor = currentMajorRaw.replaceAll("[^0-9]", "");

                        updateAvailable = !currentVersion.equalsIgnoreCase(latestVersion);

                        int latestMajorNum = latestMajor.matches("\\d+") ? Integer.parseInt(latestMajor) : 0;
                        int currentMajorNum = currentMajor.matches("\\d+") ? Integer.parseInt(currentMajor) : 0;

                        breakingChange = updateAvailable && latestMajorNum > currentMajorNum;

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                logVersionStatus(plugin, currentVersion);
                            }
                        }.runTask(plugin);
                    } else {
                        throw new Exception("Could not parse version from response");
                    }

                } catch (Exception e) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getLogger().warning("================================");
                            plugin.getLogger().warning("Could not check for updates: " + e.getMessage());
                            plugin.getLogger().warning("================================");
                        }
                    }.runTask(plugin);
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void logVersionStatus(EssentialsC plugin, String currentVersion) {
        plugin.getLogger().info("================================");

        if (updateAvailable) {
            plugin.getLogger().info("EssentialsC - Update Available");
            plugin.getLogger().info("--------------------------------");
            plugin.getLogger().info("Current: " + currentVersion);
            plugin.getLogger().info("Latest: " + latestVersion);
            plugin.getLogger().info("Download: https://modrinth.com/plugin/essentialsc");

            if (breakingChange) {
                plugin.getLogger().warning("--------------------------------");
                plugin.getLogger().warning("BREAKING CHANGES DETECTED!");
                plugin.getLogger().warning("Major version change requires manual migration.");
                plugin.getLogger().warning("Please backup before upgrading!");
                plugin.getLogger().warning("--------------------------------");
            }
        } else {
            plugin.getLogger().info("EssentialsC - Up To Date");
            plugin.getLogger().info("--------------------------------");
            plugin.getLogger().info("Running version: " + currentVersion);
        }

        plugin.getLogger().info("================================");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!updateAvailable) return;
        if (!player.hasPermission("essentialsc.version.notify") && !player.isOp()) return;

        Bukkit.getScheduler().runTaskLater(EssentialsC.getInstance(), () -> {
            if (!player.isOnline()) return;

            player.sendMessage(MINI.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));
            player.sendMessage(MINI.deserialize("<gradient:#00D2FF:#3A7BD5>EssentialsC Update Available</gradient>"));
            player.sendMessage(MINI.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));
            player.sendMessage(MINI.deserialize("<gray>Current: <red>" + EssentialsC.getInstance().getDescription().getVersion() + "</red>"));
            player.sendMessage(MINI.deserialize("<gray>Latest: <green>" + latestVersion + "</green>"));
            player.sendMessage(MINI.deserialize("<gray>Download: <aqua><click:open_url:'https://modrinth.com/plugin/essentialsc'><hover:show_text:'<gray>Click to open Modrinth'>https://modrinth.com/plugin/essentialsc</hover></click></aqua>"));

            if (breakingChange) {
                player.sendMessage(MINI.deserialize("<dark_red>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_red>"));
                player.sendMessage(MINI.deserialize("<bold><color:#FF0040>⚠ BREAKING CHANGES DETECTED!</color></bold>"));
                player.sendMessage(MINI.deserialize("<color:#FF6B6B>This update contains breaking changes.</color>"));
                player.sendMessage(MINI.deserialize("<color:#FF6B6B>Backup your server before upgrading!</color>"));
                player.sendMessage(MINI.deserialize("<dark_red>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_red>"));
            }

            player.sendMessage(MINI.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));
        }, 20L);
    }

    public static String getLatestVersion() {
        return latestVersion;
    }

    public static boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public static boolean isBreakingChange() {
        return breakingChange;
    }
}