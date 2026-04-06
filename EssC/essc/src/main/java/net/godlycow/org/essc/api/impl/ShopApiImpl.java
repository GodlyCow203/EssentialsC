package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.ShopApi;
import net.godlycow.org.essc.api.shop.ShopCategoryEntry;
import net.godlycow.org.essc.api.shop.ShopItemEntry;
import net.godlycow.org.essc.shop.ShopCategory;
import net.godlycow.org.essc.shop.ShopItem;
import net.godlycow.org.essc.shop.ShopManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ShopApiImpl implements ShopApi {

    private final ShopManager manager;

    public ShopApiImpl(ShopManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean isEnabled() {
        return manager != null;
    }

    @Override
    public List<String> getCategoryIds() {
        return List.copyOf(manager.getCategories().keySet());
    }

    @Override
    public Optional<ShopCategoryEntry> getCategory(String id) {
        return Optional.ofNullable(manager.getCategory(id)).map(this::mapToEntry);
    }

    @Override
    public List<ShopCategoryEntry> getAllCategories() {
        return manager.getCategories().values().stream()
                .map(this::mapToEntry)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ShopItemEntry> getItem(String categoryId, String itemId) {
        ShopCategory category = manager.getCategory(categoryId);
        if (category == null) return Optional.empty();

        for (int page = 1; page <= category.getMaxPage(); page++) {
            for (ShopItem item : category.getPageItems(page).values()) {
                if (item.getId().equals(itemId)) {
                    return Optional.of(mapToEntry(item));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ShopItemEntry> getItemsInCategory(String categoryId) {
        ShopCategory category = manager.getCategory(categoryId);
        if (category == null) return List.of();

        List<ShopItemEntry> items = new ArrayList<>();
        for (int page = 1; page <= category.getMaxPage(); page++) {
            for (ShopItem item : category.getPageItems(page).values()) {
                items.add(mapToEntry(item));
            }
        }
        return items;
    }

    @Override
    public void openMainShop(Player player) {
        manager.openMainShop(player);
    }

    @Override
    public void openCategory(Player player, String categoryId, int page) {
        manager.openCategory(player, categoryId, page);
    }

    @Override
    public CompletableFuture<Boolean> purchase(Player player, String itemId, int amount) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        for (ShopCategory category : manager.getCategories().values()) {
            for (int page = 1; page <= category.getMaxPage(); page++) {
                for (ShopItem item : category.getPageItems(page).values()) {
                    if (item.getId().equals(itemId)) {
                        manager.processPurchase(player, item, amount);
                        future.complete(true);
                        return future;
                    }
                }
            }
        }

        future.complete(false);
        return future;
    }

    @Override
    public CompletableFuture<Boolean> sell(Player player, String itemId, int amount) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        for (ShopCategory category : manager.getCategories().values()) {
            for (int page = 1; page <= category.getMaxPage(); page++) {
                for (ShopItem item : category.getPageItems(page).values()) {
                    if (item.getId().equals(itemId)) {
                        manager.processSale(player, item, amount);
                        future.complete(true);
                        return future;
                    }
                }
            }
        }

        future.complete(false);
        return future;
    }

    @Override
    public void reload() {
        manager.reload();
    }

    private ShopCategoryEntry mapToEntry(ShopCategory c) {
        return new ShopCategoryEntry(
                c.getId(),
                c.getDisplayName(),
                c.getIcon(),
                c.getTextureUrl(),
                List.copyOf(c.getLore()),
                c.getSlot(),
                c.isEnabled(),
                c.getPermission(),
                c.getMaxPage()
        );
    }

    private ShopItemEntry mapToEntry(ShopItem i) {
        return new ShopItemEntry(
                i.getId(),
                i.getCategory(),
                i.getMaterial(),
                i.getAmount(),
                i.getDisplayName(),
                List.copyOf(i.getLore()),
                i.getBuyPrice(),
                i.getSellPrice(),
                i.isBuyable(),
                i.isSellable(),
                i.getSlot(),
                i.getPage(),
                i.getPermission(),
                i.getStock(),
                i.getMaxStack(),
                i.isSpawner(),
                i.getSpawnerType(),
                i.isEnchantedBook(),
                Map.copyOf(i.getEnchantments()),
                Map.copyOf(i.getStoredEnchantments()),
                List.copyOf(i.getCommands())
        );
    }
}