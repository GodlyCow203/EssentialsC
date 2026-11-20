package net.godlycow.org.essc.expansion.mysql.storage;

import net.godlycow.org.essc.expansion.mysql.MySQLExpansion;

import java.util.UUID;
public class SchemaManager {

    private final MySQLExpansion plugin;
    private final ConnectionPool pool;
    private final String prefix;

    public SchemaManager(MySQLExpansion plugin, ConnectionPool pool, String tablePrefix) {
        this.plugin = plugin;
        this.pool = pool;
        this.prefix = tablePrefix;
    }

    public String  balancesTable() {
        return prefix + "network_balances";
    }

    public String nicknamesTable() {
        return prefix + "network_nicknames";
    }

    public void ensureSchema() {
        createTable(balancesTable(), "CREATE TABLE IF NOT EXISTS " + balancesTable() + " ("
                + "uuid CHAR(36) NOT NULL, "
                + "username VARCHAR(64) NOT NULL, "
                + "balance DECIMAL(20,2) NOT NULL DEFAULT 0.00, "
                + "server_id VARCHAR(64) NOT NULL, "
                + "last_updated BIGINT NOT NULL, "
                + "PRIMARY KEY (uuid)"
                + ")");

        createTable(nicknamesTable(), "CREATE TABLE IF NOT EXISTS " + nicknamesTable() + " ("
                + "uuid CHAR(36) NOT NULL, "
                + "nickname VARCHAR(512) NULL, "
                + "server_id VARCHAR(64) NOT NULL, "
                + "last_updated BIGINT NOT NULL, "
                + "PRIMARY KEY (uuid), "
                + "INDEX idx_last_updated (last_updated)"
                + ")");
    }

    private void createTable(String table, String sql) {
        pool.update(sql).whenComplete((result, error) -> {
            if (error != null) {
                Throwable cause = error.getCause() != null ? error.getCause() : error;
                plugin.getLogger().severe("Failed to create MySQL table '" + table + "': " + cause.getMessage());
            } else {
                plugin.debug("Ensured MySQL table '" + table + "' exists.");
            }
        });
    }
}
