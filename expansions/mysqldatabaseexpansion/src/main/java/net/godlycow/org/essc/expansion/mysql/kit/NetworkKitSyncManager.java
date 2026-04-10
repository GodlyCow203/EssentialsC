package net.godlycow.org.essc.expansion.mysql.kit;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import net.godlycow.org.essc.expansion.mysql.config.SyncConfig;
import net.godlycow.org.essc.expansion.mysql.database.SyncDatabase;
import net.godlycow.org.essc.kit.KitSyncHook;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class NetworkKitSyncManager implements KitSyncHook {

    private final MySQLDatabaseExpansion plugin;
    private final EssentialsC essc;
    private final SyncConfig config;
    private final NetworkKitDatabase db;

    public NetworkKitSyncManager(MySQLDatabaseExpansion plugin,
                                 EssentialsC essc,
                                 SyncConfig config,
                                 SyncDatabase syncDb) throws SQLException {
        this.plugin = plugin;
        this.essc   = essc;
        this.config = config;
        this.db     = new NetworkKitDatabase(syncDb, plugin.getLogger());
        this.db.createTable();
    }

    @Override
    public void onKitClaimed(UUID uuid, String kitName, long claimedAt, String ignoredServerId) {
        db.upsertClaim(uuid, kitName, claimedAt, config.getServerId())
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.WARNING,
                            "[NetworkKits] Failed to push claim for " + uuid + "/" + kitName, ex);
                    return null;
                });
    }

    @Override
    public CompletableFuture<Long> getNetworkLastClaimed(UUID uuid, String kitName) {
        return db.getLastClaimed(uuid, kitName)
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.WARNING,
                            "[NetworkKits] Failed to fetch cooldown for " + uuid + "/" + kitName, ex);
                    return 0L;
                });
    }

    public void shutdown() {
        essc.getKitManager().clearNetworkHook();
    }
}