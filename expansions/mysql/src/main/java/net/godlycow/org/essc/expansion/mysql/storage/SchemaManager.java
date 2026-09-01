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

    public void ensureSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS " + balancesTable() + " ("
                + "uuid CHAR(36) NOT NULL, "
                + "username VARCHAR(64) NOT NULL, "
                + "balance DECIMAL(20,2) NOT NULL DEFAULT 0.00, "
                + "server_id VARCHAR(64) NOT NULL, "
                + "last_updated BIGINT NOT NULL, "
                + "PRIMARY KEY (uuid)"
                + ")";

        pool.update(sql).whenComplete((result, error) -> {
            if (error !=  null) {

                plugin.getLogger().severe("Failed to create MySQL table '" + balancesTable() + "': "
                        + error.getCause().getMessage());
            } else {

                plugin.debug("Ensured MySQL table '" + balancesTable() + "' exists.");
            }
        });
    }
}
