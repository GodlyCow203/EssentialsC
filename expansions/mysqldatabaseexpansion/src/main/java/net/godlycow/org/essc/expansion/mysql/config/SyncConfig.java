package net.godlycow.org.essc.expansion.mysql.config;

import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import org.bukkit.configuration.file.FileConfiguration;

public class SyncConfig {

    private final MySQLDatabaseExpansion plugin;

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String serverId;
    private int maxPoolSize;
    private boolean networkKitsEnabled;
    private boolean networkNicknamesEnabled;
    private int nicknamePollIntervalTicks;

    public SyncConfig(MySQLDatabaseExpansion plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration cfg = plugin.getConfig();

        host = cfg.getString("mysql.host", "localhost");
        port = cfg.getInt("mysql.port", 3306);
        database = cfg.getString("mysql.database", "essc_sync");
        username = cfg.getString("mysql.username", "root");
        password = cfg.getString("mysql.password", "");

        maxPoolSize = cfg.getInt("mysql.pool-size", 10);

        serverId = cfg.getString("server-id", "server-1");

        networkKitsEnabled = cfg.getBoolean("network-kits.enabled", true);

        networkNicknamesEnabled = cfg.getBoolean("network-nicknames.enabled", true);
        nicknamePollIntervalTicks = cfg.getInt("network-nicknames.poll-interval-ticks", 40);
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&autoReconnect=true&characterEncoding=utf8";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getServerId() {
        return serverId;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public boolean isNetworkKitsEnabled() {
        return networkKitsEnabled;
    }

    public boolean isNetworkNicknamesEnabled() {
        return networkNicknamesEnabled;
    }

    public long getNicknamePollIntervalTicks() {
        return nicknamePollIntervalTicks;
    }

    public int getPollIntervalTicks() {
        return 20;
    }

    public int getPushDebounceMs() {
        return 0;
    }

    public int getLocalPushIntervalTicks() {
        return 20;
    }

}