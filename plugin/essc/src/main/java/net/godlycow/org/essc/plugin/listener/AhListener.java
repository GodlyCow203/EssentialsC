package net.godlycow.org.essc.plugin.listener;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.auction.AhCommand;
import net.godlycow.org.essc.modules.auction.AhSoundManager;
import net.godlycow.org.essc.modules.auction.Auction;
import net.godlycow.org.essc.modules.auction.gui.AhGuiHolder;
import net.godlycow.org.essc.modules.auction.gui.AhItemFactory;
import net.godlycow.org.essc.util.InventoryViewCompat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Optional;

public class AhListener implements Listener {
    private final EssentialsC plugin;
    private final AhCommand ahCommand;
    private final AhSoundManager soundManager;
    private final AhItemFactory itemFactory;

    public AhListener(EssentialsC plugin, AhCommand ahCommand) {
        this.plugin = plugin;
        this.ahCommand = ahCommand;
        this.soundManager = ahCommand.getSoundManager();
        this.itemFactory = ahCommand.getItemFactory();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().isAHEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (!(InventoryViewCompat.safeHolder(event.getInventory()) instanceof AhGuiHolder)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() != event.getInventory()) {
            if (event.isShiftClick()) event.setCancelled(true);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(new NamespacedKey(plugin, "gui_close"), PersistentDataType.BYTE)) {
            soundManager.playClose(player);
            player.closeInventory();
            return;
        }

        if (container.has(itemFactory.getPageKey(), PersistentDataType.INTEGER)) {
            int page = container.get(itemFactory.getPageKey(), PersistentDataType.INTEGER);
            String navType = container.getOrDefault(itemFactory.getNavKey(), PersistentDataType.STRING, "main");

            soundManager.playPageTurn(player);

            if (navType.startsWith("search_")) {
                String searchQuery = navType.substring(7);
                ahCommand.openSearchGui(player, searchQuery, page);
            } else {
                switch (navType) {
                    case "main" -> ahCommand.openMainGui(player, page);
                    case "listings" -> ahCommand.openListingsGui(player, page);
                    case "sell_history" -> ahCommand.openSellHistoryGui(player, page);
                    case "buy_history" -> ahCommand.openBuyHistoryGui(player, page);
                    case "expired" -> ahCommand.openExpiredGui(player, page);
                }
            }
            return;
        }

        if (container.has(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING)) {
            String action = container.get(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING);
            if ("ah_confirm_buy".equals(action)) {
                if (InventoryViewCompat.safeHolder(event.getInventory()) instanceof AhGuiHolder ahHolder) {
                    handleConfirmBuy(player, ahHolder.getAuctionId());
                }
                return;
            }
            handleAction(player, action);
            return;
        }

        Material type = clicked.getType();
        if (type.name().endsWith("_STAINED_GLASS_PANE") || type.name().endsWith("_GLASS_PANE")) {
            soundManager.playError(player);
            return;
        }

        if (container.has(itemFactory.getClaimKey(), PersistentDataType.BYTE)) {
            handleClaim(player);
            return;
        }

        if (container.has(itemFactory.getAuctionKey(), PersistentDataType.INTEGER)) {
            int id = container.get(itemFactory.getAuctionKey(), PersistentDataType.INTEGER);
            boolean isOwn = container.has(itemFactory.getOwnKey(), PersistentDataType.BYTE);
            handleAuctionClick(player, id, isOwn, event.getClick());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (InventoryViewCompat.safeHolder(event.getInventory()) instanceof AhGuiHolder) {
            event.setCancelled(true);
        }
    }

    private void handleAction(Player player, String action) {
        switch (action) {
            case "sell" -> {
                if (!player.hasPermission("essentialsc.ah.sell")) {
                    player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
                    soundManager.playError(player);
                    return;
                }
                soundManager.playClick(player);
                player.closeInventory();
                player.sendMessage(plugin.getLanguageManager().get(player, "ah.sell_prompt"));
            }
            case "expired" -> {
                soundManager.playClick(player);
                ahCommand.openExpiredGui(player, 1);
            }
            case "listings" -> {
                soundManager.playClick(player);
                ahCommand.openListingsGui(player, 1);
            }
            case "claim_all" -> handleClaimAll(player);
            case "refresh", "back_main" -> {
                soundManager.playClick(player);
                String searchQuery = null;
                if (player.getOpenInventory().getTopInventory().getHolder() instanceof AhGuiHolder holder) {
                    searchQuery = holder.getSearchQuery();
                }
                if (searchQuery != null) {
                    int currentPage = 1;
                    if (player.getOpenInventory().getTopInventory().getHolder() instanceof AhGuiHolder h) {
                        currentPage = h.getPage();
                    }
                    ahCommand.openSearchGui(player, searchQuery, currentPage);
                } else {
                    ahCommand.openMainGui(player, 1);
                }
            }
            case "history", "history_type", "back_history" -> {
                soundManager.playClick(player);
                ahCommand.openHistoryTypeGui(player);
            }
            case "sell_history" -> {
                soundManager.playClick(player);
                ahCommand.openSellHistoryGui(player, 1);
            }
            case "buy_history" -> {
                soundManager.playClick(player);
                ahCommand.openBuyHistoryGui(player, 1);
            }
            case "ah_confirm_cancel" -> handleConfirmCancel(player);
            case "shulker_preview_back" -> handleShulkerPreviewBack(player);
            case "shulker_preview_buy" -> handleShulkerPreviewBuy(player);
            case "close" -> {
                soundManager.playClose(player);
                player.closeInventory();
            }
        }
    }

    private void handleClaimAll(Player player) {
        var items = plugin.getAuctionManager().getExpiredItems(player.getUniqueId());
        if (items.isEmpty()) {
            soundManager.playError(player);
            return;
        }

        boolean success = plugin.getAuctionManager().claimExpiredItems(player);
        if (success) {
            soundManager.playSuccess(player);
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.claimed_all",
                    Map.of("count", String.valueOf(items.size()))));
            player.closeInventory();
        } else {
            soundManager.playError(player);
        }
    }

    private void handleClaim(Player player) {
        if (!plugin.getAuctionManager().claimExpiredItems(player)) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.claim_failed"));
            soundManager.playError(player);
            return;
        }

        soundManager.playSuccess(player);
        player.sendMessage(plugin.getLanguageManager().get(player, "ah.claimed"));

        if (!plugin.getAuctionManager().getExpiredItems(player.getUniqueId()).isEmpty()) {
            ahCommand.openExpiredGui(player, 1);
        } else {
            player.closeInventory();
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.all_claimed"));
        }
    }

    private void handleAuctionClick(Player player, int id, boolean isOwn, ClickType click) {
        Optional<Auction> opt = plugin.getAuctionManager().getAuction(id);
        if (opt.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.not_found"));
            soundManager.playError(player);
            player.closeInventory();
            return;
        }

        Auction auction = opt.get();

        if (isOwn) {
            handleCancel(player, auction, click);
        } else if (click == ClickType.RIGHT && AhItemFactory.isShulkerBox(auction.getItem().getType())) {
            handleShulkerPreview(player, auction);
        } else {
            handleBuy(player, auction);
        }
    }

    private void handleShulkerPreview(Player player, Auction auction) {

        if (!player.hasPermission("essentialsc.ah.use")) {
            player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
            soundManager.playError(player);
            return;
        }

        String returnNav = "main";
        int returnPage = 1;
        String searchQuery = null;

        if (player.getOpenInventory().getTopInventory().getHolder() instanceof AhGuiHolder holder) {

            searchQuery = holder.getSearchQuery();
            String guiId = holder.getGuiId();
            returnPage = holder.getPage();
            if (guiId != null) {
                returnNav = switch (guiId) {
                    case "auction_search" -> searchQuery != null ? "search_" + searchQuery : "main";
                    case "auction_listings" -> "listings";
                    case "auction_main" -> "main";
                    default -> "main";
                };
            }
        }

        ahCommand.openShulkerPreviewGui(player, auction, returnNav, returnPage, searchQuery);
    }

    private void handleCancel(Player player, Auction auction, ClickType click) {
        if (click != ClickType.RIGHT) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.right_click_cancel"));
            soundManager.playClick(player);
            return;
        }

        if (!player.hasPermission("essentialsc.ah.cancel")) {
            player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
            soundManager.playError(player);
            return;
        }

        soundManager.playClick(player);
        player.closeInventory();

        plugin.getAuctionManager().cancelAuction(player, auction.getId()).thenAccept(success -> {
            player.getScheduler().run(plugin, scheduledTask -> {
                if (success) {
                    player.sendMessage(plugin.getLanguageManager().get(player, "ah.cancelled"));
                    soundManager.playCancel(player);
                } else {
                    player.sendMessage(plugin.getLanguageManager().get(player, "ah.cancel_failed"));
                    soundManager.playError(player);
                }
            }, null);
        });
    }

    private void handleBuy(Player player, Auction auction) {
        if (!player.hasPermission("essentialsc.ah.buy")) {
            player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
            soundManager.playError(player);
            return;
        }

        if (auction.getSellerUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.cannot_buy_own"));
            soundManager.playError(player);
            return;
        }

        soundManager.playClick(player);

        String searchQuery = null;
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof AhGuiHolder holder) {
            searchQuery = holder.getSearchQuery();
        }

        if (!plugin.getConfigManager().isAHConfirmationGuiEnabled()) {
            player.closeInventory();
            plugin.getAuctionManager().buyAuction(player, auction.getId()).thenAccept(success -> {
                player.getScheduler().run(plugin, scheduledTask -> {
                    if (success) {
                        player.sendMessage(plugin.getLanguageManager().get(player, "ah.purchased", Map.of(
                                "item", auction.getItem().getType().toString(),
                                "price", plugin.getEconomyManager().format(auction.getPrice())
                        )));
                        soundManager.playPurchase(player);
                    } else {
                        player.sendMessage(plugin.getLanguageManager().get(player, "ah.purchase_failed"));
                        soundManager.playError(player);
                    }
                }, null);
            });
            return;
        }

        ahCommand.openConfirmBuyGui(player, auction, searchQuery);
    }

    private void handleConfirmBuy(Player player, int auctionId) {
        Optional<Auction> opt = plugin.getAuctionManager().getAuction(auctionId);
        if (opt.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.not_found"));
            soundManager.playError(player);
            player.closeInventory();
            return;
        }

        Auction auction = opt.get();

        if (auction.getSellerUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.cannot_buy_own"));
            soundManager.playError(player);
            player.closeInventory();
            return;
        }

        soundManager.playClick(player);
        player.closeInventory();

        plugin.getAuctionManager().buyAuction(player, auction.getId()).thenAccept(success -> {
            player.getScheduler().run(plugin, scheduledTask -> {
                if (success) {
                    player.sendMessage(plugin.getLanguageManager().get(player, "ah.purchased", Map.of(
                            "item", auction.getItem().getType().toString(),
                            "price", plugin.getEconomyManager().format(auction.getPrice())
                    )));
                    soundManager.playPurchase(player);
                } else {
                    player.sendMessage(plugin.getLanguageManager().get(player, "ah.purchase_failed"));
                    soundManager.playError(player);
                }
            }, null);
        });
    }

    private void handleConfirmCancel(Player player) {
        soundManager.playClick(player);
        // Check if we have a search query stored in the current inventory holder
        String searchQuery = null;
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof AhGuiHolder holder) {
            searchQuery = holder.getSearchQuery();
        }
        if (searchQuery != null) {
            ahCommand.openSearchGui(player, searchQuery, 1);
        } else {
            ahCommand.openMainGui(player, 1);
        }
    }

    private void handleShulkerPreviewBack(Player player) {

        soundManager.playClick(player);
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof AhGuiHolder holder)) {
            ahCommand.openMainGui(player, 1);
            return;
        }

        String returnNav = holder.getReturnNav();
        int returnPage = holder.getReturnPage();

        if (returnNav == null) returnNav = "main";

        if (returnNav.startsWith("search_")) {
            String query = returnNav.substring(7);
            ahCommand.openSearchGui(player, query, returnPage);

        } else {
            
            switch (returnNav) {
                case "listings" -> ahCommand.openListingsGui(player, returnPage);
                case "expired" -> ahCommand.openExpiredGui(player, returnPage);
                case "sell_history" -> ahCommand.openSellHistoryGui(player, returnPage);
                case "buy_history" -> ahCommand.openBuyHistoryGui(player, returnPage);
                default -> ahCommand.openMainGui(player, returnPage);
            }
        }
    }

    private void handleShulkerPreviewBuy(Player player) {
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof AhGuiHolder holder)) return;

        int auctionId = holder.getAuctionId();
        Optional<Auction> opt = plugin.getAuctionManager().getAuction(auctionId);
        if (opt.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.not_found"));
            soundManager.playError(player);
            player.closeInventory();
            return;
        }

        Auction auction = opt.get();

        if (!player.hasPermission("essentialsc.ah.buy")) {
            player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
            soundManager.playError(player);
            return;
        }

        if (auction.getSellerUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.cannot_buy_own"));
            soundManager.playError(player);
            return;
        }

        soundManager.playClick(player);

        String searchQuery = holder.getSearchQuery();

        if (!plugin.getConfigManager().isAHConfirmationGuiEnabled()) {
            player.closeInventory();
            plugin.getAuctionManager().buyAuction(player, auction.getId()).thenAccept(success -> {
                player.getScheduler().run(plugin, scheduledTask -> {
                    if (success) {
                        player.sendMessage(plugin.getLanguageManager().get(player, "ah.purchased", Map.of(
                                "item", auction.getItem().getType().toString(),
                                "price", plugin.getEconomyManager().format(auction.getPrice())
                        )));
                        soundManager.playPurchase(player);
                    } else {
                        player.sendMessage(plugin.getLanguageManager().get(player, "ah.purchase_failed"));
                        soundManager.playError(player);
                    }
                }, null);
            });
            return;
        }

        ahCommand.openConfirmBuyGui(player, auction, searchQuery);
    }
}