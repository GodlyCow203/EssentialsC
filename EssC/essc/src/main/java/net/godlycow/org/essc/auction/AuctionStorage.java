package net.godlycow.org.essc.auction;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.database.Database;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.*;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class AuctionStorage {
    private final EssentialsC plugin;
    private final Database database;

    public AuctionStorage(EssentialsC plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin, "auction.db");
    }

    public void connect() throws SQLException {
        database.connect();
        initTables();
    }

    public void disconnect() {
        database.disconnect();
    }

    private void initTables() throws SQLException {
        try (Connection conn = database.getConnection()) {
            conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS auctions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    seller_uuid TEXT NOT NULL,
                    seller_name TEXT NOT NULL,
                    item_base64 TEXT NOT NULL,
                    price REAL NOT NULL,
                    listed_time INTEGER NOT NULL,
                    duration INTEGER NOT NULL,
                    claimed BOOLEAN DEFAULT FALSE
                )
            """).execute();

            conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS expired_auctions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    seller_uuid TEXT NOT NULL,
                    item_base64 TEXT NOT NULL,
                    expired_time INTEGER NOT NULL,
                    claimed BOOLEAN DEFAULT FALSE
                )
            """).execute();
        }
    }

    public CompletableFuture<List<Auction>> loadActiveAuctions() {
        return database.async(conn -> {
            List<Auction> auctions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM auctions WHERE claimed = FALSE")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    try {
                        auctions.add(deserializeAuction(rs));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load auction #" + rs.getInt("id"));
                    }
                }
            }
            return auctions;
        });
    }

    public CompletableFuture<Integer> createAuction(UUID sellerUuid, String sellerName,
                                                    ItemStack item, double price, long listedTime, long duration) {
        String base64 = itemToBase64(item);

        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO auctions (seller_uuid, seller_name, item_base64, price, listed_time, duration)
                VALUES (?, ?, ?, ?, ?, ?)
            """, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, sellerUuid.toString());
                stmt.setString(2, sellerName);
                stmt.setString(3, base64);
                stmt.setDouble(4, price);
                stmt.setLong(5, listedTime);
                stmt.setLong(6, duration);

                if (stmt.executeUpdate() > 0) {
                    ResultSet keys = stmt.getGeneratedKeys();
                    if (keys.next()) return keys.getInt(1);
                }
                return -1;
            }
        });
    }

    public CompletableFuture<Boolean> markAuctionClaimed(int auctionId) {
        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE auctions SET claimed = TRUE WHERE id = ?")) {
                stmt.setInt(1, auctionId);
                return stmt.executeUpdate() > 0;
            }
        });
    }

    public CompletableFuture<Void> saveExpiredItem(UUID sellerUuid, ItemStack item) {
        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO expired_auctions (seller_uuid, item_base64, expired_time)
                VALUES (?, ?, ?)
            """)) {
                stmt.setString(1, sellerUuid.toString());
                stmt.setString(2, itemToBase64(item));
                stmt.setLong(3, System.currentTimeMillis());
                stmt.executeUpdate();
            }
            return null;
        });
    }

    public CompletableFuture<List<ItemStack>> loadExpiredItems(UUID uuid) {
        return database.async(conn -> {
            List<ItemStack> items = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT item_base64 FROM expired_auctions WHERE seller_uuid = ? AND claimed = FALSE")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    try {
                        items.add(itemFromBase64(rs.getString("item_base64")));
                    } catch (Exception ignored) {}
                }
            }
            return items;
        });
    }

    public CompletableFuture<Void> markExpiredItemsClaimed(UUID uuid) {
        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE expired_auctions SET claimed = TRUE WHERE seller_uuid = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            }
            return null;
        });
    }

    private Auction deserializeAuction(ResultSet rs) throws SQLException {
        return new Auction(
                rs.getInt("id"),
                UUID.fromString(rs.getString("seller_uuid")),
                rs.getString("seller_name"),
                itemFromBase64(rs.getString("item_base64")),
                java.math.BigDecimal.valueOf(rs.getDouble("price")),
                rs.getLong("listed_time"),
                rs.getLong("duration")
        );
    }

    private String itemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(out);
            data.writeObject(item);
            data.close();
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    private ItemStack itemFromBase64(String base64) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream data = new BukkitObjectInputStream(in);
            ItemStack item = (ItemStack) data.readObject();
            data.close();
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    public Database getDatabase() {
        return database;
    }
}