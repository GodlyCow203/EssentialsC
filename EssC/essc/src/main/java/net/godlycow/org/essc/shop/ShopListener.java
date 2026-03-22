package net.godlycow.org.essc.shop;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopListener implements Listener {
    private final EssentialsC plugin;
    private final ShopManager shopManager;
    private final Map<UUID, ShopSession> sessions = new HashMap<>();

    private static final int SLOT_PREV_PAGE       = 45;
    private static final int SLOT_BALANCE_CATEGORY = 47;
    private static final int SLOT_BACK_BUTTON      = 48;
    private static final int SLOT_CLOSE_BUTTON     = 48;
    private static final int SLOT_PAGE_INDICATOR   = 49;
    private static final int SLOT_BALANCE_MAIN     = 50;
    private static final int SLOT_NEXT_PAGE        = 53;

    public ShopListener(EssentialsC plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof ShopHolder shopHolder)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        if (shopHolder.isMain()) {
            handleMainMenuClick(player, event.getSlot());
        } else {
            handleCategoryClick(player, shopHolder, event.getSlot(),
                    event.isShiftClick(), event.isLeftClick(), event.isRightClick());
        }
    }

    private void handleMainMenuClick(Player player, int slot) {
        if (slot == SLOT_BALANCE_MAIN) {
            shopManager.openMainShop(player);
            return;
        }

        if (slot == SLOT_CLOSE_BUTTON) {
            player.closeInventory();
            removeSession(player);
            return;
        }

        for (ShopCategory category : shopManager.getCategories().values()) {
            if (!category.isEnabled()) continue;
            if (category.getPermission() != null && !player.hasPermission(category.getPermission())) continue;

            if (category.getSlot() == slot) {
                setSession(player, new ShopSession(category.getId(), 1));
                shopManager.openCategory(player, category.getId(), 1);
                return;
            }
        }
    }

    private void handleCategoryClick(Player player, ShopHolder holder, int slot,
                                     boolean shift, boolean left, boolean right) {
        String categoryId = holder.getCategoryId();
        int page          = holder.getPage();

        ShopCategory category = shopManager.getCategory(categoryId);
        if (category == null) {
            removeSession(player);
            shopManager.openMainShop(player);
            return;
        }

        if (slot == SLOT_PREV_PAGE && left) {
            if (page > 1) {
                int newPage = page - 1;
                setSession(player, new ShopSession(categoryId, newPage));
                shopManager.openCategory(player, categoryId, newPage);
            }
            return;
        }

        if (slot == SLOT_NEXT_PAGE && left) {
            if (page < category.getMaxPage()) {
                int newPage = page + 1;
                setSession(player, new ShopSession(categoryId, newPage));
                shopManager.openCategory(player, categoryId, newPage);
            }
            return;
        }

        if (slot == SLOT_PAGE_INDICATOR) return;

        if (slot == SLOT_BALANCE_CATEGORY) {
            shopManager.openCategory(player, categoryId, page);
            return;
        }

        if (slot == SLOT_BACK_BUTTON) {
            removeSession(player);
            shopManager.openMainShop(player);
            return;
        }

        ShopItem item = category.getPageItems(page).get(slot);
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof ShopHolder)) return;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Inventory current = player.getOpenInventory().getTopInventory();
            if (!(current.getHolder() instanceof ShopHolder)) {
                removeSession(player);
            }
        });
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