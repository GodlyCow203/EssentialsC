package net.godlycow.org.essc.expansion.mysql.punishment;

import net.godlycow.org.essc.expansion.mysql.database.SyncDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkPunishmentDatabase {

    private static final String TABLE = "network_punishments";

    private final SyncDatabase syncDb;
    private final Logger logger;

    public enum PunishType { BAN, IP_BAN, MUTE }

    public record NetworkPunishment(
            long id,
            String type,
            String target,
            String targetName,
            String reason,
            String punisher,
            String serverId,
            long time,
            long expires,
            boolean active
    ) {}

    public NetworkPunishmentDatabase(SyncDatabase syncDb, Logger logger) {
        this.syncDb = syncDb;
        this.logger = logger;
    }

    public void createTable() throws SQLException {
        syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `%s` (
                        `id`          BIGINT        NOT NULL AUTO_INCREMENT,
                        `type`        VARCHAR(10)   NOT NULL,
                        `target`      VARCHAR(64)   NOT NULL,
                        `target_name` VARCHAR(64)   DEFAULT NULL,
                        `reason`      VARCHAR(512)  NOT NULL,
                        `punisher`    VARCHAR(64)   NOT NULL,
                        `server_id`   VARCHAR(64)   NOT NULL,
                        `time`        BIGINT        NOT NULL,
                        `expires`     BIGINT        NOT NULL DEFAULT -1,
                        `active`      TINYINT(1)    NOT NULL DEFAULT 1,
                        PRIMARY KEY (`id`),
                        INDEX `idx_target`      (`target`),
                        INDEX `idx_type_active` (`type`, `active`),
                        INDEX `idx_time`        (`time`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """.formatted(TABLE));
            }
            return null;
        }).join();
    }


    public CompletableFuture<Long> insertPunishment(PunishType type, String target, String targetName,
                                                    String reason, String punisher,
                                                    String serverId, long expires) {
        return syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO `" + TABLE + "` (type, target, target_name, reason, punisher, server_id, time, expires, active) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)",
                         Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, type.name());
                ps.setString(2, target);
                ps.setString(3, targetName);
                ps.setString(4, reason);
                ps.setString(5, punisher);
                ps.setString(6, serverId);
                ps.setLong(7, System.currentTimeMillis());
                ps.setLong(8, expires);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                return keys.next() ? keys.getLong(1) : null;
            }
        });
    }

    public CompletableFuture<Void> deactivatePunishment(PunishType type, String target) {
        return syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE `" + TABLE + "` SET active = 0 WHERE type = ? AND target = ? AND active = 1")) {
                ps.setString(1, type.name());
                ps.setString(2, target);
                ps.executeUpdate();
            }
            return null;
        });
    }


    public CompletableFuture<NetworkPunishment> getActiveBan(UUID uuid) {
        return getActivePunishment(PunishType.BAN, uuid.toString());
    }

    public CompletableFuture<NetworkPunishment> getActiveIpBan(String ip) {
        return getActivePunishment(PunishType.IP_BAN, ip);
    }

    public CompletableFuture<NetworkPunishment> getActiveMute(UUID uuid) {
        return getActivePunishment(PunishType.MUTE, uuid.toString());
    }

    private CompletableFuture<NetworkPunishment> getActivePunishment(PunishType type, String target) {
        return syncDb.async(() -> {
            try (Connection conn = syncDb.getRawConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM `" + TABLE + "` WHERE type = ? AND target = ? AND active = 1 " +
                                 "ORDER BY time DESC LIMIT 1")) {
                ps.setString(1, type.name());
                ps.setString(2, target);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    NetworkPunishment p = mapRow(rs);
                    if (p.expires() > 0 && p.expires() < System.currentTimeMillis()) {
                        deactivatePunishment(type, target);
                        return null;
                    }
                    return p;
                }
                return null;
            }
        });
    }

    public CompletableFuture<List<NetworkPunishment>> fetchUpdatedSince(long sinceMs, String excludeServerId) {
        return syncDb.async(() -> {
            List<NetworkPunishment> results = new ArrayList<>();
            try (Connection conn = syncDb.getRawConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM `" + TABLE + "` WHERE time > ? AND server_id != ?")) {
                ps.setLong(1, sinceMs);
                ps.setString(2, excludeServerId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) results.add(mapRow(rs));
            }
            return results;
        });
    }

    private NetworkPunishment mapRow(ResultSet rs) throws SQLException {
        return new NetworkPunishment(
                rs.getLong("id"),
                rs.getString("type"),
                rs.getString("target"),
                rs.getString("target_name"),
                rs.getString("reason"),
                rs.getString("punisher"),
                rs.getString("server_id"),
                rs.getLong("time"),
                rs.getLong("expires"),
                rs.getBoolean("active")
        );
    }
}