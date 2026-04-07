package net.godlycow.org.essc.expansion.mysql.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.godlycow.org.essc.expansion.mysql.config.SyncConfig;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class SyncDatabase {

    private static final String TABLE = "economy_sync";

    private final SyncConfig config;
    private final Logger logger;
    private final ExecutorService executor;
    private HikariDataSource dataSource;

    public SyncDatabase(SyncConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
        this.executor = Executors.newFixedThreadPool(config.getMaxPoolSize(),
                r -> new Thread(r, "essc-mysql-sync"));
    }

    public void connect() throws SQLException {
        HikariConfig hkCfg = new HikariConfig();
        hkCfg.setJdbcUrl(config.getJdbcUrl());
        hkCfg.setUsername(config.getUsername());
        hkCfg.setPassword(config.getPassword());
        hkCfg.setMaximumPoolSize(config.getMaxPoolSize());
        hkCfg.setMinimumIdle(1);
        hkCfg.setConnectionTimeout(10_000);
        hkCfg.setIdleTimeout(300_000);
        hkCfg.setMaxLifetime(600_000);
        hkCfg.setPoolName("EsscMysqlSync");
        hkCfg.addDataSourceProperty("cachePrepStmts", "true");
        hkCfg.addDataSourceProperty("prepStmtCacheSize", "50");

        dataSource = new HikariDataSource(hkCfg);
        createTables();
        logger.info("[MySQLExpansion] Connected to MySQL at " + config.getJdbcUrl());
    }

    public void disconnect() {
        executor.shutdownNow();
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `%s` (
                    `uuid`         VARCHAR(36)    NOT NULL,
                    `username`     VARCHAR(64)    NOT NULL,
                    `balance`      DECIMAL(20,2)  NOT NULL DEFAULT 0.00,
                    `last_updated` BIGINT         NOT NULL DEFAULT 0,
                    `updated_by`   VARCHAR(64)    NOT NULL DEFAULT '',
                    PRIMARY KEY (`uuid`),
                    INDEX `idx_last_updated` (`last_updated`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """.formatted(TABLE));
        }
    }

    public <T> CompletableFuture<T> async(SqlSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @FunctionalInterface
    public interface SqlSupplier<T> {
        T get() throws SQLException;
    }

    public CompletableFuture<Map<UUID, BigDecimal>> fetchUpdatedSince(long sinceEpochMs, String excludeServerId) {
        return async(() -> {
            Map<UUID, BigDecimal> result = new LinkedHashMap<>();
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT uuid, balance FROM `" + TABLE + "` WHERE last_updated > ? AND updated_by != ?")) {
                ps.setLong(1, sinceEpochMs);
                ps.setString(2, excludeServerId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    result.put(UUID.fromString(rs.getString("uuid")),
                            rs.getBigDecimal("balance"));
                }
            }
            return result;
        });
    }

    public CompletableFuture<Optional<BigDecimal>> fetchBalance(UUID uuid) {
        return async(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT balance FROM `" + TABLE + "` WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return Optional.of(rs.getBigDecimal("balance"));
                }
                return Optional.empty();
            }
        });
    }

    public CompletableFuture<Void> pushBalance(UUID uuid, String username, BigDecimal balance, String serverId) {
        return async(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO `%s` (uuid, username, balance, last_updated, updated_by)
                    VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        username     = VALUES(username),
                        balance      = VALUES(balance),
                        last_updated = VALUES(last_updated),
                        updated_by   = VALUES(updated_by)
                 """.formatted(TABLE))) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username != null ? username : uuid.toString());
                ps.setBigDecimal(3, balance);
                ps.setLong(4, System.currentTimeMillis());
                ps.setString(5, serverId);
                ps.executeUpdate();
            }
            return null;
        });
    }

    public CompletableFuture<Void> ensureAccount(UUID uuid, String username, BigDecimal fallbackBalance, String serverId) {
        return async(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                    INSERT IGNORE INTO `%s` (uuid, username, balance, last_updated, updated_by)
                    VALUES (?, ?, ?, ?, ?)
                 """.formatted(TABLE))) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username != null ? username : uuid.toString());
                ps.setBigDecimal(3, fallbackBalance);
                ps.setLong(4, System.currentTimeMillis());
                ps.setString(5, serverId);
                ps.executeUpdate();
            }
            return null;
        });
    }

    public CompletableFuture<Map<UUID, BigDecimal>> fetchTopBalances(int limit) {
        return async(() -> {
            Map<UUID, BigDecimal> result = new LinkedHashMap<>();
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT uuid, balance FROM `" + TABLE + "` ORDER BY balance DESC LIMIT ?")) {
                ps.setInt(1, limit);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    result.put(UUID.fromString(rs.getString("uuid")),
                            rs.getBigDecimal("balance"));
                }
            }
            return result;
        });
    }
}