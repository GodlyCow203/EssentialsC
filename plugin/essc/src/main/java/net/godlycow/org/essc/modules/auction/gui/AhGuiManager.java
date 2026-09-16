package net.godlycow.org.essc.modules.auction.gui;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.auction.AhSoundManager;
import net.godlycow.org.essc.modules.auction.Auction;
import net.godlycow.org.essc.modules.auction.BuyHistoryEntry;
import net.godlycow.org.essc.modules.auction.SellHistoryEntry;
import net.godlycow.org.essc.plugin.gui.GuiButton;
import net.godlycow.org.essc.plugin.gui.GuiFramework;
import net.godlycow.org.essc.plugin.gui.GuiTemplate;
import net.godlycow.org.essc.util.ComponentHelper;
import net.godlycow.org.essc.util.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class AhGuiManager {
    private static final int[] AUCTION_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int PER_PAGE = 28;

    private final EssentialsC plugin;
    private final GuiFramework guiFramework;
    private final AhSoundManager sounds;
    private final AhItemFactory itemFactory;
    private final MiniMessage mm;

    public AhGuiManager(EssentialsC plugin, GuiFramework guiFramework, AhSoundManager sounds) {
        this.plugin = plugin;
        this.guiFramework = guiFramework;
        this.sounds = sounds;
        this.itemFactory = new AhItemFactory(plugin, guiFramework.getItemBuilder());
        this.mm = plugin.getMiniMessage();
    }

    public void openMainGui(Player player, int page) {
        sounds.playOpen(player);
        GuiTemplate template = guiFramework.getTemplate("auction_main");
        if (template == null) {
            plugin.getLogger().warning("[AH] Missing GUI template: auction_main.yml");
            return;
        }

        List<Auction> auctions = new ArrayList<>(plugin.getAuctionManager().getActiveAuctions());
        auctions.sort(Comparator.comparingLong(Auction::getListedTime).reversed());

        int totalPages = Math.max(1, (int) Math.ceil((double) auctions.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        Component title = template.resolveTitle(player, plugin,
                Map.of("page", String.valueOf(page), "total", String.valueOf(totalPages)));
        Inventory gui = Bukkit.createInventory(new AhGuiHolder(template.getId(), 1), template.getSize(), title);

        guiFramework.fillStaticItems(gui, "auction_main", player);

        GuiButton expiredConfig = template.getItem("expired");
        if (expiredConfig != null) {
            place(gui, expiredConfig, itemFactory.createExpiredButtonItem(player, expiredConfig));
        }

        if (auctions.isEmpty()) {
            GuiButton emptyConfig = template.getItem("empty");
            place(gui, emptyConfig, emptyConfig != null
                    ? guiFramework.getItemBuilder().build(emptyConfig, player)
                    : itemFactory.createFiller(null, player));
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, auctions.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], itemFactory.createAuctionItem(auctions.get(i), player));
            }
        }

        GuiButton navPrev = template.getItem("nav-prev");
        GuiButton navNext = template.getItem("nav-next");

        place(gui, navPrev, page > 1
                ? itemFactory.createNavItem(navPrev, page - 1, "main", player)
                : null);

        place(gui, navNext, page < totalPages
                ? itemFactory.createNavItem(navNext, page + 1, "main", player)
                : null);

        GuiButton infoConfig = template.getItem("info");
        place(gui, infoConfig, itemFactory.createInfoItem(player, infoConfig));

        openGui(player, gui);
    }

    public void openHistoryTypeGui(Player player) {
        sounds.playOpen(player);
        GuiTemplate template = guiFramework.getTemplate("auction_history_type");
        if (template == null) {
            plugin.getLogger().warning("[AH] Missing GUI template: auction_history_type.yml");
            return;
        }

        Component title = template.resolveTitle(player, plugin);
        Inventory gui = Bukkit.createInventory(new AhGuiHolder(template.getId(), 1), template.getSize(), title);
        guiFramework.fillStaticItems(gui, "auction_history_type", player);

        openGui(player, gui);
    }

    public void openSellHistoryGui(Player player, int page) {
        sounds.playOpen(player);
        GuiTemplate template = guiFramework.getTemplate("auction_sell_history");
        if (template == null) {
            plugin.getLogger().warning("[AH] Missing GUI template: auction_sell_history.yml");
            return;
        }

        List<SellHistoryEntry> history = plugin.getAuctionManager().getSellHistory(player.getUniqueId());
        int totalPages = Math.max(1, (int) Math.ceil((double) history.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        Component title = template.resolveTitle(player, plugin,
                Map.of("page", String.valueOf(page), "total", String.valueOf(totalPages)));
        Inventory gui = Bukkit.createInventory(new AhGuiHolder(template.getId(), 1), template.getSize(), title);
        guiFramework.fillStaticItems(gui, "auction_sell_history", player);

        if (history.isEmpty()) {
            GuiButton empty = template.getItem("empty");
            place(gui, empty, empty != null ? guiFramework.getItemBuilder().build(empty, player) : itemFactory.createFiller(null, player));
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, history.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], itemFactory.createSellHistoryItem(history.get(i), player));
            }
        }

        GuiButton navPrev = template.getItem("nav-prev");
        GuiButton navNext = template.getItem("nav-next");

        place(gui, navPrev, page > 1
                ? itemFactory.createNavItem(navPrev, page - 1, "sell_history", player)
                : null);
        place(gui, navNext, page < totalPages
                ? itemFactory.createNavItem(navNext, page + 1, "sell_history", player)
                : null);

        GuiButton statsConfig = template.getItem("stats");
        place(gui, statsConfig, itemFactory.createSellHistoryStatsItem(history, player, statsConfig));

        openGui(player, gui);
    }

    public void openBuyHistoryGui(Player player, int page) {
        sounds.playOpen(player);
        GuiTemplate template = guiFramework.getTemplate("auction_buy_history");
        if (template == null) {
            plugin.getLogger().warning("[AH] Missing GUI template: auction_buy_history.yml");
            return;
        }

        List<BuyHistoryEntry> history = plugin.getAuctionManager().getBuyHistory(player.getUniqueId());
        int totalPages = Math.max(1, (int) Math.ceil((double) history.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        Component title = template.resolveTitle(player, plugin,
                Map.of("page", String.valueOf(page), "total", String.valueOf(totalPages)));
        Inventory gui = Bukkit.createInventory(new AhGuiHolder(template.getId(), 1), template.getSize(), title);
        guiFramework.fillStaticItems(gui, "auction_buy_history", player);

        if (history.isEmpty()) {
            GuiButton empty = template.getItem("empty");
            place(gui, empty, empty != null ? guiFramework.getItemBuilder().build(empty, player) : itemFactory.createFiller(null, player));
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, history.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], itemFactory.createBuyHistoryItem(history.get(i), player));
            }
        }

        GuiButton navPrev = template.getItem("nav-prev");
        GuiButton navNext = template.getItem("nav-next");

        place(gui, navPrev, page > 1
                ? itemFactory.createNavItem(navPrev, page - 1, "buy_history", player)
                : null);
        place(gui, navNext, page < totalPages
                ? itemFactory.createNavItem(navNext, page + 1, "buy_history", player)
                : null);

        GuiButton statsConfig = template.getItem("stats");
        place(gui, statsConfig, itemFactory.createBuyHistoryStatsItem(history, player, statsConfig));

        openGui(player, gui);
    }

    public void openExpiredGui(Player player, int page) {
        List<ItemStack> expiredItems = plugin.getAuctionManager().getExpiredItems(player.getUniqueId());
        if (expiredItems.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.no_expired"));
            sounds.playError(player);
            return;
        }

        sounds.playOpen(player);
        GuiTemplate template = guiFramework.getTemplate("auction_expired");
        if (template == null) {
            plugin.getLogger().warning("[AH] Missing GUI template: auction_expired.yml");
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) expiredItems.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        Component title = template.resolveTitle(player, plugin);
        Inventory gui = Bukkit.createInventory(new AhGuiHolder(template.getId(), page), template.getSize(), title);
        guiFramework.fillStaticItems(gui, "auction_expired", player);

        int start = (page - 1) * PER_PAGE;
        int end = Math.min(start + PER_PAGE, expiredItems.size());
        for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
            gui.setItem(AUCTION_SLOTS[i - start], itemFactory.createClaimableItem(expiredItems.get(i), i + 1, player));
        }

        GuiButton statsConfig = template.getItem("stats");
        place(gui, statsConfig, itemFactory.createStatsItem(expiredItems.size(), player, statsConfig));

        GuiButton navPrev = template.getItem("nav-prev");
        GuiButton navNext = template.getItem("nav-next");

        place(gui, navPrev, page > 1
                ? itemFactory.createNavItem(navPrev, page - 1, "expired", player)
                : null);
        place(gui, navNext, page < totalPages
                ? itemFactory.createNavItem(navNext, page + 1, "expired", player)
                : null);

        openGui(player, gui);
    }

    public void openConfirmBuyGui(Player player, Auction auction, String searchQuery) {

        sounds.playOpen(player);
        GuiTemplate template = guiFramework.getTemplate("ah_confirm");
        if (template == null) {
            plugin.getLogger().warning("[AH] Missing GUI template: ah_confirm.yml");
            return;
        }


        String priceStr = itemFactory.formatAmount(auction.getPrice());

        Component title = template.resolveTitle(player, plugin);
        Inventory gui = Bukkit.createInventory(new AhGuiHolder(template.getId(),  1, auction.getId(), searchQuery), template.getSize(), title);

        guiFramework.fillStaticItems(gui, "ah_confirm", player);

        GuiButton itemConfig = template.getItem("item");
        int itemSlot = (itemConfig != null && !itemConfig.getSlots().isEmpty())
                ? itemConfig.getSlots().get(0) : 13;

        ItemStack display = auction.getItem().clone();
        ItemMeta displayMeta = display.getItemMeta();

        if (displayMeta != null) {
            List<Component> lore = new ArrayList<>();
            lore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.confirm.item.lore.price",
                    Map.of("price", priceStr))));
            lore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.confirm.item.lore.seller",
                    Map.of("seller", auction.getSellerName()))));
            displayMeta.lore(lore);
            display.setItemMeta(displayMeta);
        }

        gui.setItem(itemSlot, display);

        GuiButton confirmConfig = template.getItem("confirm");
        if (confirmConfig != null) {
            ItemStack confirmItem  = guiFramework.getItemBuilder().build(confirmConfig, player);
            ItemMeta confirmMeta = confirmItem.getItemMeta();

            if (confirmMeta != null) {
                List<Component> confirmLore = new ArrayList<>();
                confirmLore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.confirm.confirm.lore1")));
                confirmLore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.confirm.confirm.lore2",
                        Map.of("price", priceStr))));


                confirmMeta.lore(confirmLore);
                confirmMeta.getPersistentDataContainer().set(
                        new NamespacedKey(plugin, "gui_action"),
                        PersistentDataType.STRING,
                        "ah_confirm_buy"
                );

                confirmItem.setItemMeta(confirmMeta);
            }


            gui.setItem(confirmConfig.getSlots().get(0), confirmItem);

        }

        GuiButton cancelConfig = template.getItem("cancel");

        if (cancelConfig != null) {
            ItemStack cancelItem = guiFramework.getItemBuilder().build(cancelConfig, player);
            ItemMeta cancelMeta = cancelItem.getItemMeta();
            if (cancelMeta != null) {

                List<Component> cancelLore = new ArrayList<>();


                cancelLore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.confirm.cancel.lore1")));
                cancelLore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.confirm.cancel.lore2")));
                cancelMeta.lore(cancelLore);

                cancelMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, "ah_confirm_cancel");
                cancelItem.setItemMeta(cancelMeta);
            }
            gui.setItem(cancelConfig.getSlots().get(0), cancelItem);
        }
        

        openGui(player, gui);
    }

    public void openSearchGui(Player player, String query, int page) {

        sounds.playOpen(player);

        GuiTemplate template = guiFramework.getTemplate("auction_search");
        if (template == null) {
            plugin.getLogger().warning("[AH] Mising GUI template: auction_search.yml");
            return;
        }

        ItemUtil itemUtil = ItemUtil.getInstance();
        List<Auction> results = new ArrayList<>();
        for (Auction auction : plugin.getAuctionManager().getActiveAuctions()) {
            if (itemUtil.matchesAny(query, auction.getItem().getType())) {
                results.add(auction);

                continue;
            }

            String displayName = "";


            if (auction.getItem().getItemMeta() != null && auction.getItem().getItemMeta().hasDisplayName()) {
                displayName = LegacyComponentSerializer.legacySection().serialize(
                        auction.getItem().getItemMeta().displayName()).toLowerCase();
            }

            if (displayName.contains(query.toLowerCase())) {
                results.add(auction);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) results.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("query", query);
        placeholders.put("page", String.valueOf(page));
        placeholders.put("total", String.valueOf(totalPages));
        placeholders.put("count", String.valueOf(results.size()));

        Component title = template.resolveTitle(player, plugin, placeholders);
        Inventory gui = Bukkit.createInventory(new AhGuiHolder(template.getId(), page, query), template.getSize(), title);

        guiFramework.fillStaticItems(gui, "auction_search", player);

        if (results.isEmpty()) {
            GuiButton emptyConfig = template.getItem("empty");
            if (emptyConfig != null) {
                ItemStack emptyItem = guiFramework.getItemBuilder().build(emptyConfig, player);
                ItemMeta emptyMeta = emptyItem.getItemMeta();
                if (emptyMeta != null) {

                    List<Component> emptyLore = new ArrayList<>();
                    emptyLore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.item.empty.search.lore1")));
                    emptyLore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.item.empty.search.lore2",
                            Map.of("query", query))));
                    emptyMeta.lore(emptyLore);
                    emptyItem.setItemMeta(emptyMeta);
                }
                place(gui, emptyConfig, emptyItem);
            }
        } else {
            int start = (page - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, results.size());
            for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
                gui.setItem(AUCTION_SLOTS[i - start], itemFactory.createAuctionItem(results.get(i), player));
            }
        }

        GuiButton navPrev = template.getItem("nav-prev");
        GuiButton navNext = template.getItem("nav-next");

        place( gui, navPrev, page > 1
                ? itemFactory.createNavItem(navPrev, page - 1, "search_" + query, player) : null);
        place(gui, navNext, page < totalPages
                ? itemFactory.createNavItem(navNext, page + 1, "search_" + query, player)
                : null);

        GuiButton infoConfig = template.getItem("info");

        if (infoConfig != null) {
            ItemStack infoItem = guiFramework.getItemBuilder().build(infoConfig, player);
            ItemMeta infoMeta = infoItem.getItemMeta();
            if (infoMeta != null) {

                List<Component> infoLore = new ArrayList<>();
                infoLore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.item.search_info.lore1")));
                infoLore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.item.search_info.lore2",
                        Map.of("query", query))));
                infoLore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player, "ah.gui.item.search_info.lore3",
                        Map.of("count", String.valueOf(results.size())))));
                infoMeta.lore(infoLore);
                infoItem.setItemMeta(infoMeta);
            }


            place(gui, infoConfig, infoItem);
        }


        openGui(player, gui);
    }

    public void openListingsGui(Player player, int page) {
        List<Auction> auctions = new ArrayList<>(plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId()));
        if (auctions.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.no_listings"));
            sounds.playError(player);
            return;
        }

        sounds.playOpen(player);
        GuiTemplate template = guiFramework.getTemplate("auction_listings");
        if (template == null) {
            plugin.getLogger().warning("[AH] Missing GUI template: auction_listings.yml");
            return;
        }

        auctions.sort(Comparator.comparingLong(Auction::getTimeRemaining));

        int totalPages = Math.max(1, (int) Math.ceil((double) auctions.size() / PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        Component title = template.resolveTitle(player, plugin,
                Map.of("page", String.valueOf(page), "total", String.valueOf(totalPages)));
        Inventory gui = Bukkit.createInventory(new AhGuiHolder(template.getId(), 1), template.getSize(), title);
        guiFramework.fillStaticItems(gui, "auction_listings", player);

        int start = (page - 1) * PER_PAGE;
        int end = Math.min(start + PER_PAGE, auctions.size());
        for (int i = start; i < end && (i - start) < AUCTION_SLOTS.length; i++) {
            gui.setItem(AUCTION_SLOTS[i - start], itemFactory.createOwnAuctionItem(auctions.get(i), player));
        }

        GuiButton navPrev = template.getItem("nav-prev");
        GuiButton navNext = template.getItem("nav-next");

        place(gui, navPrev, page > 1
                ? itemFactory.createNavItem(navPrev, page - 1, "listings", player)
                : null);
        place(gui, navNext, page < totalPages
                ? itemFactory.createNavItem(navNext, page + 1, "listings", player)
                : null);

        GuiButton infoConfig = template.getItem("listings-info");
        place(gui, infoConfig, itemFactory.createListingsInfoItem(auctions.size(), player, infoConfig));

        openGui(player, gui);
    }

    private void place(Inventory gui, GuiButton button, ItemStack item) {
        if (button == null || item == null || button.getSlots().isEmpty()) return;
        int slot = button.getSlots().get(0);
        if (slot >= 0 && slot < gui.getSize()) {
            gui.setItem(slot, item);
        }
    }

    private void openGui(Player player, Inventory gui) {
        player.openInventory(gui);
    }

    public AhItemFactory getItemFactory() {
        return itemFactory;
    }

    public AhSoundManager getSoundManager() {
        return sounds;
    }

    public void reload() {
        guiFramework.reload();
    }
}
