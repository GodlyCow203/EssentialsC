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
    private final String dbPath;
    private Connection connection;

    public Database(EssentialsC plugin) {
        this(plugin, "economy.db");
    }

    public Database(EssentialsC plugin, String filename) {
        this.plugin = plugin;
        this.dbPath = new File(plugin.getDataFolder(), filename).getAbsolutePath();
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) return;

        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        plugin.debug("Connected to SQLite database: " + new File(dbPath).getName());
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.debug("Disconnected from SQLite database: " + new File(dbPath).getName());
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error closing database connection", e);
            }
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