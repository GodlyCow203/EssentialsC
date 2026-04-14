package net.godlycow.org.essc.auction.gui;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.auction.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AhGuiManager {
    private static final int[] AUCTION_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int PER_PAGE = 28;

    private final EssentialsC plugin;
    private final AhItemFactory items;
    private final AhSoundManager sounds;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public AhGuiManager(EssentialsC plugin, AhSoundManager sounds) {
        this.plugin = plugin;
        this.sounds = sounds;
        this.items = new AhItemFactory(plugin);
    }

    public void openMainGui(Player player, int page) {
        sounds.playOpen(player);
        List<Auction> auctions = new ArrayList<>(plugin.getAuctionManager().getActiveAuctions());
        auctions.sort(Comparator.comparingLong(Auction::getListedTime).reversed());

        int totalPages = Math.max(1, (int) Math.ceil((double) auctions.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        String title = mm.serialize(plugin.getLanguageManager().get(player, "ah.gui.title.main",
                Map.of("page", String.valueOf(page), "total", String.valueOf(totalPages))));
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE, "border");
        fillCorners(gui, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "corner");

        if (auctions.isEmpty()) {
            gui.setItem(31, items.createEmptyItem("auction", player));
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, auctions.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], items.createAuctionItem(auctions.get(i), player));
            }
        }

        gui.setItem(48, page > 1 ?
                items.createNavItem(Material.ARROW, "ah.gui.item.nav.prev.name", page - 1, "main", player) :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "border"));
        gui.setItem(50, page < totalPages ?
                items.createNavItem(Material.ARROW, "ah.gui.item.nav.next.name", page + 1, "main", player) :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "border"));

        gui.setItem(49, items.createInfoItem(player));

        boolean hasExpired = plugin.getAuctionManager().hasExpiredItems(player.getUniqueId());

        gui.setItem(45, items.createActionItem("listings", Material.CHEST, "ah.gui.item.listings.name", "listings",
                List.of("ah.gui.item.listings.lore1", "ah.gui.item.listings.lore2"), player));

        gui.setItem(46, items.createActionItem("expired", Material.CREEPER_HEAD, "ah.gui.item.expired.name", "expired",
                hasExpired ? List.of("ah.gui.item.expired.lore1", "ah.gui.item.expired.lore_waiting") :
                        List.of("ah.gui.item.expired.lore1", "ah.gui.item.expired.lore_empty"), player));

        gui.setItem(47, items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "border"));
        gui.setItem(51, items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "border"));

        gui.setItem(52, items.createActionItem("refresh", Material.CLOCK, "ah.gui.item.refresh.name", "refresh",
                List.of("ah.gui.item.refresh.lore1", "ah.gui.item.refresh.lore2"), player));

        gui.setItem(53, items.createActionItem("history", Material.BOOK, "ah.gui.item.history.name", "history",
                List.of("ah.gui.item.history.lore1", "ah.gui.item.history.lore2"), player));

        openGui(player, gui, AhSession.main(player.getUniqueId(), page));
    }

    public void openHistoryTypeGui(Player player) {
        sounds.playOpen(player);
        String title = mm.serialize(plugin.getLanguageManager().get(player, "ah.gui.title.history.select"));
        Inventory gui = Bukkit.createInventory(null, 27, mm.deserialize(title));

        for (int i = 0; i < 27; i++) gui.setItem(i, items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "border"));

        gui.setItem(11, items.createActionItem("sell-history", Material.GOLD_INGOT, "ah.gui.history.select.sell.name", "sell_history",
                List.of("ah.gui.history.select.sell.lore1", "ah.gui.history.select.sell.lore2"), player));

        gui.setItem(15, items.createActionItem("buy-history", Material.DIAMOND, "ah.gui.history.select.buy.name", "buy_history",
                List.of("ah.gui.history.select.buy.lore1", "ah.gui.history.select.buy.lore2"), player));

        gui.setItem(18, items.createNavItem(Material.ARROW, "ah.gui.item.back.name", 1, "main", player));
        gui.setItem(26, items.createCloseItem(player));

        openGui(player, gui, AhSession.historyType(player.getUniqueId()));
    }

    public void openSellHistoryGui(Player player, int page) {
        sounds.playOpen(player);
        List<SellHistoryEntry> history = plugin.getAuctionManager().getSellHistory(player.getUniqueId());

        int totalPages = Math.max(1, (int) Math.ceil((double) history.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        String title = mm.serialize(plugin.getLanguageManager().get(player, "ah.gui.title.history.sell",
                Map.of("page", String.valueOf(page), "total", String.valueOf(totalPages))));
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.GREEN_STAINED_GLASS_PANE, "sell_history_border");
        fillCorners(gui, Material.LIME_STAINED_GLASS_PANE, "sell_history_corner");

        if (history.isEmpty()) {
            gui.setItem(31, items.createEmptyHistoryItem("sell", player));
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, history.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], items.createSellHistoryItem(history.get(i), player));
            }
        }

        gui.setItem(48, page > 1 ?
                items.createNavItem(Material.ARROW, "ah.gui.item.nav.prev.name", page - 1, "sell_history", player) :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "sell_history_border"));
        gui.setItem(50, page < totalPages ?
                items.createNavItem(Material.ARROW, "ah.gui.item.nav.next.name", page + 1, "sell_history", player) :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "sell_history_border"));

        gui.setItem(49, items.createSellHistoryStatsItem(history, player));
        gui.setItem(45, items.createNavItem(Material.BOOK, "ah.gui.item.back_history.name", 1, "history_type", player));
        gui.setItem(53, items.createCloseItem(player));

        openGui(player, gui, AhSession.sellHistory(player.getUniqueId(), page));
    }

    public void openBuyHistoryGui(Player player, int page) {
        sounds.playOpen(player);
        List<BuyHistoryEntry> history = plugin.getAuctionManager().getBuyHistory(player.getUniqueId());

        int totalPages = Math.max(1, (int) Math.ceil((double) history.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        String title = mm.serialize(plugin.getLanguageManager().get(player, "ah.gui.title.history.buy",
                Map.of("page", String.valueOf(page), "total", String.valueOf(totalPages))));
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.BLUE_STAINED_GLASS_PANE, "buy_history_border");
        fillCorners(gui, Material.CYAN_STAINED_GLASS_PANE, "buy_history_corner");

        if (history.isEmpty()) {
            gui.setItem(31, items.createEmptyHistoryItem("buy", player));
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, history.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], items.createBuyHistoryItem(history.get(i), player));
            }
        }

        gui.setItem(48, page > 1 ?
                items.createNavItem(Material.ARROW, "ah.gui.item.nav.prev.name", page - 1, "buy_history", player) :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "buy_history_border"));
        gui.setItem(50, page < totalPages ?
                items.createNavItem(Material.ARROW, "ah.gui.item.nav.next.name", page + 1, "buy_history", player) :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "buy_history_border"));

        gui.setItem(49, items.createBuyHistoryStatsItem(history, player));
        gui.setItem(45, items.createNavItem(Material.BOOK, "ah.gui.item.back_history.name", 1, "history_type", player));
        gui.setItem(53, items.createCloseItem(player));

        openGui(player, gui, AhSession.buyHistory(player.getUniqueId(), page));
    }

    public void openExpiredGui(Player player) {
        List<ItemStack> expiredItems = plugin.getAuctionManager().getExpiredItems(player.getUniqueId());
        if (expiredItems.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.no_expired"));
            sounds.playError(player);
            return;
        }

        sounds.playOpen(player);
        String title = mm.serialize(plugin.getLanguageManager().get(player, "ah.gui.title.expired"));
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.RED_STAINED_GLASS_PANE, "expired_border");
        fillCorners(gui, Material.ORANGE_STAINED_GLASS_PANE, "expired_corner");

        for (int i = 0; i < expiredItems.size() && i < AUCTION_SLOTS.length; i++) {
            gui.setItem(AUCTION_SLOTS[i], items.createClaimableItem(expiredItems.get(i), i + 1, player));
        }

        gui.setItem(48, items.createNavItem(Material.ARROW, "ah.gui.item.back.name", 1, "main", player));
        gui.setItem(50, items.createActionItem("claim-all", Material.HOPPER, "ah.gui.item.claim_all.name", "claim_all",
                List.of("ah.gui.item.claim_all.lore1"), player));
        gui.setItem(49, items.createStatsItem(expiredItems.size(), player));
        gui.setItem(53, items.createCloseItem(player));

        openGui(player, gui, AhSession.expired(player.getUniqueId()));
    }

    public void openListingsGui(Player player, int page) {
        List<Auction> auctions = new ArrayList<>(plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId()));
        if (auctions.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.no_listings"));
            sounds.playError(player);
            return;
        }

        sounds.playOpen(player);
        auctions.sort(Comparator.comparingLong(Auction::getTimeRemaining));

        int totalPages = Math.max(1, (int) Math.ceil((double) auctions.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        String title = mm.serialize(plugin.getLanguageManager().get(player, "ah.gui.title.listings",
                Map.of("page", String.valueOf(page), "total", String.valueOf(totalPages))));
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.LIME_STAINED_GLASS_PANE, "listings_border");
        fillCorners(gui, Material.GREEN_STAINED_GLASS_PANE, "listings_corner");

        int start = (page - 1) * PER_PAGE;
        int end = Math.min(start + PER_PAGE, auctions.size());
        for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
            gui.setItem(AUCTION_SLOTS[i - start], items.createOwnAuctionItem(auctions.get(i), player));
        }

        gui.setItem(48, page > 1 ?
                items.createNavItem(Material.ARROW, "ah.gui.item.nav.prev.name", page - 1, "listings", player) :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "listings_border"));
        gui.setItem(50, page < totalPages ?
                items.createNavItem(Material.ARROW, "ah.gui.item.nav.next.name", page + 1, "listings", player) :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE, "listings_border"));

        gui.setItem(49, createListingsInfoItem(auctions.size(), player));
        gui.setItem(45, items.createNavItem(Material.BARRIER, "ah.gui.item.back_main.name", 1, "main", player));
        gui.setItem(53, items.createCloseItem(player));

        openGui(player, gui, AhSession.listings(player.getUniqueId(), page));
    }

    private ItemStack createListingsInfoItem(int totalItems, Player player) {
        String configPath = "listings-info";
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath + ".material", Material.PAPER);
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();

        String customName = plugin.getConfigManager().getAHGuiName(configPath, null);
        if (customName != null && !customName.isEmpty()) {
            meta.displayName(mm.deserialize(customName));
        } else {
            meta.displayName(plugin.getLanguageManager().get(player, "ah.gui.item.listings_info.name"));
        }

        List<String> customLore = plugin.getConfigManager().getAHGuiLore(configPath, null);
        if (customLore != null && !customLore.isEmpty()) {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : customLore) {
                lore.add(mm.deserialize(line));
            }
            meta.lore(lore);
        } else {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.separator"));
            lore.add(net.kyori.adventure.text.Component.empty());
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.item.listings_info.lore.total",
                    Map.of("count", String.valueOf(totalItems))));
            lore.add(net.kyori.adventure.text.Component.empty());
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.item.listings_info.lore.cancel_tip"));
            lore.add(plugin.getLanguageManager().get(player, "ah.gui.separator"));
            meta.lore(lore);
        }

        if (plugin.getConfigManager().getAHGuiGlow(configPath, false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void openGui(Player player, Inventory gui, AhSession session) {
        player.removeMetadata("ah_session", plugin);
        player.openInventory(gui);
        player.setMetadata("ah_session", new FixedMetadataValue(plugin, session));
    }

    private void fillBorder(Inventory gui, Material defaultMat, String configPath) {
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath + ".material", defaultMat);
        ItemStack border = items.createFiller(material, configPath);
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border);
            gui.setItem(45 + i, border);
        }
        for (int i = 1; i < 5; i++) {
            gui.setItem(i * 9, border);
            gui.setItem(i * 9 + 8, border);
        }
    }

    private void fillCorners(Inventory gui, Material defaultMat, String configPath) {
        Material material = plugin.getConfigManager().getAHGuiMaterial(configPath + ".material", defaultMat);
        ItemStack corner = items.createFiller(material, configPath);
        gui.setItem(0, corner);
        gui.setItem(8, corner);
        gui.setItem(45, corner);
        gui.setItem(53, corner);
    }

    public AhItemFactory getItemFactory() {
        return items;
    }
    public AhSoundManager getSoundManager() {
        return sounds;
    }
}