package net.godlycow.org.essc.auction;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.database.Database;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionManager implements Listener {
    private final EssentialsC plugin;
    private final Database database;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, Auction> activeAuctions = new ConcurrentHashMap<>();
    private final Map<UUID, List<ItemStack>> expiredItems = new ConcurrentHashMap<>();
    private final Set<Integer> processingAuctions = ConcurrentHashMap.newKeySet();
    private final Set<UUID> claimingPlayers = ConcurrentHashMap.newKeySet();

    public static final String PERM_USE = "essentialsc.ah.use";
    public static final String PERM_SELL = "essentialsc.ah.sell";
    public static final String PERM_BUY = "essentialsc.ah.buy";
    public static final String PERM_CANCEL = "essentialsc.ah.cancel";
    public static final String PERM_ADMIN = "essentialsc.ah.admin";
    public static final String PERM_BYPASS_LIMIT = "essentialsc.ah.bypass.limit";
    public static final String PERM_BYPASS_PRICE_MIN = "essentialsc.ah.bypass.price.min";
    public static final String PERM_BYPASS_PRICE_MAX = "essentialsc.ah.bypass.price.max";
    public static final String PERM_RELOAD = "essentialsc.ah.reload";

    public AuctionManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin, "auction.db");

        try {
            database.connect();
            createTables();
            loadActiveAuctionsSync();
            startExpiryTask();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.debug("AuctionManager initialized");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize auction database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
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
            """)) {
            stmt.execute();
        }

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS expired_auctions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    seller_uuid TEXT NOT NULL,
                    item_base64 TEXT NOT NULL,
                    expired_time INTEGER NOT NULL,
                    claimed BOOLEAN DEFAULT FALSE
                )
            """)) {
            stmt.execute();
        }
    }

    private void loadActiveAuctionsSync() throws SQLException {
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, seller_uuid, seller_name, item_base64, price, listed_time, duration FROM auctions WHERE claimed = FALSE"
             )) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                try {
                    int id = rs.getInt("id");
                    Auction auction = new Auction(
                            id,
                            UUID.fromString(rs.getString("seller_uuid")),
                            rs.getString("seller_name"),
                            itemFromBase64(rs.getString("item_base64")),
                            BigDecimal.valueOf(rs.getDouble("price")),
                            rs.getLong("listed_time"),
                            rs.getLong("duration")
                    );
                    activeAuctions.put(id, auction);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load auction #" + rs.getInt("id") + ": " + e.getMessage());
                }
            }

            plugin.debug("Loaded " + activeAuctions.size() + " active auctions");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadExpiredItems(player.getUniqueId()).thenAccept(items -> {
            if (!items.isEmpty()) {
                expiredItems.put(player.getUniqueId(), new ArrayList<>(items));
                int count = items.size();
                player.sendMessage(plugin.getLanguageManager().get(player, "ah.expired_items_waiting",
                        Map.of("count", String.valueOf(count))));
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        expiredItems.remove(event.getPlayer().getUniqueId());
        claimingPlayers.remove(event.getPlayer().getUniqueId());
    }

    public CompletableFuture<Boolean> createAuction(Player seller, ItemStack item, BigDecimal price, long durationMs) {
        if (!seller.hasPermission(PERM_SELL)) {
            seller.sendMessage(plugin.getLanguageManager().get(seller, "error.no_permission"));
            return CompletableFuture.completedFuture(false);
        }

        int maxAuctions = plugin.getConfigManager().getAHMaxAuctions();

        if (!seller.hasPermission(PERM_BYPASS_LIMIT)) {
            long playerAuctionCount = activeAuctions.values().stream()
                    .filter(a -> a.getSellerUuid().equals(seller.getUniqueId()))
                    .count();

            if (playerAuctionCount >= maxAuctions) {
                return CompletableFuture.completedFuture(false);
            }
        }

        long now = System.currentTimeMillis();
        ItemStack auctionItem = item.clone();
        String itemBase64 = itemToBase64(auctionItem);

        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO auctions (seller_uuid, seller_name, item_base64, price, listed_time, duration)
                VALUES (?, ?, ?, ?, ?, ?)
            """, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, seller.getUniqueId().toString());
                stmt.setString(2, seller.getName());
                stmt.setString(3, itemBase64);
                stmt.setDouble(4, price.doubleValue());
                stmt.setLong(5, now);
                stmt.setLong(6, durationMs);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int id = generatedKeys.getInt(1);
                            Auction auction = new Auction(id, seller.getUniqueId(), seller.getName(),
                                    auctionItem, price, now, durationMs);
                            activeAuctions.put(id, auction);
                            plugin.debug("Created auction #" + id + " for " + seller.getName());
                            return true;
                        }
                    }
                }
                return false;
            }
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to create auction: " + ex.getMessage());
            return false;
        });
    }

    public CompletableFuture<Boolean> buyAuction(Player buyer, int auctionId) {
        if (!buyer.hasPermission(PERM_BUY)) {
            buyer.sendMessage(plugin.getLanguageManager().get(buyer, "error.no_permission"));
            return CompletableFuture.completedFuture(false);
        }

        if (!processingAuctions.add(auctionId)) {
            return CompletableFuture.completedFuture(false);
        }

        Auction auction = activeAuctions.get(auctionId);
        if (auction == null || auction.isExpired()) {
            processingAuctions.remove(auctionId);
            return CompletableFuture.completedFuture(false);
        }

        if (auction.getSellerUuid().equals(buyer.getUniqueId())) {
            processingAuctions.remove(auctionId);
            return CompletableFuture.completedFuture(false);
        }

        return plugin.getEconomyManager().has(buyer.getUniqueId(), auction.getPrice())
                .thenCompose(hasEnough -> {
                    if (!hasEnough) {
                        processingAuctions.remove(auctionId);
                        return CompletableFuture.completedFuture(false);
                    }

                    return plugin.getEconomyManager().withdraw(buyer.getUniqueId(), auction.getPrice())
                            .thenCompose(withdrawSuccess -> {
                                if (!withdrawSuccess) {
                                    processingAuctions.remove(auctionId);
                                    return CompletableFuture.completedFuture(false);
                                }

                                return plugin.getEconomyManager().deposit(auction.getSellerUuid(), auction.getPrice())
                                        .thenCompose(depositSuccess -> {
                                            if (!depositSuccess) {
                                                plugin.getEconomyManager().deposit(buyer.getUniqueId(), auction.getPrice());
                                                processingAuctions.remove(auctionId);
                                                return CompletableFuture.completedFuture(false);
                                            }

                                            return database.async(conn -> {
                                                try (PreparedStatement stmt = conn.prepareStatement(
                                                        "UPDATE auctions SET claimed = TRUE WHERE id = ?"
                                                )) {
                                                    stmt.setInt(1, auctionId);
                                                    return stmt.executeUpdate() > 0;
                                                }
                                            }).thenApply(success -> {
                                                if (success) {
                                                    activeAuctions.remove(auctionId);
                                                    processingAuctions.remove(auctionId);

                                                    Map<Integer, ItemStack> overflow = buyer.getInventory().addItem(auction.getItem());
                                                    for (ItemStack drop : overflow.values()) {
                                                        buyer.getWorld().dropItemNaturally(buyer.getLocation(), drop);
                                                    }

                                                    Player seller = Bukkit.getPlayer(auction.getSellerUuid());
                                                    if (seller != null && seller.isOnline()) {
                                                        seller.sendMessage(plugin.getLanguageManager().get(seller, "ah.sold",
                                                                Map.of("item", auction.getItem().getType().toString(),
                                                                        "price", plugin.getEconomyManager().format(auction.getPrice()),
                                                                        "buyer", buyer.getName())));
                                                    }
                                                } else {
                                                    processingAuctions.remove(auctionId);
                                                }
                                                return success;
                                            });
                                        });
                            });
                }).exceptionally(ex -> {
                    processingAuctions.remove(auctionId);
                    plugin.getLogger().severe("Error in buyAuction: " + ex.getMessage());
                    return false;
                });
    }

    public CompletableFuture<Boolean> cancelAuction(Player seller, int auctionId) {
        if (!seller.hasPermission(PERM_CANCEL)) {
            seller.sendMessage(plugin.getLanguageManager().get(seller, "error.no_permission"));
            return CompletableFuture.completedFuture(false);
        }

        if (!processingAuctions.add(auctionId)) {
            return CompletableFuture.completedFuture(false);
        }

        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            processingAuctions.remove(auctionId);
            return CompletableFuture.completedFuture(false);
        }

        if (!auction.getSellerUuid().equals(seller.getUniqueId()) && !seller.hasPermission(PERM_ADMIN)) {
            processingAuctions.remove(auctionId);
            return CompletableFuture.completedFuture(false);
        }

        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE auctions SET claimed = TRUE WHERE id = ?"
            )) {
                stmt.setInt(1, auctionId);
                return stmt.executeUpdate() > 0;
            }
        }).thenApply(success -> {
            if (success) {
                activeAuctions.remove(auctionId);
                processingAuctions.remove(auctionId);
                addExpiredItem(auction.getSellerUuid(), auction.getItem());
            } else {
                processingAuctions.remove(auctionId);
            }
            return success;
        }).exceptionally(ex -> {
            processingAuctions.remove(auctionId);
            plugin.getLogger().severe("Error in cancelAuction: " + ex.getMessage());
            return false;
        });
    }

    public List<Auction> getActiveAuctions() {
        return new ArrayList<>(activeAuctions.values());
    }

    public List<Auction> getPlayerAuctions(UUID playerUuid) {
        return activeAuctions.values().stream()
                .filter(a -> a.getSellerUuid().equals(playerUuid))
                .toList();
    }

    public Optional<Auction> getAuction(int id) {
        return Optional.ofNullable(activeAuctions.get(id));
    }

    public void addExpiredItem(UUID playerUuid, ItemStack item) {
        List<ItemStack> items = expiredItems.computeIfAbsent(playerUuid, k -> new ArrayList<>());
        items.add(item);

        database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO expired_auctions (seller_uuid, item_base64, expired_time)
                VALUES (?, ?, ?)
            """)) {
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, itemToBase64(item));
                stmt.setLong(3, System.currentTimeMillis());
                stmt.executeUpdate();
                return null;
            }
        });
    }

    private CompletableFuture<List<ItemStack>> loadExpiredItems(UUID playerUuid) {
        return database.async(conn -> {
            List<ItemStack> items = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT item_base64 FROM expired_auctions WHERE seller_uuid = ? AND claimed = FALSE"
            )) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    try {
                        items.add(itemFromBase64(rs.getString("item_base64")));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load expired item: " + e.getMessage());
                    }
                }
            }
            return items;
        });
    }

    public List<ItemStack> getPlayerExpiredItems(UUID playerUuid) {
        return expiredItems.getOrDefault(playerUuid, new ArrayList<>());
    }

    public boolean claimExpiredItems(Player player) {
        if (!claimingPlayers.add(player.getUniqueId())) {
            return false;
        }

        List<ItemStack> items = expiredItems.remove(player.getUniqueId());
        if (items == null || items.isEmpty()) {
            claimingPlayers.remove(player.getUniqueId());
            return false;
        }

        database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE expired_auctions SET claimed = TRUE WHERE seller_uuid = ? AND claimed = FALSE"
            )) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.executeUpdate();
            }
            return null;
        }).thenRun(() -> {
            claimingPlayers.remove(player.getUniqueId());
        });

        for (ItemStack item : items) {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        return true;
    }

    private void startExpiryTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            List<Auction> expired = activeAuctions.values().stream()
                    .filter(Auction::isExpired)
                    .toList();

            for (Auction auction : expired) {
                final int auctionId = auction.getId();

                if (!processingAuctions.add(auctionId)) {
                    continue;
                }

                final UUID sellerUuid = auction.getSellerUuid();
                final ItemStack item = auction.getItem().clone();
                final String itemType = auction.getItem().getType().toString();

                database.async(conn -> {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE auctions SET claimed = TRUE WHERE id = ?"
                    )) {
                        stmt.setInt(1, auctionId);
                        stmt.executeUpdate();
                    }
                    return null;
                }).thenRun(() -> {
                    activeAuctions.remove(auctionId);
                    processingAuctions.remove(auctionId);
                    addExpiredItem(sellerUuid, item);

                    Player seller = Bukkit.getPlayer(sellerUuid);
                    if (seller != null && seller.isOnline()) {
                        seller.sendMessage(plugin.getLanguageManager().get(seller, "ah.expired",
                                Map.of("item", itemType)));
                    }
                }).exceptionally(ex -> {
                    processingAuctions.remove(auctionId);
                    plugin.getLogger().warning("Error processing expired auction #" + auctionId + ": " + ex.getMessage());
                    return null;
                });
            }
        }, 20L * 60, 20L * 60);
    }

    private String itemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize item", e);
        }
    }

    private ItemStack itemFromBase64(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize item", e);
        }
    }

    public void reload() {
        plugin.debug("Reloading AuctionManager...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                activeAuctions.clear();
                expiredItems.clear();
                processingAuctions.clear();
                loadActiveAuctionsSync();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    loadExpiredItems(player.getUniqueId()).thenAccept(items -> {
                        if (!items.isEmpty()) {
                            expiredItems.put(player.getUniqueId(), new ArrayList<>(items));
                        }
                    });
                }

                plugin.debug("AuctionManager reload complete");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to reload AuctionManager: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        database.disconnect();
    }
}