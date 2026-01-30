package net.godlycow.org.essc.economy;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.database.Database;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager implements EconomyService, Listener {
    private final EssentialsC plugin;
    private final Database database;
    private final Map<UUID, BigDecimal> cache = new ConcurrentHashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    private String currencySingular;
    private String currencyPlural;
    private BigDecimal startingBalance;
    private String formatPattern;
    private BigDecimal minTransaction;
    private BigDecimal maxBalance;
    private DecimalFormat decimalFormat;

    public EconomyManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin);
        loadConfig();

        try {
            database.connect();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.debug("EconomyManager initialized");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize economy database: " + e.getMessage());
        }
    }

    public void reload() {
        plugin.debug("Reloading economy configuration...");
        loadConfig();
        cache.clear();
    }

    private void loadConfig() {
        var config = plugin.getConfig();
        this.currencySingular = config.getString("economy.currency.singular", "Dollar");
        this.currencyPlural = config.getString("economy.currency.plural", "Dollars");
        this.startingBalance = new BigDecimal(config.getString("economy.starting-balance", "100.00"));
        this.formatPattern = config.getString("economy.format", "#,##0.00");
        this.minTransaction = new BigDecimal(config.getString("economy.minimum-transaction", "0.01"));

        String maxStr = config.getString("economy.max-balance", "-1");
        this.maxBalance = maxStr.equals("-1") ? null : new BigDecimal(maxStr);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        this.decimalFormat = new DecimalFormat(formatPattern, symbols);

        plugin.debug("Economy config loaded: format=" + formatPattern + ", min=" + minTransaction +
                ", max=" + (maxBalance != null ? maxBalance : "unlimited"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createAccount(player.getUniqueId(), player.getName()).thenAccept(success -> {
            if (success) {
                plugin.debug("Created/Verified economy account for " + player.getName());
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> hasAccount(UUID uuid) {
        return database.async(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM economy WHERE uuid = ?"
            )) {
                ps.setString(1, uuid.toString());
                return ps.executeQuery().next();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> createAccount(UUID uuid, String name) {
        return database.async(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT OR IGNORE INTO economy (uuid, username, balance) VALUES (?, ?, ?)"
            )) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.setDouble(3, startingBalance.doubleValue());
                return ps.executeUpdate() > 0;
            }
        });
    }

    @Override
    public CompletableFuture<BigDecimal> getBalance(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(cache.get(uuid));
        }

        return database.async(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM economy WHERE uuid = ?"
            )) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    BigDecimal balance = BigDecimal.valueOf(rs.getDouble("balance"));
                    cache.put(uuid, balance);
                    return balance;
                }
                return BigDecimal.ZERO;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> has(UUID uuid, BigDecimal amount) {
        return getBalance(uuid).thenApply(balance ->
                balance.compareTo(amount) >= 0
        );
    }

    @Override
    public CompletableFuture<Boolean> withdraw(UUID uuid, BigDecimal amount) {
        if (amount.compareTo(minTransaction) < 0) {
            return CompletableFuture.completedFuture(false);
        }

        return database.async(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE economy SET balance = balance - ?, last_updated = strftime('%s', 'now') WHERE uuid = ? AND balance >= ?"
            )) {
                ps.setDouble(1, amount.doubleValue());
                ps.setString(2, uuid.toString());
                ps.setDouble(3, amount.doubleValue());
                boolean success = ps.executeUpdate() > 0;
                if (success) cache.remove(uuid);
                return success;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deposit(UUID uuid, BigDecimal amount) {
        if (amount.compareTo(minTransaction) < 0) {
            return CompletableFuture.completedFuture(false);
        }

        if (maxBalance != null) {
            try {
                BigDecimal current = getBalance(uuid).get();
                if (current.add(amount).compareTo(maxBalance) > 0) {
                    return CompletableFuture.completedFuture(false);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check balance for max limit: " + e.getMessage());
            }
        }

        return database.async(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE economy SET balance = balance + ?, last_updated = strftime('%s', 'now') WHERE uuid = ?"
            )) {
                ps.setDouble(1, amount.doubleValue());
                ps.setString(2, uuid.toString());
                int updated = ps.executeUpdate();

                if (updated > 0) {
                    cache.remove(uuid);
                    return true;
                }
            }

            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) {
                plugin.getLogger().warning("Cannot deposit to " + uuid + " - player name not found");
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO economy (uuid, username, balance) VALUES (?, ?, ?)"
            )) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.setDouble(3, amount.doubleValue());
                cache.remove(uuid);
                return ps.executeUpdate() > 0;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> setBalance(UUID uuid, BigDecimal amount) {
        if (maxBalance != null && amount.compareTo(maxBalance) > 0) {
            return CompletableFuture.completedFuture(false);
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return CompletableFuture.completedFuture(false);
        }

        return database.async(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE economy SET balance = ?, last_updated = strftime('%s', 'now') WHERE uuid = ?"
            )) {
                ps.setDouble(1, amount.doubleValue());
                ps.setString(2, uuid.toString());
                cache.remove(uuid);
                return ps.executeUpdate() > 0;
            }
        });
    }

    @Override
    public CompletableFuture<Map<UUID, BigDecimal>> getTopBalances(int limit) {
        return database.async(conn -> {
            Map<UUID, BigDecimal> top = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT uuid, balance FROM economy ORDER BY balance DESC LIMIT ?"
            )) {
                ps.setInt(1, limit);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    top.put(
                            UUID.fromString(rs.getString("uuid")),
                            BigDecimal.valueOf(rs.getDouble("balance"))
                    );
                }
            }
            return top;
        });
    }

    @Override
    public String format(BigDecimal amount) {
        return decimalFormat.format(amount) + " " +
                (amount.compareTo(BigDecimal.ONE) == 0 ? currencySingular : currencyPlural);
    }

    public String formatPlain(BigDecimal amount) {
        return decimalFormat.format(amount);
    }

    @Override
    public String currencyNameSingular() {
        return currencySingular;
    }

    @Override
    public String currencyNamePlural() {
        return currencyPlural;
    }

    @Override
    public boolean isVault() {
        return false;
    }

    public BigDecimal getMinTransaction() {
        return minTransaction;
    }

    public BigDecimal getMaxBalance() {
        return maxBalance;
    }

    public boolean hasMaxBalance() {
        return maxBalance != null;
    }

    public void shutdown() {
        database.disconnect();
    }
}