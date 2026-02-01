package net.godlycow.org.essc.listener;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class EnderSeeListener implements Listener {

    private final EssentialsC plugin;

    public EnderSeeListener(EssentialsC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof Player target)) return;
        if (!inventory.equals(target.getEnderChest())) return;
        if (viewer.equals(target)) return;

        if (event.getRawSlot() < inventory.getSize()) {
            if (!viewer.hasPermission("essentialsc.endersee.modify")) {
                event.setCancelled(true);
                viewer.sendMessage(
                        plugin.getLanguageManager().get(viewer, "endersee.no_modify")
                );
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof Player target)) return;

        if (!inventory.equals(target.getEnderChest())) return;

        if (viewer.equals(target)) return;

        int topSize = inventory.getSize();
        boolean affectsTop = event.getRawSlots().stream()
                .anyMatch(slot -> slot < topSize);

        if (affectsTop && !viewer.hasPermission("essentialsc.endersee.modify")) {
            event.setCancelled(true);
            viewer.sendMessage(
                    plugin.getLanguageManager().get(viewer, "endersee.no_modify")
            );
        }
    }
}
