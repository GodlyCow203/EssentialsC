package net.godlycow.org.essc.listener;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.auction.AhSession;
import net.godlycow.org.essc.auction.Auction;
import net.godlycow.org.essc.auction.AuctionManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AhListener implements Listener {
    private final EssentialsC plugin;
    private final NamespacedKey auctionIdKey;
    private final NamespacedKey ownAuctionKey;
    private final NamespacedKey navPageKey;
    private final NamespacedKey navExpiredKey;
    private final NamespacedKey actionKey;
    private final NamespacedKey closeKey;
    private final NamespacedKey claimableKey;

    public AhListener(EssentialsC plugin) {
        this.plugin = plugin;
        this.auctionIdKey = new NamespacedKey(plugin, "ah_auction_id");
        this.ownAuctionKey = new NamespacedKey(plugin, "ah_own_auction");
        this.navPageKey = new NamespacedKey(plugin, "ah_nav_page");
        this.navExpiredKey = new NamespacedKey(plugin, "ah_nav_expired");
        this.actionKey = new NamespacedKey(plugin, "ah_action");
        this.closeKey = new NamespacedKey(plugin, "ah_close");
        this.claimableKey = new NamespacedKey(plugin, "ah_claimable");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().isAHEnabled()) {
            return;
        }

        String title;
        try {
            title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        } catch (Exception e) {
            title = event.getView().getTitle();
        }

        if (!title.contains("Auction House") && !title.contains("Expired Items")) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (plugin.getAuctionManager() == null) {
            player.closeInventory();
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.not_loaded"));
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(closeKey, PersistentDataType.BYTE)) {
            player.closeInventory();
            return;
        }

        if (container.has(navPageKey, PersistentDataType.INTEGER)) {
            int targetPage = container.get(navPageKey, PersistentDataType.INTEGER);
            boolean isExpired = container.getOrDefault(navExpiredKey, PersistentDataType.BYTE, (byte) 0) == 1;

            player.closeInventory();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isExpired) {
                    player.performCommand("ah expired");
                } else {
                    player.performCommand("ah " + targetPage);
                }
            }, 1L);
            return;
        }

        if (container.has(actionKey, PersistentDataType.STRING)) {
            String action = container.get(actionKey, PersistentDataType.STRING);

            if (action.equals("sell") && !player.hasPermission(AuctionManager.PERM_SELL)) {
                player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
                return;
            }

            player.closeInventory();

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                switch (action) {
                    case "sell" -> player.performCommand("ah sell");
                    case "expired" -> player.performCommand("ah expired");
                    case "listings" -> player.performCommand("ah listings");
                }
            }, 1L);
            return;
        }

        if (container.has(claimableKey, PersistentDataType.BYTE)) {
            boolean success = plugin.getAuctionManager().claimExpiredItems(player);
            if (success) {
                player.sendMessage(plugin.getLanguageManager().get(player, "ah.claimed_expired"));
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    player.performCommand("ah expired");
                }, 1L);
            }
            return;
        }

        if (container.has(auctionIdKey, PersistentDataType.INTEGER)) {
            int auctionId = container.get(auctionIdKey, PersistentDataType.INTEGER);
            boolean isOwnAuction = container.has(ownAuctionKey, PersistentDataType.BYTE);

            Optional<Auction> auctionOpt = plugin.getAuctionManager().getAuction(auctionId);
            if (auctionOpt.isEmpty()) {
                player.sendMessage(plugin.getLanguageManager().get(player, "ah.auction_not_found"));
                player.closeInventory();
                return;
            }

            Auction auction = auctionOpt.get();

            if (isOwnAuction) {
                if (event.getClick() != ClickType.RIGHT) {
                    player.sendMessage(plugin.getLanguageManager().get(player, "ah.right_click_to_cancel"));
                    return;
                }

                if (!player.hasPermission(AuctionManager.PERM_CANCEL)) {
                    player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
                    return;
                }

                plugin.getAuctionManager().cancelAuction(player, auctionId).thenAccept(success -> {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (success) {
                            player.sendMessage(plugin.getLanguageManager().get(player, "ah.cancelled"));
                        } else {
                            player.sendMessage(plugin.getLanguageManager().get(player, "ah.cancel_failed"));
                        }
                        player.closeInventory();
                    });
                });
                return;
            }


            if (!player.hasPermission(AuctionManager.PERM_BUY)) {
                player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
                return;
            }

            handlePurchase(player, auction);
            return;
        }

        if (title.contains("Expired Items") && event.getSlot() == 49) {
            player.closeInventory();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.performCommand("ah");
            }, 1L);
            return;
        }
    }

    private void handlePurchase(Player player, Auction auction) {
        if (auction.getSellerUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "ah.cannot_buy_own"));
            return;
        }

        player.closeInventory();

        plugin.getAuctionManager().buyAuction(player, auction.getId()).thenAccept(success -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.sendMessage(plugin.getLanguageManager().get(player, "ah.purchased",
                            Map.of("item", auction.getItem().getType().toString(),
                                    "price", plugin.getEconomyManager().format(auction.getPrice()))));
                } else {
                    player.sendMessage(plugin.getLanguageManager().get(player, "ah.purchase_failed"));
                }
            });
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title;
        try {
            title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        } catch (Exception e) {
            title = event.getView().getTitle();
        }

        if (title.contains("Auction House") || title.contains("Expired Items")) {
            player.removeMetadata("ah_session", plugin);
        }
    }
}