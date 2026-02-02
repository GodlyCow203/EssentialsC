package net.godlycow.org.essc.shop;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopListener implements Listener {
    private final EssentialsC plugin;
    private final ShopManager shopManager;
    private final Map<UUID, ShopSession> sessions = new HashMap<>();
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    public ShopListener(EssentialsC plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = plainSerializer.serialize(event.getView().title());
        String mainTitle = plainSerializer.serialize(
                plugin.getMiniMessage().deserialize(plugin.getConfigManager().getShopMainMenuTitle())
        );

        boolean isMainShop = title.equals(mainTitle);
        boolean isCategoryShop = !isMainShop && title.contains("Shop");

        if (!isMainShop && !isCategoryShop) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        if (isMainShop) {
            handleMainMenuClick(player, clicked, event.getSlot());
        } else {
            handleCategoryClick(player, clicked, event.getSlot(),
                    event.isShiftClick(), event.isLeftClick(), event.isRightClick());
        }
    }

    private void handleMainMenuClick(Player player, ItemStack clicked, int slot) {
        for (ShopCategory category : shopManager.getCategories().values()) {
            if (!category.isEnabled()) continue;
            if (category.getPermission() != null && !player.hasPermission(category.getPermission())) continue;

            if (category.getSlot() == slot) {
                setSession(player, new ShopSession(category.getId(), 1));
                shopManager.openCategory(player, category.getId(), 1);
                return;
            }
        }

        if (clicked.getType() == Material.PLAYER_HEAD) {
            shopManager.openMainShop(player);
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            removeSession(player);
        }
    }

    private void handleCategoryClick(Player player, ItemStack clicked, int slot,
                                     boolean shift, boolean left, boolean right) {
        ShopSession session = getSession(player);
        if (session == null) {
            shopManager.openMainShop(player);
            return;
        }

        ShopCategory category = shopManager.getCategory(session.getCategoryId());
        if (category == null) {
            removeSession(player);
            shopManager.openMainShop(player);
            return;
        }

        if (slot == 45 && left) {
            if (session.getPage() > 1) {
                int newPage = session.getPage() - 1;
                setSession(player, new ShopSession(session.getCategoryId(), newPage));
                shopManager.openCategory(player, session.getCategoryId(), newPage);
            }
            return;
        }

        if (slot == 53 && left) {
            int maxPage = category.getMaxPage();
            if (session.getPage() < maxPage) {
                int newPage = session.getPage() + 1;
                setSession(player, new ShopSession(session.getCategoryId(), newPage));
                shopManager.openCategory(player, session.getCategoryId(), newPage);
            }
            return;
        }

        if (slot == 49 && clicked.getType() == Material.BARRIER) {
            removeSession(player);
            shopManager.openMainShop(player);
            return;
        }

        if (clicked.getType() == Material.PLAYER_HEAD) {
            shopManager.openCategory(player, session.getCategoryId(), session.getPage());
            return;
        }

        ShopItem item = findItemBySlot(category, session.getPage(), slot);
        if (item == null) return;

        if (item.getPermission() != null && !player.hasPermission(item.getPermission())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
            return;
        }

        if (left) {
            int amount = shift ? Math.min(64, item.getMaxStack()) : 1;
            shopManager.processPurchase(player, item, amount);
        } else if (right) {
            int amount = shift ? Math.min(64, item.getMaxStack()) : 1;
            shopManager.processSale(player, item, amount);
        }
    }

    private ShopItem findItemBySlot(ShopCategory category, int page, int slot) {
        Map<Integer, ShopItem> pageItems = category.getPageItems(page);
        return pageItems.get(slot);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        removeSession(player);
    }

    public ShopSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void setSession(Player player, ShopSession session) {
        sessions.put(player.getUniqueId(), session);
    }

    public void removeSession(Player player) {
        sessions.remove(player.getUniqueId());
    }
}