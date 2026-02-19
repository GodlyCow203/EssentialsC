package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.event.shop.ShopItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ShopItemImpl implements ShopItem {
    private final String id;
    private final String category;
    private final Material material;
    private final int amount;
    private final String displayName;
    private final List<String> lore;
    private final double buyPrice;
    private final double sellPrice;
    private final boolean buyable;
    private final boolean sellable;
    private final int stock;
    private final int maxStack;
    private final int slot;
    private final int page;
    private final String permission;
    private final boolean glow;
    private final boolean spawner;
    private final String spawnerType;
    private final boolean enchantedBook;
    private final List<String> commands;

    public ShopItemImpl(
            @NotNull String id,
            @Nullable String category,
            @NotNull Material material,
            int amount,
            @Nullable String displayName,
            @NotNull List<String> lore,
            double buyPrice,
            double sellPrice,
            boolean buyable,
            boolean sellable,
            int stock,
            int maxStack,
            int slot,
            int page,
            @Nullable String permission,
            boolean glow,
            boolean spawner,
            @Nullable String spawnerType,
            boolean enchantedBook,
            @NotNull List<String> commands
    ) {
        this.id = id.toLowerCase();
        this.category = category != null ? category.toLowerCase() : null;
        this.material = material;
        this.amount = amount;
        this.displayName = displayName;
        this.lore = new ArrayList<>(lore);
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.buyable = buyable;
        this.sellable = sellable;
        this.stock = stock;
        this.maxStack = maxStack;
        this.slot = slot;
        this.page = page;
        this.permission = permission;
        this.glow = glow;
        this.spawner = spawner;
        this.spawnerType = spawnerType;
        this.enchantedBook = enchantedBook;
        this.commands = new ArrayList<>(commands);
    }

    public static ShopItemImpl fromInternal(net.godlycow.org.essc.shop.ShopItem item) {
        return new ShopItemImpl(
                item.getId(),
                item.getCategory(),
                item.getMaterial(),
                item.getAmount(),
                item.getDisplayName(),
                item.getLore(),
                item.getBuyPrice(),
                item.getSellPrice(),
                item.isBuyable(),
                item.isSellable(),
                item.getStock(),
                item.getMaxStack(),
                item.getSlot(),
                item.getPage(),
                item.getPermission(),
                item.isGlow(),
                item.isSpawner(),
                item.getSpawnerType(),
                item.isEnchantedBook(),
                item.getCommands()
        );
    }

    @Override @NotNull public String getId() { return id; }
    @Override @Nullable public String getCategory() { return category; }
    @Override @NotNull public Material getMaterial() { return material; }
    @Override public int getAmount() { return amount; }
    @Override @Nullable public String getDisplayName() { return displayName; }
    @Override @NotNull public List<String> getLore() { return Collections.unmodifiableList(lore); }
    @Override public double getBuyPrice() { return buyPrice; }
    @Override public double getSellPrice() { return sellPrice; }
    @Override public boolean isBuyable() { return buyable; }
    @Override public boolean isSellable() { return sellable; }
    @Override public int getStock() { return stock; }
    @Override public int getMaxStack() { return maxStack; }
    @Override public int getSlot() { return slot; }
    @Override public int getPage() { return page; }
    @Override @Nullable public String getPermission() { return permission; }
    @Override public boolean isGlow() { return glow; }
    @Override public boolean isSpawner() { return spawner; }
    @Override @Nullable public String getSpawnerType() { return spawnerType; }
    @Override public boolean isEnchantedBook() { return enchantedBook; }
    @Override @NotNull public List<String> getCommands() { return Collections.unmodifiableList(commands); }

    @Override @NotNull
    public ItemStack createItemStack() {
        net.godlycow.org.essc.shop.ShopItem internal = toInternal();
        return internal.createItemStack();
    }

    @Override @NotNull
    public ItemStack createComparisonItem(int amount) {
        net.godlycow.org.essc.shop.ShopItem internal = toInternal();
        return internal.createComparisonItem(amount);
    }

    @Override @NotNull
    public ShopItem withBuyPrice(double price) {
        return new ShopItemImpl(
                id, category, material, amount, displayName, lore,
                price, sellPrice, buyable, sellable, stock, maxStack,
                slot, page, permission, glow, spawner, spawnerType,
                enchantedBook, commands
        );
    }

    @Override @NotNull
    public ShopItem withSellPrice(double price) {
        return new ShopItemImpl(
                id, category, material, amount, displayName, lore,
                buyPrice, price, buyable, sellable, stock, maxStack,
                slot, page, permission, glow, spawner, spawnerType,
                enchantedBook, commands
        );
    }

    @Override @NotNull
    public ShopItem withStock(int stock) {
        return new ShopItemImpl(
                id, category, material, amount, displayName, lore,
                buyPrice, sellPrice, buyable, sellable, stock, maxStack,
                slot, page, permission, glow, spawner, spawnerType,
                enchantedBook, commands
        );
    }

    @Override @NotNull
    public ShopItem withCategory(@NotNull String category) {
        return new ShopItemImpl(
                id, category.toLowerCase(), material, amount, displayName, lore,
                buyPrice, sellPrice, buyable, sellable, stock, maxStack,
                slot, page, permission, glow, spawner, spawnerType,
                enchantedBook, commands
        );
    }

    private net.godlycow.org.essc.shop.ShopItem toInternal() {
        net.godlycow.org.essc.shop.ShopItem internal = new net.godlycow.org.essc.shop.ShopItem(id);
        internal.setCategory(category);
        internal.setMaterial(material);
        internal.setAmount(amount);
        internal.setDisplayName(displayName);
        internal.setLore(new ArrayList<>(lore));
        internal.setBuyPrice(buyPrice);
        internal.setSellPrice(sellPrice);
        internal.setBuyable(buyable);
        internal.setSellable(sellable);
        internal.setStock(stock);
        internal.setMaxStack(maxStack);
        internal.setSlot(slot);
        internal.setPage(page);
        internal.setPermission(permission);
        internal.setGlow(glow);
        internal.setSpawner(spawner);
        internal.setSpawnerType(spawnerType);
        internal.setEnchantedBook(enchantedBook);
        internal.setCommands(new ArrayList<>(commands));
        return internal;
    }

    @Override
    public String toString() {
        return "ShopItemImpl{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", material=" + material +
                ", buyPrice=" + buyPrice +
                ", sellPrice=" + sellPrice +
                ", stock=" + stock +
                '}';
    }
}