package net.godlycow.org.essc.expansion.mysql.kit;

import net.godlycow.org.essc.expansion.mysql.database.SyncDatabase;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class NetworkKitDatabase {

    private static final String TABLE = "network_kit_cooldowns";

    private final SyncDatabase syncDb;
    private final Logger logger;

    public NetworkKitDatabase(SyncDatabase syncDb, Logger logger) {
        this.syncDb = syncDb;
        this.logger = logger;
    }

    public void createTable() throws SQLException {
        syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `%s` (
                        `uuid`        VARCHAR(36)  NOT NULL,
                        `kit_name`    VARCHAR(64)  NOT NULL,
                        `claimed_at`  BIGINT       NOT NULL,
                        `server_id`   VARCHAR(64)  NOT NULL,
                        PRIMARY KEY (`uuid`, `kit_name`),
                        INDEX `idx_kit_name` (`kit_name`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """.formatted(TABLE));
            }
            return null;
        }).join();
    }


    public CompletableFuture<Void> upsertClaim(UUID uuid, String kitName, long claimedAt, String serverId) {
        return syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO `%s` (uuid, kit_name, claimed_at, server_id)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        claimed_at = GREATEST(claimed_at, VALUES(claimed_at)),
                        server_id  = IF(claimed_at < VALUES(claimed_at), VALUES(server_id), server_id)
                 """.formatted(TABLE))) {
                ps.setString(1, uuid.toString());
                ps.setString(2, kitName);
                ps.setLong(3, claimedAt);
                ps.setString(4, serverId);
                ps.executeUpdate();
            }
            return null;
        });
    }

    public CompletableFuture<Long> getLastClaimed(UUID uuid, String kitName) {
        return syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT claimed_at FROM `" + TABLE + "` WHERE uuid = ? AND kit_name = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, kitName);
                ResultSet rs = ps.executeQuery();
                return rs.next() ? rs.getLong("claimed_at") : 0L;
            }
        });
    }
}