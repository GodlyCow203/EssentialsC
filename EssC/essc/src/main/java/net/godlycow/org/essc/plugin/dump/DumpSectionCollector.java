
package net.godlycow.org.essc.plugin.dump;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.punishment.PunishmentManager;
import net.godlycow.org.essc.server.FeatureFlags;
import net.godlycow.org.essc.server.software.ServerSoftware;
import net.godlycow.org.essc.storage.user.UserDatabase;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DumpSectionCollector {

    private final EssentialsC plugin;

    private final List<String> sectionNames = List.of(
            "environment", "hooks", "config", "economy", "punishments",
            "databases", "managers", "plugins"
    );

    private final Set<String> sensitiveKeyFragments = Set.of(
            "password", "passwd", "secret", "token", "apikey", "api-key",
            "api_key", "credential", "private", "auth"
    );

    public DumpSectionCollector(EssentialsC plugin) {
        this.plugin = plugin;
    }

    public List<String> getSectionNames() {
        return sectionNames;
    }

    public void collect(List<String> sections, Map<String, Object> out) {
        boolean all = sections == null || sections.isEmpty();

        out.put("generatedAt", Instant.now().toString());
        out.put("pluginVersion", plugin.getDescription().getVersion());
        out.put("serverVersion", Bukkit.getVersion());
        out.put("software", ServerSoftware.get().name());
        out.put("java", System.getProperty("java.version"));
        out.put("uptime", formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()));

        if (all || sections.contains("environment")) {
            safePut(out, "environment", this::buildEnvironment);
        }

        if (all || sections.contains("hooks")) {
            safePut(out, "hooks", this::buildHooks);
        }

        if (all || sections.contains("config")) {
            safePut(out, "config", this::buildConfig);
        }

        if (all || sections.contains("economy")) {
            safePut(out, "economy", this::buildEconomy);
        }

        if (all || sections.contains("punishments")) {
            safePut(out, "punishments", this::buildPunishments);
        }

        if (all || sections.contains("databases")) {
            safePut(out, "databases", this::buildDatabases);
        }

        if (all || sections.contains("managers")) {
            safePut(out, "managers", this::buildManagers);
        }

        if (all || sections.contains("plugins")) {
            safePut(out, "plugins", this::buildConflictingPlugins);
        }
    }

    private Map<String, Object> buildEnvironment() {
        Map<String, Object> env = new LinkedHashMap<>();

        double[] tps = Bukkit.getServer().getTPS();
        env.put("tps_1m", tps.length > 0 ? Math.round(tps[0] * 100.0) / 100.0 : null);
        env.put("tps_5m", tps.length > 1 ? Math.round(tps[1] * 100.0) / 100.0 : null);
        env.put("tps_15m", tps.length > 2 ? Math.round(tps[2] * 100.0) / 100.0 : null);

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long maxMb = rt.maxMemory() / 1024 / 1024;

        env.put("memoryUsedMb", usedMb);
        env.put("memoryMaxMb", maxMb);
        env.put("memoryPercent", Math.round(usedMb * 100.0 / maxMb) + "%");
        env.put("processors", rt.availableProcessors());

        env.put("onlineMode", Bukkit.getOnlineMode());
        env.put("onlinePlayers", Bukkit.getOnlinePlayers().size());
        env.put("maxPlayers", Bukkit.getMaxPlayers());

        env.put("folia", ServerSoftware.isFolia());
        env.put("paper", ServerSoftware.isPaper());
        env.put("nativeAsyncTeleport", FeatureFlags.supportsNativeAsyncTeleport());
        env.put("paperChatEvent", FeatureFlags.supportsPaperChatEvent());

        return env;
    }

    private Map<String, Object> buildHooks() {
        Map<String, Object> hooks = new LinkedHashMap<>();

        hooks.put("vault", plugin.isVaultHooked());
        hooks.put("luckPerms", Bukkit.getPluginManager().getPlugin("LuckPerms") != null);
        hooks.put("placeholderAPI", Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);
        hooks.put("discordSRV", plugin.getDiscordSRVHook() != null);
        hooks.put("floodgate", plugin.getBedrockUtil() != null);
        hooks.put("tab", Bukkit.getPluginManager().getPlugin("TAB") != null);

        if (!plugin.isVaultHooked()) {
            boolean vaultPresent = Bukkit.getPluginManager().getPlugin("Vault") != null;
            hooks.put("vault_detail", vaultPresent
                    ? "Vault present but hook failed"
                    : "Vault not installed");
        }

        return hooks;
    }

    private Map<String, Object> buildConfig() {
        Map<String, Object> config = new LinkedHashMap<>();

        try {
            Map<String, Object> sanitized =
                    (Map<String, Object>) sanitize(plugin.getConfig().getValues(true));
            config.putAll(sanitized);
        } catch (Exception e) {
            config.put("_error", "Could not read config.yml: " + e.getMessage());
        }

        return config;
    }

    private Map<String, Object> buildEconomy() {
        Map<String, Object> eco = new LinkedHashMap<>();

        if (plugin.getEconomyManager() == null) {
            eco.put("enabled", false);
            return eco;
        }

        eco.put("enabled", true);
        eco.put("vaultHooked", plugin.isVaultHooked());
        eco.put("currencySingular", plugin.getEconomyManager().currencyNameSingular());
        eco.put("currencyPlural", plugin.getEconomyManager().currencyNamePlural());
        eco.put("startingBalance", plugin.getEconomyManager().getStartingBalance().toPlainString());
        eco.put("minTransaction", plugin.getEconomyManager().getMinTransaction().toPlainString());
        eco.put("maxBalance", plugin.getEconomyManager().hasMaxBalance()
                ? plugin.getEconomyManager().getMaxBalance().toPlainString()
                : "none");

        try (Connection conn = plugin.getEconomyManager().getDatabase().openFreshConnection();
             Statement st = conn.createStatement()) {

            ResultSet count = st.executeQuery("SELECT COUNT(*) AS c FROM economy");
            if (count.next()) {
                eco.put("totalAccounts", count.getInt("c"));
            }

            ResultSet total = st.executeQuery("SELECT ROUND(SUM(balance), 2) AS t FROM economy");
            if (total.next()) {
                eco.put("totalMoneyInCirculation", total.getDouble("t"));
            }

            ResultSet top = st.executeQuery(
                    "SELECT username, balance FROM economy ORDER BY balance DESC LIMIT 5"
            );

            List<String> topList = new ArrayList<>();
            while (top.next()) {
                topList.add(top.getString("username") + " — " + top.getDouble("balance"));
            }

            eco.put("top5Balances", topList);

            ResultSet zeroes = st.executeQuery(
                    "SELECT COUNT(*) AS c FROM economy WHERE balance = 0"
            );

            if (zeroes.next()) {
                eco.put("accountsWithZeroBalance", zeroes.getInt("c"));
            }

        } catch (Exception e) {
            eco.put("db_error", e.getMessage());
        }

        return eco;
    }

    private Map<String, Object> buildPunishments() {
        Map<String, Object> pun = new LinkedHashMap<>();

        if (plugin.getPunishmentManager() == null) {
            pun.put("enabled", false);
            return pun;
        }

        PunishmentManager pm = plugin.getPunishmentManager();

        List<PunishmentManager.BanEntry> bans = pm.getActiveBans();
        List<PunishmentManager.IpBanEntry> ipBans = pm.getActiveIpBans();
        List<PunishmentManager.MuteEntry> mutes = pm.getAllMutes();

        pun.put("activeBans", bans.size());
        pun.put("activeIpBans", ipBans.size());
        pun.put("activeMutes", mutes.size());
        pun.put("networkHookActive", pm.getNetworkHook() != null);

        List<Map<String, Object>> banList = new ArrayList<>();
        for (PunishmentManager.BanEntry ban : bans) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("player", ban.name());
            entry.put("uuid", ban.uuid().toString());
            entry.put("reason", ban.reason());
            entry.put("banner", ban.banner());
            entry.put("expires", ban.expires() <= 0
                    ? "permanent"
                    : Instant.ofEpochMilli(ban.expires()).toString());
            banList.add(entry);
        }

        pun.put("bans", banList);

        List<Map<String, Object>> muteList = new ArrayList<>();
        for (PunishmentManager.MuteEntry mute : mutes) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("player", mute.name());
            entry.put("uuid", mute.uuid().toString());
            entry.put("reason", mute.reason());
            entry.put("muter", mute.muter());
            entry.put("expires", mute.expires() <= 0
                    ? "permanent"
                    : Instant.ofEpochMilli(mute.expires()).toString());
            muteList.add(entry);
        }

        pun.put("mutes", muteList);

        return pun;
    }

    private Map<String, Object> buildDatabases() {
        Map<String, Object> dbs = new LinkedHashMap<>();

        File dbDir = new File(plugin.getDataFolder(), "databases");

        if (!dbDir.exists()) {
            dbs.put("directory", "missing");
            return dbs;
        }

        dbs.put("directory", dbDir.getAbsolutePath());

        File[] files = dbDir.listFiles((dir, name) -> name.endsWith(".db"));

        if (files == null || files.length == 0) {
            dbs.put("files", "none found");
            return dbs;
        }

        List<Map<String, Object>> fileList = new ArrayList<>();

        for (File f : files) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", f.getName());
            entry.put("sizeMb", Math.round(f.length() / 1024.0 / 1024.0 * 100.0) / 100.0);
            entry.put("readable", f.canRead());
            entry.put("writable", f.canWrite());
            fileList.add(entry);
        }

        dbs.put("files", fileList);

        if (plugin.getUserManager() != null) {
            try {
                UserDatabase userDb =
                        (UserDatabase) plugin.getUserManager().getRepository();

                try (Connection conn = userDb.getDatabase().openFreshConnection();
                     Statement st = conn.createStatement()) {

                    ResultSet users = st.executeQuery("SELECT COUNT(*) AS c FROM users");
                    if (users.next()) {
                        dbs.put("userCount", users.getInt("c"));
                    }

                    ResultSet bannedUsers = st.executeQuery(
                            "SELECT COUNT(*) AS c FROM users WHERE ban_expires IS NOT NULL AND ban_expires != 0"
                    );

                    if (bannedUsers.next()) {
                        dbs.put("usersWithBanRecord", bannedUsers.getInt("c"));
                    }

                    ResultSet mutedUsers = st.executeQuery(
                            "SELECT COUNT(*) AS c FROM users WHERE mute_expires IS NOT NULL AND mute_expires != 0"
                    );

                    if (mutedUsers.next()) {
                        dbs.put("usersWithMuteRecord", mutedUsers.getInt("c"));
                    }

                    ResultSet flyUsers = st.executeQuery(
                            "SELECT COUNT(*) AS c FROM users WHERE fly_enabled = 1"
                    );

                    if (flyUsers.next()) {
                        dbs.put("usersWithFlyEnabled", flyUsers.getInt("c"));
                    }
                }
            } catch (Exception e) {
                dbs.put("users_db_error", e.getMessage());
            }
        }

        return dbs;
    }

    private Map<String, Object> buildManagers() {
        Map<String, Object> mgr = new LinkedHashMap<>();

        mgr.put("economy", plugin.getEconomyManager() != null);
        mgr.put("punishment", plugin.getPunishmentManager() != null);
        mgr.put("user", plugin.getUserManager() != null);
        mgr.put("home", plugin.getHomeManager() != null);
        mgr.put("warp", plugin.getWarpManager() != null);
        mgr.put("kit", plugin.getKitManager() != null);
        mgr.put("afk", plugin.getAfkManager() != null);
        mgr.put("vanish", plugin.getVanishManager() != null);
        mgr.put("scoreboard", plugin.getScoreboardManager() != null);
        mgr.put("nick", plugin.getNickManager() != null);
        mgr.put("tpa", plugin.getTPAManager() != null);
        mgr.put("rtp", plugin.getRtpManager() != null);
        mgr.put("tab", plugin.getTabManager() != null);
        mgr.put("shop", plugin.getShopManager() != null);
        mgr.put("auction", plugin.getAuctionManager() != null);
        mgr.put("backup", plugin.getBackupManager() != null);
        mgr.put("discord", plugin.getDiscordSRVHook() != null);
        mgr.put("fly", plugin.getFlyManager() != null);
        mgr.put("chat", plugin.getChatManager() != null);

        return mgr;
    }

    private List<Map<String, Object>> buildConflictingPlugins() {
        Set<String> knownConflicts = Set.of(
                "Essentials",
                "EssentialsX",
                "CMI",
                "EssentialsXChat",
                "EssentialsXSpawn",
                "EssentialsXAntiBuild"
        );

        List<Map<String, Object>> found = new ArrayList<>();

        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            Map<String, Object> entry = new LinkedHashMap<>();

            entry.put("name", p.getName());
            entry.put("version", p.getDescription().getVersion());
            entry.put("enabled", p.isEnabled());

            if (knownConflicts.contains(p.getName())) {
                entry.put("conflict", true);
            }

            found.add(entry);
        }

        return found;
    }

    private void safePut(Map<String, Object> map, String key, MapSupplier supplier) {
        try {
            map.put(key, supplier.get());
        } catch (Exception e) {
            map.put(key + "_error", e.getMessage());
        }
    }

    @FunctionalInterface
    private interface MapSupplier {
        Object get() throws Exception;
    }

    private boolean isSensitiveKey(String key) {
        String lower = key.toLowerCase();

        for (String fragment : sensitiveKeyFragments) {
            if (lower.contains(fragment)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private Object sanitize(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof ConfigurationSection section) {
            Map<String, Object> result = new LinkedHashMap<>();

            for (String key : section.getKeys(false)) {
                result.put(
                        key,
                        isSensitiveKey(key)
                                ? "[redacted]"
                                : sanitize(section.get(key))
                );
            }

            return result;
        }

        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());

                result.put(
                        key,
                        isSensitiveKey(key)
                                ? "[redacted]"
                                : sanitize(entry.getValue())
                );
            }

            return result;
        }

        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>();

            for (Object item : list) {
                result.add(sanitize(item));
            }

            return result;
        }

        if (value instanceof Number
                || value instanceof String
                || value instanceof Boolean) {
            return value;
        }

        return value.toString();
    }

    private String formatUptime(long ms) {
        long seconds = ms / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return hours + "h " + minutes + "m " + secs + "s";
    }
}
