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
    private int pollIntervalTicks;
    private int pushDebounceMs;
    private int maxPoolSize;

    public SyncConfig(MySQLDatabaseExpansion plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration cfg = plugin.getConfig();

        host         = cfg.getString("mysql.host", "localhost");
        port         = cfg.getInt("mysql.port", 3306);
        database     = cfg.getString("mysql.database", "essc_sync");
        username     = cfg.getString("mysql.username", "root");
        password     = cfg.getString("mysql.password", "");
        maxPoolSize  = cfg.getInt("mysql.pool-size", 5);

        serverId         = cfg.getString("server-id", "server-1");
        pollIntervalTicks = cfg.getInt("sync.poll-interval-ticks", 200);
        pushDebounceMs   = cfg.getInt("sync.push-debounce-ms", 500);
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&autoReconnect=true&characterEncoding=utf8";
    }

    public String getUsername()         { return username; }
    public String getPassword()         { return password; }
    public String getServerId()         { return serverId; }
    public int getPollIntervalTicks()   { return pollIntervalTicks; }
    public int getPushDebounceMs()      { return pushDebounceMs; }
    public int getMaxPoolSize()         { return maxPoolSize; }
}