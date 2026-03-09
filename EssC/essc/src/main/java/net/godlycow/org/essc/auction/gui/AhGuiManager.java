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

        String title = "<color:#9CA3AF>Auction House <color:#6B7280>| <color:#9CA3AF>Page " + page + "/" + totalPages;
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE);
        fillCorners(gui, Material.LIGHT_GRAY_STAINED_GLASS_PANE);

        if (auctions.isEmpty()) {
            gui.setItem(31, items.createEmptyItem("No Auctions Available"));
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, auctions.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], items.createAuctionItem(auctions.get(i), player));
            }
        }

        gui.setItem(48, page > 1 ?
                items.createNavItem(Material.ARROW, "<color:#9CA3AF>← Previous Page", page - 1, "main") :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(50, page < totalPages ?
                items.createNavItem(Material.ARROW, "<color:#9CA3AF>Next Page →", page + 1, "main") :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE));

        gui.setItem(49, items.createInfoItem(player));

        boolean hasExpired = plugin.getAuctionManager().hasExpiredItems(player.getUniqueId());
        gui.setItem(45, items.createActionItem(Material.CHEST, "<color:#10B981>Your Listings", "listings",
                List.of("<color:#6B7280>View your active auctions", "<color:#6B7280>Click to manage")));
        gui.setItem(46, items.createActionItem(Material.CREEPER_HEAD, "<color:#F59E0B>Expired Items", "expired",
                List.of("<color:#6B7280>Claim returned items",
                        hasExpired ? "<color:#EF4444>⚠ You have items waiting!" : "<color:#6B7280>No items waiting")));
        gui.setItem(47, items.createFiller(Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(51, items.createFiller(Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(52, items.createActionItem(Material.CLOCK, "<color:#3B82F6>Refresh", "refresh",
                List.of("<color:#6B7280>Click to refresh", "<color:#6B7280>the auction house")));
        gui.setItem(53, items.createActionItem(Material.BOOK, "<color:#8B5CF6>History", "history",
                List.of("<color:#6B7280>View your buy/sell", "<color:#6B7280>transaction history")));

        openGui(player, gui, AhSession.main(player.getUniqueId(), page));
    }

    public void openHistoryTypeGui(Player player) {
        sounds.playOpen(player);
        String title = "<color:#9CA3AF>History <color:#6B7280>| <color:#9CA3AF>Select Type";
        Inventory gui = Bukkit.createInventory(null, 27, mm.deserialize(title));

        for (int i = 0; i < 27; i++) gui.setItem(i, items.createFiller(Material.GRAY_STAINED_GLASS_PANE));

        gui.setItem(11, items.createActionItem(Material.GOLD_INGOT, "<color:#10B981>Sell History", "sell_history",
                List.of("<color:#6B7280>View all your sold", "<color:#6B7280>auctions and earnings")));
        gui.setItem(15, items.createActionItem(Material.DIAMOND, "<color:#3B82F6>Buy History", "buy_history",
                List.of("<color:#6B7280>View all your purchases", "<color:#6B7280>and spending")));
        gui.setItem(18, items.createNavItem(Material.ARROW, "<color:#9CA3AF>← Back to Auction House", 1, "main"));
        gui.setItem(26, items.createCloseItem());

        openGui(player, gui, AhSession.historyType(player.getUniqueId()));
    }

    public void openSellHistoryGui(Player player, int page) {
        sounds.playOpen(player);
        List<SellHistoryEntry> history = plugin.getAuctionManager().getSellHistory(player.getUniqueId());

        int totalPages = Math.max(1, (int) Math.ceil((double) history.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        String title = "<color:#9CA3AF>Sell History <color:#6B7280>| <color:#9CA3AF>Page " + page + "/" + totalPages;
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.GREEN_STAINED_GLASS_PANE);
        fillCorners(gui, Material.LIME_STAINED_GLASS_PANE);

        if (history.isEmpty()) {
            gui.setItem(31, items.createEmptyHistoryItem("No sell history yet"));
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, history.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], items.createSellHistoryItem(history.get(i)));
            }
        }

        gui.setItem(48, page > 1 ?
                items.createNavItem(Material.ARROW, "<color:#9CA3AF>← Previous", page - 1, "sell_history") :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(50, page < totalPages ?
                items.createNavItem(Material.ARROW, "<color:#9CA3AF>Next →", page + 1, "sell_history") :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE));

        gui.setItem(49, items.createSellHistoryStatsItem(history));
        gui.setItem(45, items.createNavItem(Material.BOOK, "<color:#8B5CF6>Back to History", 1, "history_type"));
        gui.setItem(53, items.createCloseItem());

        openGui(player, gui, AhSession.sellHistory(player.getUniqueId(), page));
    }

    public void openBuyHistoryGui(Player player, int page) {
        sounds.playOpen(player);
        List<BuyHistoryEntry> history = plugin.getAuctionManager().getBuyHistory(player.getUniqueId());

        int totalPages = Math.max(1, (int) Math.ceil((double) history.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        String title = "<color:#9CA3AF>Buy History <color:#6B7280>| <color:#9CA3AF>Page " + page + "/" + totalPages;
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.BLUE_STAINED_GLASS_PANE);
        fillCorners(gui, Material.CYAN_STAINED_GLASS_PANE);

        if (history.isEmpty()) {
            gui.setItem(31, items.createEmptyHistoryItem("No buy history yet"));
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, history.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], items.createBuyHistoryItem(history.get(i)));
            }
        }

        gui.setItem(48, page > 1 ?
                items.createNavItem(Material.ARROW, "<color:#9CA3AF>← Previous", page - 1, "buy_history") :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(50, page < totalPages ?
                items.createNavItem(Material.ARROW, "<color:#9CA3AF>Next →", page + 1, "buy_history") :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE));

        gui.setItem(49, items.createBuyHistoryStatsItem(history));
        gui.setItem(45, items.createNavItem(Material.BOOK, "<color:#8B5CF6>Back to History", 1, "history_type"));
        gui.setItem(53, items.createCloseItem());

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
        String title = "<color:#9CA3AF>Expired Items <color:#6B7280>| <color:#9CA3AF>Click to Claim";
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.RED_STAINED_GLASS_PANE);
        fillCorners(gui, Material.ORANGE_STAINED_GLASS_PANE);

        for (int i = 0; i < expiredItems.size() && i < AUCTION_SLOTS.length; i++) {
            gui.setItem(AUCTION_SLOTS[i], items.createClaimableItem(expiredItems.get(i), i + 1));
        }

        gui.setItem(48, items.createNavItem(Material.ARROW, "<color:#9CA3AF>← Back to Auction House", 1, "main"));
        gui.setItem(50, items.createActionItem(Material.HOPPER, "<color:#10B981>Claim All", "claim_all",
                List.of("<color:#6B7280>Click to claim all items at once")));
        gui.setItem(49, items.createStatsItem(expiredItems.size()));
        gui.setItem(53, items.createCloseItem());

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

        String title = "<color:#9CA3AF>Your Listings <color:#6B7280>| <color:#9CA3AF>Page " + page + "/" + totalPages;
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorder(gui, Material.LIME_STAINED_GLASS_PANE);
        fillCorners(gui, Material.GREEN_STAINED_GLASS_PANE);

        int start = (page - 1) * PER_PAGE;
        int end = Math.min(start + PER_PAGE, auctions.size());
        for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
            gui.setItem(AUCTION_SLOTS[i - start], items.createOwnAuctionItem(auctions.get(i)));
        }

        gui.setItem(48, page > 1 ?
                items.createNavItem(Material.ARROW, "<color:#9CA3AF>← Previous", page - 1, "listings") :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(50, page < totalPages ?
                items.createNavItem(Material.ARROW, "<color:#9CA3AF>Next →", page + 1, "listings") :
                items.createFiller(Material.GRAY_STAINED_GLASS_PANE));

        gui.setItem(49, createListingsInfoItem(auctions.size()));
        gui.setItem(45, items.createNavItem(Material.BARRIER, "<color:#EF4444>Back to Main", 1, "main"));
        gui.setItem(53, items.createCloseItem());

        openGui(player, gui, AhSession.listings(player.getUniqueId(), page));
    }

    private ItemStack createListingsInfoItem(int totalItems) {
        ItemStack item = new ItemStack(Material.PAPER);
        var meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#9CA3AF>Your Listings Info"));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));
        lore.add(net.kyori.adventure.text.Component.empty());
        lore.add(mm.deserialize("<color:#9CA3AF>Total Active: <color:#FFFFFF>" + totalItems));
        lore.add(net.kyori.adventure.text.Component.empty());
        lore.add(mm.deserialize("<color:#6B7280>Right-click any item to cancel"));
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));
        meta.lore(lore);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void openGui(Player player, Inventory gui, AhSession session) {
        player.removeMetadata("ah_session", plugin);
        player.openInventory(gui);
        player.setMetadata("ah_session", new FixedMetadataValue(plugin, session));
    }

    private void fillBorder(Inventory gui, Material mat) {
        ItemStack border = items.createFiller(mat);
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border);
            gui.setItem(45 + i, border);
        }
        for (int i = 1; i < 5; i++) {
            gui.setItem(i * 9, border);
            gui.setItem(i * 9 + 8, border);
        }
    }

    private void fillCorners(Inventory gui, Material mat) {
        ItemStack corner = items.createFiller(mat);
        gui.setItem(0, corner);
        gui.setItem(8, corner);
        gui.setItem(45, corner);
        gui.setItem(53, corner);
    }

    public AhItemFactory getItemFactory() { return items; }
    public AhSoundManager getSoundManager() { return sounds; }
}