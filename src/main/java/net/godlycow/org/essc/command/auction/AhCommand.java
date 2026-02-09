package net.godlycow.org.essc.command.auction;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.auction.AhSession;
import net.godlycow.org.essc.auction.Auction;
import net.godlycow.org.essc.auction.AuctionManager;
import net.godlycow.org.essc.command.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AhCommand extends Command {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private static final int[] AUCTION_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int ITEMS_PER_PAGE = 28;

    public AhCommand(EssentialsC plugin) {
        super(plugin, "ah", AuctionManager.PERM_USE, true, 0, "command.usage.ah");
        this.aliases = new String[]{"auction", "auctionhouse"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!plugin.getConfigManager().isAHEnabled()) {
            sender.sendMessage(lang.get(sender, "ah.disabled"));
            return true;
        }

        if (plugin.getAuctionManager() == null) {
            sender.sendMessage(lang.get(sender, "ah.not_loaded"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            openAuctionHouse(player, 1, false);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "sell", "s" -> handleSell(player, args);
            case "cancel", "c" -> handleCancel(player, args);
            case "expired", "e" -> openExpiredGui(player);
            case "listings", "l" -> handleListings(player);
            case "reload", "rl" -> handleReload(player);
            case "help", "?" -> sendUsage(player);
            default -> {
                try {
                    int page = Integer.parseInt(sub);
                    openAuctionHouse(player, page, false);
                } catch (NumberFormatException e) {
                    player.sendMessage(lang.get(player, "ah.invalid_subcommand"));
                }
            }
        }

        return true;
    }

    private void openAuctionHouse(Player player, int page, boolean isExpiredView) {
        List<Auction> auctions = plugin.getAuctionManager().getActiveAuctions();

        auctions.sort(Comparator.comparingLong(Auction::getListedTime).reversed());

        if (auctions.isEmpty()) {
            int totalPages = 1;
            page = 1;

            String title = "<color:#FFB300><b>Auction House<reset> <color:#666666>|<reset> <color:#AAAAAA>Page " + page + "/" + totalPages;
            Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

            fillBorders(gui, Material.BLACK_STAINED_GLASS_PANE);
            setCornerDecorations(gui, false);

            gui.setItem(49, createInfoCenterItem(player));
            gui.setItem(45, createUtilityItem(Material.CHEST, "<color:#FFB300>Your Listings", "listings", 0));
            gui.setItem(46, createUtilityItem(Material.BARRIER, "<color:#FFB300>Expired Items", "expired", 0));
            gui.setItem(52, createUtilityItem(Material.EMERALD, "<color:#FFB300>Sell Item", "sell", 0));
            gui.setItem(53, createCloseItem());

            ItemStack empty = createGlassPane(Material.BARRIER, "<color:#FF4444>No Active Auctions");
            ItemMeta meta = empty.getItemMeta();
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<color:#AAAAAA>Be the first to sell something!"));
            lore.add(mm.deserialize("<color:#AAAAAA>Use <color:#66AAFF>/ah sell <price>"));
            meta.lore(lore);
            empty.setItemMeta(meta);
            gui.setItem(31, empty);

            player.openInventory(gui);
            player.setMetadata("ah_session", new FixedMetadataValue(plugin, new AhSession(page, false)));
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) auctions.size() / ITEMS_PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, auctions.size());
        List<Auction> pageAuctions = auctions.subList(start, end);

        String title = "<color:#FFB300><b>Auction House<reset> <color:#666666>|<reset> <color:#AAAAAA>Page " + page + "/" + totalPages;

        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorders(gui, Material.BLACK_STAINED_GLASS_PANE);
        setCornerDecorations(gui, false);

        for (int i = 0; i < pageAuctions.size() && i < AUCTION_SLOTS.length; i++) {
            Auction auction = pageAuctions.get(i);
            gui.setItem(AUCTION_SLOTS[i], createAuctionItem(auction, player));
        }

        if (page > 1) {
            gui.setItem(48, createNavigationItem(Material.ARROW, "<color:#FFB300>← Previous Page", page - 1, false));
        }

        if (page < totalPages) {
            gui.setItem(50, createNavigationItem(Material.ARROW, "<color:#FFB300>Next Page →", page + 1, false));
        }

        gui.setItem(49, createInfoCenterItem(player));
        gui.setItem(45, createUtilityItem(Material.CHEST, "<color:#FFB300>Your Listings", "listings", 0));
        gui.setItem(46, createUtilityItem(Material.BARRIER, "<color:#FFB300>Expired Items", "expired", 0));
        gui.setItem(52, createUtilityItem(Material.EMERALD, "<color:#FFB300>Sell Item", "sell", 0));
        gui.setItem(53, createCloseItem());

        player.openInventory(gui);
        player.setMetadata("ah_session", new FixedMetadataValue(plugin, new AhSession(page, false)));
    }

    private void openExpiredGui(Player player) {
        List<org.bukkit.inventory.ItemStack> expiredItems = plugin.getAuctionManager().getPlayerExpiredItems(player.getUniqueId());

        if (expiredItems.isEmpty()) {
            player.sendMessage(lang.get(player, "ah.no_expired"));
            return;
        }

        String title = "<color:#FFB300><b>Expired Items<reset> <color:#666666>|<reset> <color:#AAAAAA>Click to Claim";
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(title));

        fillBorders(gui, Material.RED_STAINED_GLASS_PANE);
        setCornerDecorations(gui, true);

        int slot = 0;
        for (org.bukkit.inventory.ItemStack item : expiredItems) {
            if (slot >= AUCTION_SLOTS.length) break;

            org.bukkit.inventory.ItemStack displayItem = item.clone();
            ItemMeta meta = displayItem.getItemMeta();

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.remove(new NamespacedKey(plugin, "ah_auction_id"));
            container.remove(new NamespacedKey(plugin, "ah_own_auction"));
            container.remove(new NamespacedKey(plugin, "ah_nav_page"));
            container.remove(new NamespacedKey(plugin, "ah_nav_expired"));
            container.remove(new NamespacedKey(plugin, "ah_action"));
            container.remove(new NamespacedKey(plugin, "ah_close"));

            List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            lore.add(mm.deserialize("<color:#FFB300>Click to claim this item!"));
            meta.lore(lore);

            container.set(
                    new NamespacedKey(plugin, "ah_claimable"),
                    PersistentDataType.BYTE, (byte) 1
            );

            displayItem.setItemMeta(meta);
            gui.setItem(AUCTION_SLOTS[slot], displayItem);
            slot++;
        }

        gui.setItem(49, createNavigationItem(Material.BARRIER, "<color:#FFB300>Back to Auction House", 1, false));
        gui.setItem(53, createCloseItem());

        player.openInventory(gui);
    }

    private void fillBorders(Inventory gui, Material material) {
        ItemStack border = createGlassPane(material, " ");

        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border);
            gui.setItem(45 + i, border);
        }

        for (int i = 1; i < 5; i++) {
            gui.setItem(i * 9, border);
            gui.setItem(i * 9 + 8, border);
        }
    }

    private void setCornerDecorations(Inventory gui, boolean isExpired) {
        Material mat = isExpired ? Material.REDSTONE_BLOCK : Material.GOLD_BLOCK;
        String color = isExpired ? "<color:#FF4444>" : "<color:#FFD700>";

        ItemStack corner = createDecorationItem(mat, color + (""));
        gui.setItem(0, corner);
        gui.setItem(8, corner);
    }

    private ItemStack createGlassPane(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDecorationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createAuctionItem(Auction auction, Player viewer) {
        ItemStack item = auction.getItem().clone();
        ItemMeta meta = item.getItemMeta();

        List<Component> lore = new ArrayList<>();

        lore.add(mm.deserialize("<color:#666666>━━━━━━━━━━━━━━━━━━</color>"));
        lore.add(mm.deserialize(" "));
        lore.add(mm.deserialize("<color:#FFB300>Price: <color:#AAAAAA>" +
                plugin.getEconomyManager().format(auction.getPrice()) + ""));
        lore.add(mm.deserialize("<color:#FFB300>Seller: <color:#AAAAAA>" + auction.getSellerName()));

        long hours = TimeUnit.MILLISECONDS.toHours(auction.getTimeRemaining());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(auction.getTimeRemaining()) % 60;
        String timeStr = hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
        lore.add(mm.deserialize("<color:#FFB300>Time: <color:#AAAAAA>" + timeStr));

        lore.add(mm.deserialize(" "));

        boolean isOwn = auction.getSellerUuid().equals(viewer.getUniqueId());

        if (isOwn) {
            lore.add(mm.deserialize("<color:#AAAAAA>This is your auction"));
            if (viewer.hasPermission(AuctionManager.PERM_CANCEL)) {
                lore.add(mm.deserialize("<color:#AAAAAA>Right-click to cancel"));
            }
        } else {
            if (viewer.hasPermission(AuctionManager.PERM_BUY)) {
                lore.add(mm.deserialize("<color:#AAAAAA>➜ Click to purchase!"));
            } else {
                lore.add(mm.deserialize("<color:#AAAAAA>You cannot buy items"));
            }
        }

        lore.add(mm.deserialize("<color:#666666>━━━━━━━━━━━━━━━━━━</color>"));
        lore.add(mm.deserialize("<color:#AAAAAA>Auction ID: #" + auction.getId()));

        meta.lore(lore);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "ah_auction_id"),
                PersistentDataType.INTEGER,
                auction.getId()
        );

        if (isOwn) {
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "ah_own_auction"),
                    PersistentDataType.BYTE,
                    (byte) 1
            );
        }

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavigationItem(Material material, String name, int targetPage, boolean isExpired) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(plugin, "ah_nav_page"), PersistentDataType.INTEGER, targetPage);
        container.set(new NamespacedKey(plugin, "ah_nav_expired"), PersistentDataType.BYTE, (byte) (isExpired ? 1 : 0));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createUtilityItem(Material material, String name, String action, int data) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize(" "));
        lore.add(mm.deserialize("<color:#AAAAAA>Click to execute"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "ah_action"),
                PersistentDataType.STRING,
                action
        );

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoCenterItem(Player player) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#FFB300><b>Auction House"));

        int activeAuctions = plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId()).size();
        int maxAuctions = plugin.getConfigManager().getAHMaxAuctions();
        boolean bypass = player.hasPermission(AuctionManager.PERM_BYPASS_LIMIT);

        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<color:#666666>━━━━━━━━━━━━━━━━━━</color>"));
        lore.add(mm.deserialize(" "));

        if (bypass) {
            lore.add(mm.deserialize("<color:#FFB300>Your Active Listings: <color:#AAAAAA>" + activeAuctions + "/∞"));
        } else {
            lore.add(mm.deserialize("<color:#FFB300>Your Active Listings: <color:#AAAAAA>" + activeAuctions + "/" + maxAuctions));
        }

        lore.add(mm.deserialize(" "));
        lore.add(mm.deserialize("<color:#FFB300>/ah sell <price> <color:#FFFFFF>- <color:#AAAAAA>Sell held item"));
        lore.add(mm.deserialize("<color:#FFB300>/ah cancel <id> <color:#FFFFFF>- <color:#AAAAAA>Cancel auction"));
        lore.add(mm.deserialize("<color:#FFB300>/ah expired <color:#FFFFFF>- <color:#AAAAAA>Claim returned items"));
        lore.add(mm.deserialize("<color:#FFB300>/ah listings <color:#FFFFFF>- <color:#AAAAAA>View your auctions"));
        lore.add(mm.deserialize(" "));
        lore.add(mm.deserialize("<color:#666666>━━━━━━━━━━━━━━━━━━</color>"));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#FF4444>✕ Close"));

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "ah_close"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    private void handleSell(Player player, String[] args) {
        if (!player.hasPermission(AuctionManager.PERM_SELL)) {
            player.sendMessage(lang.get(player, "error.no_permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(lang.get(player, "command.usage.ah_sell"));
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(args[1]);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                player.sendMessage(lang.get(player, "ah.invalid_price"));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(lang.get(player, "ah.invalid_price"));
            return;
        }

        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(lang.get(player, "ah.no_item"));
            return;
        }

        BigDecimal minPrice = plugin.getConfigManager().getAHMinPrice();
        if (!player.hasPermission(AuctionManager.PERM_BYPASS_PRICE_MIN) && price.compareTo(minPrice) < 0) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("min", plugin.getEconomyManager().format(minPrice));
            player.sendMessage(lang.get(player, "ah.price_too_low", placeholders));
            return;
        }

        BigDecimal maxPrice = plugin.getConfigManager().getAHMaxPrice();
        if (!player.hasPermission(AuctionManager.PERM_BYPASS_PRICE_MAX) && maxPrice != null && price.compareTo(maxPrice) > 0) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max", plugin.getEconomyManager().format(maxPrice));
            player.sendMessage(lang.get(player, "ah.price_too_high", placeholders));
            return;
        }

        long duration = plugin.getConfigManager().getAHDuration();

        plugin.getAuctionManager().createAuction(player, item, price, duration).thenAccept(success -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.getInventory().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("price", plugin.getEconomyManager().format(price));
                    placeholders.put("duration", String.valueOf(duration / (1000 * 60 * 60)));
                    player.sendMessage(lang.get(player, "ah.listed", placeholders));
                } else {
                    player.sendMessage(lang.get(player, "ah.max_auctions_reached"));
                }
            });
        });
    }

    private void handleCancel(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(lang.get(player, "command.usage.ah_cancel"));
            return;
        }

        int auctionId;
        try {
            auctionId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(lang.get(player, "ah.invalid_id"));
            return;
        }

        plugin.getAuctionManager().cancelAuction(player, auctionId).thenAccept(success -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.sendMessage(lang.get(player, "ah.cancelled"));
                } else {
                    player.sendMessage(lang.get(player, "ah.not_your_auction"));
                }
            });
        });
    }

    private void handleReload(Player player) {
        if (!player.hasPermission(AuctionManager.PERM_RELOAD)) {
            player.sendMessage(lang.get(player, "error.no_permission"));
            return;
        }

        plugin.getAuctionManager().reload();
        player.sendMessage(lang.get(player, "ah.reloaded"));
        plugin.debug("Auction House reloaded by " + player.getName());
    }

    private void handleListings(Player player) {
        List<Auction> auctions = plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId());

        if (auctions.isEmpty()) {
            player.sendMessage(lang.get(player, "ah.no_listings"));
            return;
        }

        player.sendMessage(lang.get(player, "ah.your_listings_header"));

        for (Auction auction : auctions) {
            long hoursRemaining = auction.getTimeRemaining() / (1000 * 60 * 60);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("id", String.valueOf(auction.getId()));
            placeholders.put("item", auction.getItem().getType().toString());
            placeholders.put("price", plugin.getEconomyManager().format(auction.getPrice()));
            placeholders.put("time", hoursRemaining + "h");
            player.sendMessage(lang.get(player, "ah.listing_entry", placeholders));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(AuctionManager.PERM_USE)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("sell", "expired", "listings", "help"));

            if (sender.hasPermission(AuctionManager.PERM_CANCEL)) {
                subs.add("cancel");
            }
            if (sender.hasPermission(AuctionManager.PERM_RELOAD)) {
                subs.add("reload");
            }

            return subs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("cancel") && sender instanceof Player player
                && sender.hasPermission(AuctionManager.PERM_CANCEL)) {
            return plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId()).stream()
                    .map(a -> String.valueOf(a.getId()))
                    .filter(id -> id.startsWith(args[1]))
                    .toList();
        }

        return Collections.emptyList();
    }
}