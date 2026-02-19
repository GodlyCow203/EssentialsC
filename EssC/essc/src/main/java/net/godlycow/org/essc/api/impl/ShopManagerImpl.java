package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.event.shop.ShopCategory;
import net.godlycow.org.essc.api.event.shop.ShopItem;
import net.godlycow.org.essc.api.event.shop.ShopManager;
import net.godlycow.org.essc.api.event.shop.open.ShopOpenEvent;
import net.godlycow.org.essc.api.event.shop.purchase.ShopPurchaseEvent;
import net.godlycow.org.essc.api.event.shop.reload.ShopReloadEvent;
import net.godlycow.org.essc.api.event.shop.sell.ShopSellEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ShopManagerImpl implements ShopManager {
    private final EssentialsC plugin;
    private final net.godlycow.org.essc.shop.ShopManager internal;

    private final Map<UUID, BigDecimal> balanceCache = new HashMap<>();
    private final Map<UUID, Long> balanceCacheTimestamps = new HashMap<>();
    private static final long CACHE_DURATION_MS = 5000;

    public ShopManagerImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.internal = plugin.getShopManager();
    }

    @Override @NotNull
    public Map<String, ShopCategory> getCategories() {
        Map<String, ShopCategory> result = new HashMap<>();
        if (internal == null) return result;

        internal.getCategories().forEach((id, cat) ->
                result.put(id, ShopCategoryImpl.fromInternal(cat))
        );
        return Collections.unmodifiableMap(result);
    }

    @Override @Nullable
    public ShopCategory getCategory(@NotNull String id) {
        if (internal == null) return null;
        net.godlycow.org.essc.shop.ShopCategory cat = internal.getCategory(id);
        return cat != null ? ShopCategoryImpl.fromInternal(cat) : null;
    }

    @Override
    public boolean hasCategory(@NotNull String id) {
        return internal != null && internal.getCategories().containsKey(id.toLowerCase());
    }

    @Override
    public int getCategoryCount() {
        return internal != null ? internal.getCategories().size() : 0;
    }

    @Override @NotNull
    public CompletableFuture<Optional<ShopItem>> findItem(@NotNull String itemId) {
        return CompletableFuture.supplyAsync(() -> {
            if (internal == null) return Optional.empty();

            String lowerId = itemId.toLowerCase();
            for (net.godlycow.org.essc.shop.ShopCategory cat : internal.getCategories().values()) {
                for (int page = 1; page <= cat.getMaxPage(); page++) {
                    for (net.godlycow.org.essc.shop.ShopItem item : cat.getPageItems(page).values()) {
                        if (item.getId().equalsIgnoreCase(lowerId)) {
                            return Optional.of(ShopItemImpl.fromInternal(item));
                        }
                    }
                }
            }
            return Optional.empty();
        });
    }

    @Override @NotNull
    public CompletableFuture<List<ShopItem>> getItemsByCategory(@NotNull String categoryId) {
        return CompletableFuture.supplyAsync(() -> {
            net.godlycow.org.essc.shop.ShopCategory cat = internal != null ? internal.getCategory(categoryId) : null;
            if (cat == null) return Collections.emptyList();

            List<ShopItem> items = new ArrayList<>();
            for (int page = 1; page <= cat.getMaxPage(); page++) {
                for (net.godlycow.org.essc.shop.ShopItem item : cat.getPageItems(page).values()) {
                    items.add(ShopItemImpl.fromInternal(item));
                }
            }
            return items;
        });
    }

    @Override
    public int getTotalItemCount() {
        if (internal == null) return 0;
        int count = 0;
        for (net.godlycow.org.essc.shop.ShopCategory cat : internal.getCategories().values()) {
            for (int page = 1; page <= cat.getMaxPage(); page++) {
                count += cat.getPageItems(page).size();
            }
        }
        return count;
    }

    @Override @NotNull
    public CompletableFuture<PurchaseResult> purchase(@NotNull Player player, @NotNull ShopItem item, int amount) {
        return purchaseInternal(player, item, amount, false);
    }

    @Override @NotNull
    public CompletableFuture<PurchaseResult> purchase(@NotNull Player player, @NotNull String itemId, int amount) {
        return findItem(itemId).thenCompose(opt -> {
            if (opt.isPresent()) {
                return purchase(player, opt.get(), amount);
            }
            return CompletableFuture.completedFuture(
                    new PurchaseResultImpl(false, "Item not found: " + itemId, 0, 0, null)
            );
        });
    }

    private CompletableFuture<PurchaseResult> purchaseInternal(Player player, ShopItem item, int amount, boolean admin) {
        double totalPrice = item.getBuyPrice() * amount;

        ShopPurchaseEvent event = new ShopPurchaseEvent(player, item, amount, totalPrice, admin);

        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<Boolean> eventFuture = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(event);
                eventFuture.complete(event.isCancelled());
            });

            boolean cancelled = eventFuture.join();

            if (cancelled) {
                String reason = event.getCancelReason() != null ? event.getCancelReason() : "Purchase cancelled";
                return new PurchaseResultImpl(false, reason, 0, 0, item);
            }

            CompletableFuture<PurchaseResult> resultFuture = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    net.godlycow.org.essc.shop.ShopItem internalItem = findInternalItem(item.getId());
                    if (internalItem == null) {
                        resultFuture.complete(new PurchaseResultImpl(false, "Item not found in internal shop", 0, 0, item));
                        return;
                    }

                    internal.processPurchase(player, internalItem, amount);
                    resultFuture.complete(new PurchaseResultImpl(true, null, amount, totalPrice, item));

                } catch (Exception e) {
                    resultFuture.complete(new PurchaseResultImpl(false, e.getMessage(), 0, 0, item));
                }
            });

            return resultFuture.join();
        });
    }

    @Override @NotNull
    public CompletableFuture<SellResult> sell(@NotNull Player player, @NotNull ShopItem item, int amount) {
        return sellInternal(player, item, amount, false);
    }

    @Override @NotNull
    public CompletableFuture<SellResult> sell(@NotNull Player player, @NotNull String itemId, int amount) {
        return findItem(itemId).thenCompose(opt -> {
            if (opt.isPresent()) {
                return sell(player, opt.get(), amount);
            }
            return CompletableFuture.completedFuture(
                    new SellResultImpl(false, "Item not found: " + itemId, 0, 0, null)
            );
        });
    }

    @Override @NotNull
    public CompletableFuture<SellResult> sellAll(@NotNull Player player, @NotNull ShopItem item) {
        CompletableFuture<Integer> countFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            int count = getPlayerItemCount(player, item);
            countFuture.complete(count);
        });

        return countFuture.thenCompose(count -> {
            if (count <= 0) {
                return CompletableFuture.completedFuture(
                        new SellResultImpl(false, "You don't have any items to sell", 0, 0, item)
                );
            }
            return sell(player, item, count);
        });
    }

    private CompletableFuture<SellResult> sellInternal(Player player, ShopItem item, int amount, boolean admin) {
        double totalPrice = item.getSellPrice() * amount;

        ShopSellEvent event = new ShopSellEvent(player, item, amount, totalPrice, admin);

        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<Boolean> eventFuture = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(event);
                eventFuture.complete(event.isCancelled());
            });

            boolean cancelled = eventFuture.join();

            if (cancelled) {
                String reason = event.getCancelReason() != null ? event.getCancelReason() : "Sale cancelled";
                return new SellResultImpl(false, reason, 0, 0, item);
            }

            CompletableFuture<SellResult> resultFuture = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    net.godlycow.org.essc.shop.ShopItem internalItem = findInternalItem(item.getId());
                    if (internalItem == null) {
                        resultFuture.complete(new SellResultImpl(false, "Item not found in internal shop", 0, 0, item));
                        return;
                    }

                    internal.processSale(player, internalItem, amount);
                    resultFuture.complete(new SellResultImpl(true, null, amount, totalPrice, item));

                } catch (Exception e) {
                    resultFuture.complete(new SellResultImpl(false, e.getMessage(), 0, 0, item));
                }
            });

            return resultFuture.join();
        });
    }

    @Override
    public int getPlayerItemCount(@NotNull Player player, @NotNull ShopItem item) {
        ItemStack checkItem = item.createComparisonItem(1);
        int count = 0;

        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null || invItem.getType().isAir()) continue;
            if (isItemMatching(invItem, checkItem)) {
                count += invItem.getAmount();
            }
        }
        return count;
    }

    private boolean isItemMatching(ItemStack playerItem, ItemStack checkItem) {
        if (playerItem.getType() != checkItem.getType()) return false;
        if (playerItem.getType().name().equals("SPAWNER") || playerItem.getType().name().equals("ENCHANTED_BOOK")) {
            return playerItem.isSimilar(checkItem);
        }
        return playerItem.isSimilar(checkItem);
    }


    @Override @NotNull
    public CompletableFuture<BigDecimal> getBalance(@NotNull Player player) {
        refreshBalanceCacheIfNeeded(player);
        return CompletableFuture.completedFuture(
                balanceCache.getOrDefault(player.getUniqueId(), BigDecimal.ZERO)
        );
    }

    @Override @NotNull
    public String formatBalance(@NotNull BigDecimal amount) {
        if (plugin.getEconomyManager() != null) {
            return plugin.getEconomyManager().format(amount);
        }
        return amount.toPlainString() + " " + getCurrencyPlural();
    }

    @Override @NotNull
    public CompletableFuture<Boolean> hasEnough(@NotNull Player player, @NotNull BigDecimal amount) {
        if (plugin.getEconomyManager() == null) {
            return CompletableFuture.completedFuture(true);
        }
        return plugin.getEconomyManager().has(player.getUniqueId(), amount);
    }

    private void refreshBalanceCacheIfNeeded(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastUpdate = balanceCacheTimestamps.get(uuid);

        if (lastUpdate == null || (now - lastUpdate) > CACHE_DURATION_MS) {
            balanceCacheTimestamps.put(uuid, now);

            if (plugin.getEconomyManager() != null) {
                plugin.getEconomyManager().getBalance(uuid).thenAccept(balance -> {
                    balanceCache.put(uuid, balance);
                    balanceCacheTimestamps.put(uuid, System.currentTimeMillis());
                });
            } else {
                balanceCache.put(uuid, BigDecimal.ZERO);
            }
        }
    }



    @Override
    public void openMainShop(@NotNull Player player) {
        net.godlycow.org.essc.api.event.shop.open.ShopOpenEvent event =
                new net.godlycow.org.essc.api.event.shop.open.ShopOpenEvent(
                        player,
                        net.godlycow.org.essc.api.event.shop.open.ShopOpenEvent.OpenContext.MAIN_MENU,
                        null,
                        1
                );

        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled() && internal != null) {
            Bukkit.getScheduler().runTask(plugin, () -> internal.openMainShop(player));
        }
    }

    @Override
    public void openCategory(@NotNull Player player, @NotNull String categoryId, int page) {
        ShopCategory category = getCategory(categoryId);

        net.godlycow.org.essc.api.event.shop.open.ShopOpenEvent event =
                new net.godlycow.org.essc.api.event.shop.open.ShopOpenEvent(
                        player,
                        net.godlycow.org.essc.api.event.shop.open. ShopOpenEvent.OpenContext.CATEGORY_VIEW,
                        category,
                        page
                );

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled() && internal != null) {
                internal.openCategory(player, categoryId, page);
            }
        });
    }

    @Override
    public void reload() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            int cats = getCategoryCount();
            int items = getTotalItemCount();

            ShopReloadEvent event = new ShopReloadEvent(null, cats, items);
            Bukkit.getPluginManager().callEvent(event);

            if (internal != null) {
                internal.reload();
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfigManager().isShopEnabled();
    }

    @Override @NotNull
    public String getCurrencySingular() {
        if (plugin.getEconomyManager() != null) {
            return plugin.getEconomyManager().currencyNameSingular();
        }
        return plugin.getConfigManager().getShopCurrencySingular();
    }

    @Override @NotNull
    public String getCurrencyPlural() {
        if (plugin.getEconomyManager() != null) {
            return plugin.getEconomyManager().currencyNamePlural();
        }
        return plugin.getConfigManager().getShopCurrencyPlural();
    }

    @Override @NotNull
    public CompletableFuture<Boolean> setItemStock(@NotNull String itemId, int stock) {
        return CompletableFuture.supplyAsync(() -> {
            net.godlycow.org.essc.shop.ShopItem item = findInternalItem(itemId);
            if (item == null) return false;

            item.setStock(stock);
            return true;
        });
    }

    @Override @NotNull
    public CompletableFuture<Boolean> setItemBuyPrice(@NotNull String itemId, double price) {
        return CompletableFuture.supplyAsync(() -> {
            net.godlycow.org.essc.shop.ShopItem item = findInternalItem(itemId);
            if (item == null) return false;

            item.setBuyPrice(price);
            return true;
        });
    }

    @Override @NotNull
    public CompletableFuture<Boolean> setItemSellPrice(@NotNull String itemId, double price) {
        return CompletableFuture.supplyAsync(() -> {
            net.godlycow.org.essc.shop.ShopItem item = findInternalItem(itemId);
            if (item == null) return false;

            item.setSellPrice(price);
            return true;
        });
    }

    private net.godlycow.org.essc.shop.ShopItem findInternalItem(String itemId) {
        if (internal == null) return null;

        String lowerId = itemId.toLowerCase();
        for (net.godlycow.org.essc.shop.ShopCategory cat : internal.getCategories().values()) {
            for (int page = 1; page <= cat.getMaxPage(); page++) {
                for (net.godlycow.org.essc.shop.ShopItem item : cat.getPageItems(page).values()) {
                    if (item.getId().equalsIgnoreCase(lowerId)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public void clearCache(UUID uuid) {
        balanceCache.remove(uuid);
        balanceCacheTimestamps.remove(uuid);
    }
    private static class PurchaseResultImpl implements PurchaseResult {
        private final boolean success;
        private final String errorMessage;
        private final int amountPurchased;
        private final double totalPrice;
        private final ShopItem item;

        public PurchaseResultImpl(boolean success, String errorMessage, int amountPurchased,
                                  double totalPrice, ShopItem item) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.amountPurchased = amountPurchased;
            this.totalPrice = totalPrice;
            this.item = item;
        }

        @Override public boolean success() { return success; }
        @Override public String getErrorMessage() { return errorMessage; }
        @Override public int getAmountPurchased() { return amountPurchased; }
        @Override public double getTotalPrice() { return totalPrice; }
        @Override public ShopItem getItem() { return item; }
    }

    private static class SellResultImpl implements SellResult {
        private final boolean success;
        private final String errorMessage;
        private final int amountSold;
        private final double totalPrice;
        private final ShopItem item;

        public SellResultImpl(boolean success, String errorMessage, int amountSold,
                              double totalPrice, ShopItem item) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.amountSold = amountSold;
            this.totalPrice = totalPrice;
            this.item = item;
        }

        @Override public boolean success() { return success; }
        @Override public String getErrorMessage() { return errorMessage; }
        @Override public int getAmountSold() { return amountSold; }
        @Override public double getTotalPrice() { return totalPrice; }
        @Override public ShopItem getItem() { return item; }
    }
}