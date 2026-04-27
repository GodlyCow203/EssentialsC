package net.godlycow.org.essc.expansion.mysql.nickname;

import net.godlycow.org.essc.expansion.mysql.database.SyncDatabase;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class NetworkNicknameDatabase {

    private static final String TABLE = "network_nicknames";

    private final SyncDatabase syncDb;
    private final Logger logger;

    public NetworkNicknameDatabase(SyncDatabase syncDb, Logger logger) {
        this.syncDb = syncDb;
        this.logger = logger;
    }

    public void createTable() throws SQLException {
        syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `%s` (
                        `uuid`         VARCHAR(36)   NOT NULL,
                        `nickname`     VARCHAR(256)  DEFAULT NULL,
                        `last_updated` BIGINT        NOT NULL DEFAULT 0,
                        `server_id`    VARCHAR(64)   NOT NULL DEFAULT '',
                        PRIMARY KEY (`uuid`),
                        INDEX `idx_last_updated` (`last_updated`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """.formatted(TABLE));
            }
            return null;
        }).join();
    }

    public CompletableFuture<Void> upsertNickname(UUID uuid, String nickname, long updatedAt, String serverId) {
        return syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO `%s` (uuid, nickname, last_updated, server_id)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        nickname     = IF(last_updated < VALUES(last_updated), VALUES(nickname),     nickname),
                        server_id    = IF(last_updated < VALUES(last_updated), VALUES(server_id),    server_id),
                        last_updated = GREATEST(last_updated, VALUES(last_updated))
                 """.formatted(TABLE))) {
                ps.setString(1, uuid.toString());
                ps.setString(2, nickname);
                ps.setLong(3, updatedAt);
                ps.setString(4, serverId);
                ps.executeUpdate();
            }
            return null;
        });
    }

    public CompletableFuture<Void> clearNickname(UUID uuid, long updatedAt, String serverId) {
        return syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO `%s` (uuid, nickname, last_updated, server_id)
                    VALUES (?, NULL, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        nickname     = IF(last_updated < VALUES(last_updated), NULL,             nickname),
                        server_id    = IF(last_updated < VALUES(last_updated), VALUES(server_id), server_id),
                        last_updated = GREATEST(last_updated, VALUES(last_updated))
                 """.formatted(TABLE))) {
                ps.setString(1, uuid.toString());
                ps.setLong(2, updatedAt);
                ps.setString(3, serverId);
                ps.executeUpdate();
            }
            return null;
        });
    }

    public CompletableFuture<NicknameRow> fetchNickname(UUID uuid) {
        return syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT nickname, last_updated FROM `" + TABLE + "` WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return null;
                }
                String nick = rs.getString("nickname");
                long ts = rs.getLong("last_updated");
                return new NicknameRow(nick, ts);
            }
        });
    }

    public record NicknameRow(String nickname, long lastUpdated) {
    }
}