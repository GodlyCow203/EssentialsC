package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.event.shop.ShopCategory;
import net.godlycow.org.essc.api.event.shop.ShopItem;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShopCategoryImpl implements ShopCategory {
    private final String id;
    private final String displayName;
    private final Material icon;
    private final String textureUrl;
    private final List<String> lore;
    private final int slot;
    private final boolean enabled;
    private final String permission;
    private final net.godlycow.org.essc.shop.ShopCategory internalCategory;

    public ShopCategoryImpl(
            @NotNull String id,
            @NotNull String displayName,
            @NotNull Material icon,
            @Nullable String textureUrl,
            @NotNull List<String> lore,
            int slot,
            boolean enabled,
            @Nullable String permission,
            @NotNull net.godlycow.org.essc.shop.ShopCategory internalCategory
    ) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.icon = icon;
        this.textureUrl = textureUrl;
        this.lore = new ArrayList<>(lore);
        this.slot = slot;
        this.enabled = enabled;
        this.permission = permission;
        this.internalCategory = internalCategory;
    }

    public static ShopCategoryImpl fromInternal(net.godlycow.org.essc.shop.ShopCategory category) {
        return new ShopCategoryImpl(
                category.getId(),
                category.getDisplayName(),
                category.getIcon(),
                category.getTextureUrl(),
                category.getLore(),
                category.getSlot(),
                category.isEnabled(),
                category.getPermission(),
                category
        );
    }

    @Override @NotNull public String getId() { return id; }
    @Override @NotNull public String getDisplayName() { return displayName; }
    @Override @NotNull public Material getIcon() { return icon; }
    @Override @Nullable public String getTextureUrl() { return textureUrl; }
    @Override @NotNull public List<String> getLore() { return Collections.unmodifiableList(lore); }
    @Override public int getSlot() { return slot; }
    @Override public boolean isEnabled() { return enabled; }
    @Override @Nullable public String getPermission() { return permission; }

    @Override @NotNull
    public Map<Integer, ShopItem> getPageItems(int page) {
        Map<Integer, net.godlycow.org.essc.shop.ShopItem> internalItems = internalCategory.getPageItems(page);
        Map<Integer, ShopItem> result = new HashMap<>();
        internalItems.forEach((slot, item) -> result.put(slot, ShopItemImpl.fromInternal(item)));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public int getMaxPage() {
        return internalCategory.getMaxPage();
    }

    @Override
    public boolean hasPage(int page) {
        return internalCategory.hasPage(page);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (int page = 1; page <= getMaxPage(); page++) {
            count += internalCategory.getPageItems(page).size();
        }
        return count;
    }

    @Override @Nullable
    public ShopItem getItem(@NotNull String itemId) {
        String lowerId = itemId.toLowerCase();
        for (int page = 1; page <= getMaxPage(); page++) {
            for (net.godlycow.org.essc.shop.ShopItem item : internalCategory.getPageItems(page).values()) {
                if (item.getId().equalsIgnoreCase(lowerId)) {
                    return ShopItemImpl.fromInternal(item);
                }
            }
        }
        return null;
    }

    @Override @NotNull
    public List<ShopItem> getAllItems() {
        List<ShopItem> items = new ArrayList<>();
        for (int page = 1; page <= getMaxPage(); page++) {
            for (net.godlycow.org.essc.shop.ShopItem item : internalCategory.getPageItems(page).values()) {
                items.add(ShopItemImpl.fromInternal(item));
            }
        }
        return Collections.unmodifiableList(items);
    }

    @Override
    public String toString() {
        return "ShopCategoryImpl{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", itemCount=" + getItemCount() +
                '}';
    }
}