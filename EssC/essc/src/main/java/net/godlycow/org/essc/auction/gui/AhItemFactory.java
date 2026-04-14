package net.godlycow.org.essc.auction.gui;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.auction.Auction;
import net.godlycow.org.essc.auction.BuyHistoryEntry;
import net.godlycow.org.essc.auction.SellHistoryEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AhItemFactory {
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final EssentialsC plugin;

    private final NamespacedKey auctionKey;
    private final NamespacedKey ownKey;
    private final NamespacedKey pageKey;
    private final NamespacedKey actionKey;
    private final NamespacedKey closeKey;
    private final NamespacedKey claimKey;
    private final NamespacedKey navKey;

    public AhItemFactory(EssentialsC plugin) {
        this.plugin = plugin;
        this.auctionKey = new NamespacedKey(plugin, "ah_id");
        this.ownKey = new NamespacedKey(plugin, "ah_own");
        this.pageKey = new NamespacedKey(plugin, "ah_page");
        this.actionKey = new NamespacedKey(plugin, "ah_action");
        this.closeKey = new NamespacedKey(plugin, "ah_close");
        this.claimKey = new NamespacedKey(plugin, "ah_claim");
        this.navKey = new NamespacedKey(plugin, "ah_nav");
    }

    public ItemStack createAuctionItem(Auction auction, Player viewer) {
        ItemStack display = auction.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        List<Component> lore = createBaseLore(viewer);

        String priceStr = plugin.getEconomyManager().format(auction.getPrice());
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.price",
                Map.of("price", priceStr)));

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.seller",
                Map.of("seller", auction.getSellerName())));

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.time_left",
                Map.of("time", formatTime(auction.getTimeRemaining()))));

        lore.add(Component.empty());
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.separator"));

        boolean isOwn = auction.getSellerUuid().equals(viewer.getUniqueId());
        if (isOwn) {
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.your_auction"));
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.right_click_cancel"));
        } else {
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.click_purchase"));
        }

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.id",
                Map.of("id", String.valueOf(auction.getId()))));

        meta.lore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(auctionKey, PersistentDataType.INTEGER, auction.getId());
        if (isOwn) container.set(ownKey, PersistentDataType.BYTE, (byte) 1);

        display.setItemMeta(meta);
        return display;
    }

    public ItemStack createOwnAuctionItem(Auction auction, Player viewer) {
        ItemStack display = auction.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        List<Component> lore = createBaseLore(viewer);

        String priceStr = plugin.getEconomyManager().format(auction.getPrice());
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.price",
                Map.of("price", priceStr)));

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.time_left",
                Map.of("time", formatTime(auction.getTimeRemaining()))));

        lore.add(Component.empty());
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.separator"));
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.right_click_cancel"));
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.id",
                Map.of("id", String.valueOf(auction.getId()))));

        meta.lore(lore);
        meta.getPersistentDataContainer().set(auctionKey, PersistentDataType.INTEGER, auction.getId());
        meta.getPersistentDataContainer().set(ownKey, PersistentDataType.BYTE, (byte) 1);
        display.setItemMeta(meta);
        return display;
    }

    public ItemStack createSellHistoryItem(SellHistoryEntry entry, Player viewer) {
        ItemStack display = entry.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        List<Component> lore = createBaseLore(viewer);

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.sold_for",
                Map.of("price", plugin.getEconomyManager().format(entry.getPrice()))));

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.buyer",
                Map.of("buyer", entry.getBuyerName())));

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.when",
                Map.of("time", formatTimeAgo(entry.getTimestamp()))));

        lore.add(Component.empty());
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.separator"));

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    public ItemStack createBuyHistoryItem(BuyHistoryEntry entry, Player viewer) {
        ItemStack display = entry.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        List<Component> lore = createBaseLore(viewer);

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.bought_for",
                Map.of("price", plugin.getEconomyManager().format(entry.getPrice()))));

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.seller_name",
                Map.of("seller", entry.getSellerName())));

        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.when",
                Map.of("time", formatTimeAgo(entry.getTimestamp()))));

        lore.add(Component.empty());
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.separator"));

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    public ItemStack createClaimableItem(ItemStack original, int slot, Player viewer) {
        ItemStack item = original.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Component.empty());
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.separator"));
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.click_claim"));
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.lore.slot",
                Map.of("slot", String.valueOf(slot))));

        meta.lore(lore);
        meta.getPersistentDataContainer().set(claimKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createNavItem(Material defaultMat, String nameKey, int targetPage, String type, Player viewer) {
        String configPath = "navigation." + (targetPage < 0 ? "prev" : "next");
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath, defaultMat);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        Component name;
        if (customName != null && !customName.isEmpty()) {
            name = mm.deserialize(customName);
        } else {
            name = plugin.getLanguageManager().get(viewer, nameKey);
        }
        meta.displayName(name);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(pageKey, PersistentDataType.INTEGER, targetPage);
        container.set(navKey, PersistentDataType.STRING, type);

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createActionItem(String configPath, Material defaultMat, String nameKey, String action, List<String> loreKeys, Player viewer) {
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath, defaultMat);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        Component name;
        if (customName != null && !customName.isEmpty()) {
            name = mm.deserialize(customName);
        } else {
            name = plugin.getLanguageManager().get(viewer, nameKey);
        }
        meta.displayName(name);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        List<Component> lore = new ArrayList<>();
        List<String> customLore = plugin.getConfigManager().getAHGuiLore(configPath, null);
        if (customLore != null && !customLore.isEmpty()) {
            for (String line : customLore) {
                lore.add(mm.deserialize(line));
            }
        } else {
            for (String key : loreKeys) {
                lore.add(plugin.getLanguageManager().get(viewer, key));
            }
        }

        meta.lore(lore);
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createInfoItem(Player player) {
        String configPath = "info";
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath, Material.PLAYER_HEAD);
        ItemStack item = new ItemStack(material);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        if (customName != null && !customName.isEmpty()) {
            meta.displayName(mm.deserialize(customName));
        } else {
            meta.displayName(plugin.getLanguageManager().get(player, "ah.gui.item.info.name"));
        }

        int active = plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId()).size();
        int max = plugin.getConfigManager().getAHMaxAuctions();
        boolean bypass = player.hasPermission("essentialsc.ah.bypass.limit");
        boolean hasExpired = plugin.getAuctionManager().hasExpiredItems(player.getUniqueId());

        List<String> customLore = plugin.getConfigManager().getAHGuiLore(configPath, null);
        if (customLore != null && !customLore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : customLore) {
                lore.add(mm.deserialize(line));
            }
            meta.lore(lore);
        } else {
            List<Component> lore = createBaseLore(player);
            String maxStr = bypass ? "∞" : String.valueOf(max);
            String color = (active >= max && !bypass) ? "<color:#EF4444>" : "<color:#10B981>";
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.item.info.lore.active",
                    Map.of("count", String.valueOf(active), "max", maxStr, "color", color)));
            if (hasExpired) {
                lore.add(plugin.getLanguageManager().get(player, "ah.gui.item.info.lore.expired"));
            }
            lore.add(Component.empty());
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.item.info.lore.help_sell"));
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.item.info.lore.help_cancel"));
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.item.info.lore.help_expired"));
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.item.info.lore.help_listings"));
            lore.add(Component.empty());
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.separator"));
            meta.lore(lore);
        }

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createStatsItem(int count, Player viewer) {
        String configPath = "stats";
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath, Material.CHEST);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        if (customName != null && !customName.isEmpty()) {
            meta.displayName(mm.deserialize(customName));
        } else {
            meta.displayName(plugin.getLanguageManager().get(viewer, "ah.gui.item.stats.name"));
        }

        List<String> customLore = plugin.getConfigManager().getAHGuiLore(configPath, null);
        if (customLore != null && !customLore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : customLore) {
                lore.add(mm.deserialize(line));
            }
            meta.lore(lore);
        } else {
            List<Component> lore = createBaseLore(viewer);
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.stats.lore.items_waiting",
                    Map.of("count", String.valueOf(count))));
            lore.add(Component.empty());
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.stats.lore.click_individual"));
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.stats.lore.claim_all"));
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.separator"));
            meta.lore(lore);
        }

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createSellHistoryStatsItem(List<SellHistoryEntry> history, Player viewer) {
        String configPath = "sell-history";
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath, Material.GOLD_INGOT);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        if (customName != null && !customName.isEmpty()) {
            meta.displayName(mm.deserialize(customName));
        } else {
            meta.displayName(plugin.getLanguageManager().get(viewer, "ah.gui.item.sell_stats.name"));
        }

        BigDecimal totalEarnings = history.stream()
                .map(SellHistoryEntry::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> customLore = plugin.getConfigManager().getAHGuiLore(configPath, null);
        if (customLore != null && !customLore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : customLore) {
                lore.add(mm.deserialize(line));
            }
            meta.lore(lore);
        } else {
            List<Component> lore = createBaseLore(viewer);
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.sell_stats.lore.total_sales",
                    Map.of("count", String.valueOf(history.size()))));
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.sell_stats.lore.total_earnings",
                    Map.of("amount", plugin.getEconomyManager().format(totalEarnings))));
            lore.add(Component.empty());
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.separator"));
            meta.lore(lore);
        }

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createBuyHistoryStatsItem(List<BuyHistoryEntry> history, Player viewer) {
        String configPath = "buy-history";
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath, Material.DIAMOND);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        if (customName != null && !customName.isEmpty()) {
            meta.displayName(mm.deserialize(customName));
        } else {
            meta.displayName(plugin.getLanguageManager().get(viewer, "ah.gui.item.buy_stats.name"));
        }

        BigDecimal totalSpent = history.stream()
                .map(BuyHistoryEntry::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> customLore = plugin.getConfigManager().getAHGuiLore(configPath, null);
        if (customLore != null && !customLore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : customLore) {
                lore.add(mm.deserialize(line));
            }
            meta.lore(lore);
        } else {
            List<Component> lore = createBaseLore(viewer);
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.buy_stats.lore.total_purchases",
                    Map.of("count", String.valueOf(history.size()))));
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.buy_stats.lore.total_spent",
                    Map.of("amount", plugin.getEconomyManager().format(totalSpent))));
            lore.add(Component.empty());
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.separator"));
            meta.lore(lore);
        }

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createCloseItem(Player viewer) {
        String configPath = "close";
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath, Material.BARRIER);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        if (customName != null && !customName.isEmpty()) {
            meta.displayName(mm.deserialize(customName));
        } else {
            meta.displayName(plugin.getLanguageManager().get(viewer, "ah.gui.item.close.name"));
        }

        meta.getPersistentDataContainer().set(closeKey, PersistentDataType.BYTE, (byte) 1);

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createEmptyItem(String type, Player viewer) {
        String configPath = type.equals("auction") ? "empty-auction" : "empty-history";
        Material material;
        if (type.equals("history") || type.equals("sell") || type.equals("buy")) {
            material = plugin.getConfigManager().getAHGuiMaterial(configPath, Material.PAPER);
        } else {
            material = plugin.getConfigManager().getAHGuiMaterial(configPath, Material.CANDLE);
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        if (customName != null && !customName.isEmpty()) {
            meta.displayName(mm.deserialize(customName));
        } else {
            meta.displayName(plugin.getLanguageManager().get(viewer, "ah.gui.item.empty." + type + ".name"));
        }

        List<String> customLore = plugin.getConfigManager().getAHGuiLore(configPath, null);
        if (customLore != null && !customLore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : customLore) {
                lore.add(mm.deserialize(line));
            }
            meta.lore(lore);
        } else {
            List<Component> lore = new ArrayList<>();
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.empty." + type + ".lore1"));
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.empty." + type + ".lore2"));
            if (type.equals("auction")) {
                lore.add(Component.empty());
                lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.empty.auction.help"));
            }
            meta.lore(lore);
        }

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createEmptyHistoryItem(String type, Player viewer) {
        String configPath = "empty-history";
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath, Material.PAPER);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        if (customName != null && !customName.isEmpty()) {
            meta.displayName(mm.deserialize(customName));
        } else {
            meta.displayName(plugin.getLanguageManager().get(viewer, "ah.gui.item.empty_history." + type + ".name"));
        }

        List<String> customLore = plugin.getConfigManager().getAHGuiLore(configPath, null);
        if (customLore != null && !customLore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : customLore) {
                lore.add(mm.deserialize(line));
            }
            meta.lore(lore);
        } else {
            List<Component> lore = new ArrayList<>();
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.empty_history." + type + ".lore1"));
            lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.item.empty_history." + type + ".lore2"));
            meta.lore(lore);
        }

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createFiller(Material defaultMat, String configPath) {
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath, defaultMat);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String configName = plugin.getConfigManager().getAHGuiName(configPath, null);
        if (configName != null && !configName.isEmpty()) {
            meta.displayName(mm.deserialize(configName));
        } else {
            meta.displayName(plugin.getLanguageManager().get(null, "ah.gui.filler.name"));
        }

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private List<Component> createBaseLore(Player viewer) {
        List<Component> lore = new ArrayList<>();
        lore.add(plugin.getLanguageManager().get(viewer, "ah.gui.separator_top"));
        lore.add(Component.empty());
        return lore;
    }

    private String formatTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long mins = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        String time = hours > 0 ? hours + "h " + mins + "m" : mins + "m";
        String color = hours < 1 ? "<color:#EF4444>" : "<color:#D1D5DB>";
        return color + time;
    }

    private String formatTimeAgo(long timestamp) {
        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - timestamp);
        long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - timestamp) % 24;
        return days > 0 ? days + "d " + hours + "h ago" : hours + "h ago";
    }

    public NamespacedKey getAuctionKey() {
        return auctionKey;
    }

    public NamespacedKey getOwnKey() {
        return ownKey;
    }

    public NamespacedKey getPageKey() {
        return pageKey;
    }

    public NamespacedKey getActionKey() {
        return actionKey;
    }

    public NamespacedKey getCloseKey() {
        return closeKey;
    }

    public NamespacedKey getClaimKey() {
        return claimKey;
    }

    public NamespacedKey getNavKey() {
        return navKey;
    }
}