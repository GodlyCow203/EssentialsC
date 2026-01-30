package net.godlycow.org.essc.database;

import net.godlycow.org.essc.EssentialsC;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Database {
    private final EssentialsC plugin;
    private Connection connection;
    private final String dbPath;

    public Database(EssentialsC plugin) {
        this.plugin = plugin;
        this.dbPath = new File(plugin.getDataFolder(), "economy.db").getAbsolutePath();
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) return;

        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        plugin.debug("Connected to SQLite database");
        createTables();
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.debug("Disconnected from SQLite database");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error closing database connection", e);
            }
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS economy (
                    uuid TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    balance REAL DEFAULT 0.0,
                    last_updated INTEGER DEFAULT (strftime('%s', 'now'))
                )
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_balance ON economy(balance DESC)
            """);
            plugin.debug("Database tables initialized");
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            return connection;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get database connection: " + e.getMessage());
            return null;
        }
    }

    public <T> CompletableFuture<T> async(AsyncQuery<T> query) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection()) {
                if (conn == null) throw new SQLException("No connection available");
                return query.execute(conn);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Database error", e);
                throw new RuntimeException(e);
            }
        }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable).getTaskId());
    }

    @FunctionalInterface
    public interface AsyncQuery<T> {
        T execute(Connection connection) throws SQLException;
    }
}