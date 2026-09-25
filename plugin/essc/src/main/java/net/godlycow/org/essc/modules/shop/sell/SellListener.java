package net.godlycow.org.essc.modules.shop.sell;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.util.InventoryViewCompat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class SellListener implements Listener {
    private final EssentialsC plugin;
    private SellManager sellManager;

    public SellListener(EssentialsC plugin) {
        this.plugin = plugin;
    }

    public void setSellManager(SellManager sellManager) {
        this.sellManager = sellManager;
    }

    public void registerGUI(Player player, SellGUI gui) {
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = InventoryViewCompat.safeHolder(event.getInventory());
        if (!(holder instanceof SellHolder)) return;

        SellGUI gui = sellManager != null ? sellManager.getActiveGUI(player) : null;
        if (gui == null) return;

        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;

        if (InventoryViewCompat.safeHolder(clickedInv) instanceof SellHolder) {
            int slot = event.getRawSlot();

            if (slot < 0 || slot >= event.getInventory().getSize()) return;

            if (gui.isConfirmSlot(slot)) {
                event.setCancelled(true);
                gui.processSale();
                return;
            }

            if (gui.isCancelSlot(slot)) {
                event.setCancelled(true);
                gui.cancel();
                return;
            }

            if (gui.isBorderSlot(slot) || !gui.isInputSlot(slot)) {
                event.setCancelled(true);
                return;
            }

        } else if (event.isShiftClick()) {
            event.setCancelled(true);

            ItemStack cursorItem = event.getCurrentItem();
            if (cursorItem == null || cursorItem.getType().isAir()) return;

            for (int inputSlot : SellGUI.getInputSlots()) {
                ItemStack existing = gui.getInventory().getItem(inputSlot);

                if (existing == null || existing.getType().isAir()) {
                    gui.getInventory().setItem(inputSlot, cursorItem.clone());
                    event.setCurrentItem(null);
                    break;
                }
            }
        }

        player.getScheduler().runDelayed(plugin, task -> {
            SellGUI currentGUI = sellManager != null
                    ? sellManager.getActiveGUI(player)
                    : null;

            if (currentGUI != null && !currentGUI.isProcessed()) {
                currentGUI.updateButtons();
            }
        }, null, 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = InventoryViewCompat.safeHolder(event.getView().getTopInventory());
        if (!(holder instanceof SellHolder)) return;

        SellGUI gui = sellManager != null ? sellManager.getActiveGUI(player) : null;
        if (gui == null) return;

        Inventory topInv = event.getView().getTopInventory();

        for (int slot : event.getRawSlots()) {
            if (slot < topInv.getSize()) {
                if (!gui.isInputSlot(slot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        player.getScheduler().runDelayed(plugin, task -> {
            SellGUI currentGUI = sellManager != null ? sellManager.getActiveGUI(player) : null;
            if (currentGUI != null) {
                currentGUI.updateButtons();
            }
        }, null, 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        InventoryHolder holder = InventoryViewCompat.safeHolder(event.getInventory());
        if (!(holder instanceof SellHolder)) return;

        SellGUI gui = sellManager != null ? sellManager.getActiveGUI(player) : null;
        if (gui != null && !gui.isProcessed()) {
            gui.onClose();
        }
        if (sellManager != null) {
            sellManager.unregisterGUI(player);
        }
    }
}