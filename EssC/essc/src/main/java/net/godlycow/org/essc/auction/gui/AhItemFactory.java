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

        List<Component> lore = createBaseLore();
        lore.add(mm.deserialize("<color:#9CA3AF>Price: <color:#10B981>" +
                plugin.getEconomyManager().format(auction.getPrice())));
        lore.add(mm.deserialize("<color:#9CA3AF>Seller: <color:#D1D5DB>" + auction.getSellerName()));
        lore.add(mm.deserialize("<color:#9CA3AF>Time Left: " + formatTime(auction.getTimeRemaining())));

        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));

        boolean isOwn = auction.getSellerUuid().equals(viewer.getUniqueId());
        if (isOwn) {
            lore.add(mm.deserialize("<color:#F59E0B>✦ Your auction"));
            lore.add(mm.deserialize("<color:#6B7280>Right-click to cancel"));
        } else {
            lore.add(mm.deserialize("<color:#10B981>✦ Click to purchase"));
        }
        lore.add(mm.deserialize("<color:#6B7280>ID: #" + auction.getId()));

        meta.lore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(auctionKey, PersistentDataType.INTEGER, auction.getId());
        if (isOwn) container.set(ownKey, PersistentDataType.BYTE, (byte) 1);

        display.setItemMeta(meta);
        return display;
    }

    public ItemStack createOwnAuctionItem(Auction auction) {
        ItemStack display = auction.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        List<Component> lore = createBaseLore();
        lore.add(mm.deserialize("<color:#9CA3AF>Price: <color:#10B981>" +
                plugin.getEconomyManager().format(auction.getPrice())));
        lore.add(mm.deserialize("<color:#9CA3AF>Time Left: " + formatTime(auction.getTimeRemaining())));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));
        lore.add(mm.deserialize("<color:#EF4444>✦ Right-click to cancel"));
        lore.add(mm.deserialize("<color:#6B7280>ID: #" + auction.getId()));

        meta.lore(lore);
        meta.getPersistentDataContainer().set(auctionKey, PersistentDataType.INTEGER, auction.getId());
        meta.getPersistentDataContainer().set(ownKey, PersistentDataType.BYTE, (byte) 1);
        display.setItemMeta(meta);
        return display;
    }

    public ItemStack createSellHistoryItem(SellHistoryEntry entry) {
        ItemStack display = entry.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        List<Component> lore = createBaseLore();
        lore.add(mm.deserialize("<color:#9CA3AF>Sold for: <color:#10B981>" +
                plugin.getEconomyManager().format(entry.getPrice())));
        lore.add(mm.deserialize("<color:#9CA3AF>Buyer: <color:#D1D5DB>" + entry.getBuyerName()));
        lore.add(mm.deserialize("<color:#9CA3AF>When: <color:#6B7280>" + formatTimeAgo(entry.getTimestamp())));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    public ItemStack createBuyHistoryItem(BuyHistoryEntry entry) {
        ItemStack display = entry.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        List<Component> lore = createBaseLore();
        lore.add(mm.deserialize("<color:#9CA3AF>Bought for: <color:#EF4444>" +
                plugin.getEconomyManager().format(entry.getPrice())));
        lore.add(mm.deserialize("<color:#9CA3AF>Seller: <color:#D1D5DB>" + entry.getSellerName()));
        lore.add(mm.deserialize("<color:#9CA3AF>When: <color:#6B7280>" + formatTimeAgo(entry.getTimestamp())));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    public ItemStack createClaimableItem(ItemStack original, int slot) {
        ItemStack item = original.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));
        lore.add(mm.deserialize("<color:#10B981>✦ Click to claim this item"));
        lore.add(mm.deserialize("<color:#6B7280>Slot #" + slot));

        meta.lore(lore);
        meta.getPersistentDataContainer().set(claimKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createNavItem(Material mat, String name, int targetPage, String type) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(pageKey, PersistentDataType.INTEGER, targetPage);
        container.set(navKey, PersistentDataType.STRING, type);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createActionItem(Material mat, String name, String action, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) lore.add(mm.deserialize(line));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createInfoItem(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        meta.displayName(mm.deserialize("<color:#9CA3AF>Your Auction Stats"));

        int active = plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId()).size();
        int max = plugin.getConfigManager().getAHMaxAuctions();
        boolean bypass = player.hasPermission("essentialsc.ah.bypass.limit");
        boolean hasExpired = plugin.getAuctionManager().hasExpiredItems(player.getUniqueId());

        List<Component> lore = createBaseLore();
        String limit = bypass ? "<color:#10B981>" + active + "/∞" :
                (active >= max ? "<color:#EF4444>" : "<color:#10B981>") + active + "/" + max;
        lore.add(mm.deserialize("<color:#9CA3AF>Active Listings: " + limit));
        if (hasExpired) lore.add(mm.deserialize("<color:#F59E0B>⚠ Expired items waiting!"));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#6B7280>/ah sell <price> <color:#4B5563>- Sell item"));
        lore.add(mm.deserialize("<color:#6B7280>/ah cancel <id> <color:#4B5563>- Cancel auction"));
        lore.add(mm.deserialize("<color:#6B7280>/ah expired <color:#4B5563>- Claim items"));
        lore.add(mm.deserialize("<color:#6B7280>/ah listings <color:#4B5563>- View your auctions"));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createStatsItem(int count) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#9CA3AF>Expired Items Info"));
        List<Component> lore = createBaseLore();
        lore.add(mm.deserialize("<color:#9CA3AF>Items waiting: <color:#FFFFFF>" + count));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#6B7280>Click items individually"));
        lore.add(mm.deserialize("<color:#6B7280>or use 'Claim All'"));
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createSellHistoryStatsItem(List<SellHistoryEntry> history) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#9CA3AF>Sell Statistics"));

        BigDecimal totalEarnings = history.stream()
                .map(SellHistoryEntry::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Component> lore = createBaseLore();
        lore.add(mm.deserialize("<color:#9CA3AF>Total Sales: <color:#FFFFFF>" + history.size()));
        lore.add(mm.deserialize("<color:#9CA3AF>Total Earnings: <color:#10B981>" +
                plugin.getEconomyManager().format(totalEarnings)));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createBuyHistoryStatsItem(List<BuyHistoryEntry> history) {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#9CA3AF>Buy Statistics"));

        BigDecimal totalSpent = history.stream()
                .map(BuyHistoryEntry::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Component> lore = createBaseLore();
        lore.add(mm.deserialize("<color:#9CA3AF>Total Purchases: <color:#FFFFFF>" + history.size()));
        lore.add(mm.deserialize("<color:#9CA3AF>Total Spent: <color:#EF4444>" +
                plugin.getEconomyManager().format(totalSpent)));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#EF4444>✕ Close"));
        meta.getPersistentDataContainer().set(closeKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createEmptyItem(String message) {
        ItemStack item = new ItemStack(Material.CANDLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#9CA3AF>" + message));
        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<color:#6B7280>No active auctions found"));
        lore.add(mm.deserialize("<color:#6B7280>Be the first to sell!"));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<color:#6B7280>Use <color:#9CA3AF>/ah sell <price>"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createEmptyHistoryItem(String message) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#9CA3AF>" + message));
        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<color:#6B7280>No transactions found"));
        lore.add(mm.deserialize("<color:#6B7280>Start trading!"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createFiller(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(" "));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private List<Component> createBaseLore() {
        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<color:#4B5563>━━━━━━━━━━━━━━━━━━━━━</color>"));
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

    public NamespacedKey getAuctionKey() { return auctionKey; }
    public NamespacedKey getOwnKey() { return ownKey; }
    public NamespacedKey getPageKey() { return pageKey; }
    public NamespacedKey getActionKey() { return actionKey; }
    public NamespacedKey getCloseKey() { return closeKey; }
    public NamespacedKey getClaimKey() { return claimKey; }
    public NamespacedKey getNavKey() { return navKey; }
}