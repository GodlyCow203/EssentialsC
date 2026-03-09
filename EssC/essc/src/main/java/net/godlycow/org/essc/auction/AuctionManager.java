package net.godlycow.org.essc.auction;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionManager implements Listener {
    private final EssentialsC plugin;
    private final AuctionStorage storage;
    private final AuctionEconomy economy;

    private final Map<Integer, Auction> activeAuctions = new ConcurrentHashMap<>();
    private final Map<UUID, List<ItemStack>> expiredItems = new ConcurrentHashMap<>();
    private final Set<UUID> claiming = ConcurrentHashMap.newKeySet();
    private final Map<UUID, List<SellHistoryEntry>> sellHistory = new ConcurrentHashMap<>();
    private final Map<UUID, List<BuyHistoryEntry>> buyHistory = new ConcurrentHashMap<>();

    public AuctionManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.storage = new AuctionStorage(plugin);
        this.economy = new AuctionEconomy(plugin);

        try {
            storage.connect();
            loadAuctions();
            startExpiryTask();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize AuctionManager: " + e.getMessage());
        }
    }

    private void loadAuctions() {
        storage.loadActiveAuctions().thenAccept(auctions -> {
            auctions.forEach(a -> activeAuctions.put(a.getId(), a));
            plugin.debug("Loaded " + auctions.size() + " auctions");
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        storage.loadExpiredItems(e.getPlayer().getUniqueId()).thenAccept(items -> {
            if (!items.isEmpty()) {
                expiredItems.put(e.getPlayer().getUniqueId(), new ArrayList<>(items));
                e.getPlayer().sendMessage(plugin.getLanguageManager().get(e.getPlayer(),
                        "ah.expired_waiting", Map.of("count", String.valueOf(items.size()))));
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        expiredItems.remove(e.getPlayer().getUniqueId());
        claiming.remove(e.getPlayer().getUniqueId());
    }

    public CompletableFuture<Boolean> createAuction(Player seller, ItemStack item, BigDecimal price, long duration) {
        int max = plugin.getConfigManager().getAHMaxAuctions();
        long count = activeAuctions.values().stream()
                .filter(a -> a.getSellerUuid().equals(seller.getUniqueId()))
                .count();

        if (!seller.hasPermission("essentialsc.ah.bypass.limit") && count >= max) {
            return CompletableFuture.completedFuture(false);
        }

        ItemStack clone = item.clone();
        long now = System.currentTimeMillis();

        return storage.createAuction(seller.getUniqueId(), seller.getName(), clone,
                        price.doubleValue(), now, duration)
                .thenApply(id -> {
                    if (id > 0) {
                        Auction auction = new Auction(id, seller.getUniqueId(), seller.getName(),
                                clone, price, now, duration);
                        activeAuctions.put(id, auction);
                        return true;
                    }
                    return false;
                });
    }

    public CompletableFuture<Boolean> buyAuction(Player buyer, int id) {
        Auction auction = activeAuctions.get(id);
        if (auction == null || auction.isExpired() ||
                auction.getSellerUuid().equals(buyer.getUniqueId())) {
            return CompletableFuture.completedFuture(false);
        }

        return economy.processPurchase(buyer, auction)
                .thenCompose(success -> finalizePurchase(buyer, auction, success));
    }

    private CompletableFuture<Boolean> finalizePurchase(Player buyer, Auction auction, boolean success) {
        if (!success) return CompletableFuture.completedFuture(false);

        return storage.markAuctionClaimed(auction.getId())
                .thenApply(dbSuccess -> {
                    if (dbSuccess) {
                        activeAuctions.remove(auction.getId());
                        economy.unlockAuction(auction.getId());
                        economy.deliverItem(buyer, auction.getItem());
                        notifySeller(auction, buyer.getName());
                        recordHistory(auction, buyer);
                    } else {
                        economy.unlockAuction(auction.getId());
                    }
                    return dbSuccess;
                });
    }

    public CompletableFuture<Boolean> cancelAuction(Player player, int id) {
        if (!economy.tryLockAuction(id)) return CompletableFuture.completedFuture(false);

        Auction auction = activeAuctions.get(id);
        if (auction == null) {
            economy.unlockAuction(id);
            return CompletableFuture.completedFuture(false);
        }

        boolean canCancel = auction.getSellerUuid().equals(player.getUniqueId()) ||
                player.hasPermission("essentialsc.ah.admin");
        if (!canCancel) {
            economy.unlockAuction(id);
            return CompletableFuture.completedFuture(false);
        }

        return storage.markAuctionClaimed(id)
                .thenApply(success -> {
                    if (success) {
                        activeAuctions.remove(id);
                        addExpiredItem(auction.getSellerUuid(), auction.getItem());
                    }
                    economy.unlockAuction(id);
                    return success;
                });
    }

    private void addExpiredItem(UUID uuid, ItemStack item) {
        expiredItems.computeIfAbsent(uuid, k -> new ArrayList<>()).add(item);
        storage.saveExpiredItem(uuid, item);
    }

    public boolean claimExpiredItems(Player player) {
        if (!claiming.add(player.getUniqueId())) return false;

        List<ItemStack> items = expiredItems.remove(player.getUniqueId());
        if (items == null || items.isEmpty()) {
            claiming.remove(player.getUniqueId());
            return false;
        }

        storage.markExpiredItemsClaimed(player.getUniqueId())
                .thenRun(() -> claiming.remove(player.getUniqueId()));

        items.forEach(item -> economy.deliverItem(player, item));
        return true;
    }

    private void startExpiryTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            List<Auction> expired = activeAuctions.values().stream()
                    .filter(Auction::isExpired)
                    .toList();

            for (Auction auction : expired) {
                if (!economy.tryLockAuction(auction.getId())) continue;

                storage.markAuctionClaimed(auction.getId())
                        .thenRun(() -> {
                            activeAuctions.remove(auction.getId());
                            economy.unlockAuction(auction.getId());
                            addExpiredItem(auction.getSellerUuid(), auction.getItem());

                            Player seller = Bukkit.getPlayer(auction.getSellerUuid());
                            if (seller != null && seller.isOnline()) {
                                seller.sendMessage(plugin.getLanguageManager().get(seller, "ah.expired",
                                        Map.of("item", auction.getItem().getType().toString())));
                            }
                        });
            }
        }, 1200L, 1200L);
    }

    private void notifySeller(Auction auction, String buyerName) {
        Player seller = Bukkit.getPlayer(auction.getSellerUuid());
        if (seller != null && seller.isOnline()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                seller.sendMessage(plugin.getLanguageManager().get(seller, "ah.sold", Map.of(
                        "item", auction.getItem().getType().toString(),
                        "price", plugin.getEconomyManager().format(auction.getPrice()),
                        "buyer", buyerName
                )));
            });
        }
    }

    private void recordHistory(Auction auction, Player buyer) {
        SellHistoryEntry sellEntry = new SellHistoryEntry(
                auction.getId(), auction.getSellerUuid(), buyer.getName(),
                auction.getItem(), auction.getPrice(), System.currentTimeMillis()
        );
        sellHistory.computeIfAbsent(auction.getSellerUuid(), k -> new ArrayList<>()).add(0, sellEntry);
        trimHistory(sellHistory.get(auction.getSellerUuid()));

        BuyHistoryEntry buyEntry = new BuyHistoryEntry(
                auction.getId(), buyer.getUniqueId(), auction.getSellerName(),
                auction.getItem(), auction.getPrice(), System.currentTimeMillis()
        );
        buyHistory.computeIfAbsent(buyer.getUniqueId(), k -> new ArrayList<>()).add(0, buyEntry);
        trimHistory(buyHistory.get(buyer.getUniqueId()));
    }

    private <T> void trimHistory(List<T> list) {
        if (list.size() > 100) list.subList(100, list.size()).clear();
    }

    public void reload() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            activeAuctions.clear();
            expiredItems.clear();
            loadAuctions();
            Bukkit.getOnlinePlayers().forEach(p ->
                    storage.loadExpiredItems(p.getUniqueId()).thenAccept(items -> {
                        if (!items.isEmpty()) expiredItems.put(p.getUniqueId(), items);
                    }));
        });
    }

    public void shutdown() {
        storage.disconnect();
    }

    public List<Auction> getActiveAuctions() { return new ArrayList<>(activeAuctions.values()); }
    public List<Auction> getPlayerAuctions(UUID uuid) {
        return activeAuctions.values().stream()
                .filter(a -> a.getSellerUuid().equals(uuid))
                .toList();
    }
    public Optional<Auction> getAuction(int id) { return Optional.ofNullable(activeAuctions.get(id)); }
    public List<ItemStack> getExpiredItems(UUID uuid) { return expiredItems.getOrDefault(uuid, new ArrayList<>()); }

    public boolean hasExpiredItems(UUID uuid) {
        List<ItemStack> cached = expiredItems.get(uuid);
        if (cached != null && !cached.isEmpty()) {
            return true;
        }
        return false;
    }
    public List<SellHistoryEntry> getSellHistory(UUID uuid) { return sellHistory.getOrDefault(uuid, new ArrayList<>()); }
    public List<BuyHistoryEntry> getBuyHistory(UUID uuid) { return buyHistory.getOrDefault(uuid, new ArrayList<>()); }
}