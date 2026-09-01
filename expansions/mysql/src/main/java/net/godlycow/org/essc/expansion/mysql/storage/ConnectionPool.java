package net.godlycow.org.essc.expansion.mysql.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.godlycow.org.essc.expansion.mysql.MySQLExpansion;
import net.godlycow.org.essc.expansion.mysql.config.ExpansionConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {

    private final MySQLExpansion plugin;
    private final ExpansionConfig config;

    private HikariDataSource source;
    private volatile boolean healthy = false;
    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(  runnable, "EssentialsC-MySQL-Worker");
        thread.setDaemon(true) ;
        return thread;
    });

    public ConnectionPool(MySQLExpansion plugin, ExpansionConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean connect() {
        try {
            HikariConfig hikari = new HikariConfig();

            hikari.setJdbcUrl(buildJdbcUrl());
            hikari.setUsername(config.getMysqlUser());
            hikari.setPassword(config.getMysqlPassword());
            hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");

            hikari.setMaximumPoolSize(config.getPoolMaximumSize());
            hikari.setMinimumIdle(config.getPoolMinimumIdle());
            hikari.setConnectionTimeout(config.getConnectionTimeoutMs());
            hikari.setIdleTimeout(config.getIdleTimeoutMs());
            hikari.setMaxLifetime(config.getMaxLifetimeMs());
            hikari.setPoolName("EssentialsC-MySQL");
            hikari.setInitializationFailTimeout(-1);

            for (Map.Entry<String, String> property : config.getPoolProperties().entrySet()) {
                hikari.addDataSourceProperty(property.getKey(), property.getValue());
            }

            this.source = new HikariDataSource(hikari);

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create MySQL connection pool: " + e.getMessage());
            if (config.isDebug()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private String buildJdbcUrl() {

        return "jdbc:mysql://" + config.getMysqlHost() + ":" + config.getMysqlPort() + "/"
                + config.getMysqlDatabase();
    }

    public boolean isConnected() {
        return source != null && !source.isClosed() && healthy;
    }

    public CompletableFuture<Integer> update(String sql, Object... params) {
        CompletableFuture<Integer> future =   CompletableFuture.supplyAsync(() -> {
            try (Connection connection = source.getConnection();

                 PreparedStatement statement = bind(connection.prepareStatement(sql), params)) {
                return statement.executeUpdate();
            } catch (SQLException e) {

                throw new DatabaseException(e);
            }
        }, executor);
        return future.whenComplete((result, error) -> healthy = error == null);
    }


    public < T >  CompletableFuture<T> query( String sql, ResultSetExtractor<T> extractor, Object... params) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = source.getConnection();
                 PreparedStatement statement = bind(connection.prepareStatement(sql), params);
                 ResultSet resultSet = statement.executeQuery()) {

                return extractor.extract(resultSet);
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }, executor);
        return future.whenComplete((result, error) -> healthy = error == null);
    }



    public CompletableFuture<Long> testConnection() {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            try (Connection connection = source.getConnection();
                    PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
                 statement.executeQuery().next();
                return System.currentTimeMillis() - start;
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }, executor);
        return future.whenComplete((result, error) -> healthy = error == null);
    }



    private PreparedStatement bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int idx = 0; idx < params.length; idx++) {
            Object value = params[idx];
            int index = idx + 1;
            if (value == null) {
                statement.setNull(index, java.sql.Types.NULL);
            } else if (value instanceof String s) {
                statement.setString(index, s);
            } else if (value instanceof Integer  i) {
                statement.setInt(index, i);
            } else if (value instanceof Long l) {
                statement.setLong(index, l);
            } else if (value instanceof  Double d) {
                statement.setDouble(index, d);
            } else if (value instanceof BigDecimal    b) {
                statement.setBigDecimal(index, b);
            } else if (value instanceof Boolean bl) {
                statement.setBoolean(index, bl);
            } else if (value instanceof UUID u) {
                statement.setString(index, u.toString());
            } else {
                statement.setObject(index, value);
            }
        }
        return statement;
    }

    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (source != null && !source.isClosed()) {
            source.close();
        }
    }

    @FunctionalInterface
    public interface ResultSetExtractor<T> {
        T extract(ResultSet resultSet) throws
                SQLException;
    }

    public static class DatabaseException extends RuntimeException {
        public DatabaseException(SQLException cause) {
            super(cause);
        }
    }
}
