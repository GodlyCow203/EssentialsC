package net.godlycow.org.essc.expansion.mysql.config;

import net.godlycow.org.essc.expansion.mysql.MySQLExpansion;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ExpansionConfig {


    private final MySQLExpansion plugin;
    private final FileConfiguration config;

    public ExpansionConfig(MySQLExpansion plugin) {
        this.plugin = plugin;

        this.config = plugin.getConfig();
    }

    public String getMysqlHost() {
        return config.getString("mysql.host", "localhost");
    }

    public int getMysqlPort() {
        return config.getInt("mysql.port", 3306);
    }

    public String getMysqlDatabase() {
        return config.getString("mysql.database", "essentialsc");
    }

    public String getMysqlUser() {
        return config.getString("mysql.username", "root");
    }


    public String getMysqlPassword() {
        return config.getString("mysql.password", "");
    }

    public String getTablePrefix() {
        String prefix = config.getString("mysql.table-prefix", "essc_net_");
        return prefix == null ? "" : prefix;

    }

    public int getPoolMaximumSize() {
        return Math.max(1, config.getInt("mysql.pool.maximum-pool-size", 10));
    }

    public int getPoolMinimumIdle() {
        return Math.max(0, config.getInt("mysql.pool.minimum-idle", 2));
    }


    public long getConnectionTimeoutMs() {
        return config.getLong("mysql.pool.connection-timeout-ms", 30000);
    }

    public long getIdleTimeoutMs() {
        return config.getLong("mysql.pool.idle-timeout-ms", 600000);
    }

    public long getMaxLifetimeMs() {
        return config.getLong("mysql.pool.max-lifetime-ms", 1800000);
    }

    public Map<String, String> getPoolProperties() {
        Map<String, String> props = new HashMap<>();
        var section = config.getConfigurationSection("mysql.pool.properties");
        if (section != null) {
            for (var key : section.getKeys(false)) {
                props.put(key, String.valueOf(section.get(key)));
            }
        }
        return props;
    }


    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    public String getServerId() {
        String id = config.getString("server-id", "");
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            config.set("server-id", id);
            plugin.saveConfig();
            plugin.getLogger().info("Generated new network server-id: " + id);
        }
        return id;
    }


    public boolean isEconomyEnabled() {
        return config.getBoolean("sync.economy.enabled", true);
    }

    public boolean isPullOnJoin() {
        return config.getBoolean("sync.economy.pull-on-join", true);
    }

    public boolean isPushOnQuit() {
        return config.getBoolean("sync.economy.push-on-quit", true);
    }

    public int getPushIntervalSeconds() {
        return Math.max(1, config.getInt("sync.economy.push-interval-seconds", 2));
    }

    public boolean isSyncLocal() {
        return config.getBoolean("sync.economy.sync-local",  true);
    }

    public int getMirrorIntervalSeconds() {
        return Math.max(1, config.getInt("sync.economy.mirror-interval-seconds", 5));
    }

    public int getBaltopPageSize() {
        return Math.max(1, config.getInt("sync.economy.baltop-page-size", 10));
    }

    public boolean isHomesEnabled() {
        return config.getBoolean("sync.homes.enabled", false);
    }

    public boolean isWarpsEnabled() {
        return config.getBoolean("sync.warps.enabled", false);
    }

    public boolean isNicknamesEnabled() {
        return config.getBoolean("sync.nicknames.enabled", false);
    }

    public boolean isPunishmentsEnabled() {
        return config.getBoolean("sync.punishments.enabled", false);
    }

    public boolean isKitsEnabled() {
        return config.getBoolean("sync.kits.enabled", false);
    }
}
