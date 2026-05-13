package net.godlycow.org.essc.storage.database;

import net.godlycow.org.essc.EssentialsC;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Database {
    private final EssentialsC plugin;
    private final String dbPath;
    private final String jdbcUrl;
    private Connection connection;

    public Database(EssentialsC plugin) {
        this(plugin, "economy.db");
    }

    public Database(EssentialsC plugin, String filename) {
        this.plugin = plugin;

        File databasesDir = new File(plugin.getDataFolder(), "databases");
        if (!databasesDir.exists()) {
            databasesDir.mkdirs();
        }

        this.dbPath = new File(databasesDir, filename).getAbsolutePath();
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) return;
        connection = DriverManager.getConnection(jdbcUrl);
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

    public String getDbPath() {
        return dbPath;
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

    public Connection openFreshConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcUrl);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA busy_timeout=5000");
        }
        return conn;
    }

    public <T> CompletableFuture<T> async(AsyncQuery<T> query) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = openFreshConnection()) {
                return query.execute(conn);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Database error", e);
                throw new RuntimeException(e);
            }
        }, plugin.getEssScheduler().asyncExecutor());
    }

    @FunctionalInterface
    public interface AsyncQuery<T> {
        T execute(Connection connection) throws SQLException;
    }
}