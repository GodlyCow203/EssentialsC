package net.godlycow.org.essc.modules.shop.sell;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.godlycow.org.essc.util.InventoryViewCompat;

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

        Inventory topInv = InventoryViewCompat.getTopInventory(player);
        if (topInv == null) return;

        InventoryHolder holder = topInv.getHolder();
        if (!(holder instanceof SellHolder)) return;

        SellGUI gui = sellManager != null ? sellManager.getActiveGUI(player) : null;
        if (gui == null) return;

        Inventory clickedInv = event.getClickedInventory();
        int slot = event.getSlot();

        if (clickedInv == topInv) {
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

            if (gui.isBorderSlot(slot)) {
                event.setCancelled(true);
                return;
            }

            if (!gui.isInputSlot(slot)) {
                event.setCancelled(true);
                return;
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
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInv = InventoryViewCompat.getTopInventory(player);
        if (topInv == null) return;

        InventoryHolder holder = topInv.getHolder();
        if (!(holder instanceof SellHolder)) return;

        SellGUI gui = sellManager != null ? sellManager.getActiveGUI(player) : null;
        if (gui == null) return;

        for (int slot : event.getRawSlots()) {
            if (slot < topInv.getSize()) {
                if (!gui.isInputSlot(slot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        player.getScheduler().runDelayed(plugin, task -> gui.updateButtons(), null, 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
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