package net.godlycow.org.essc.listener;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class InvseeListener implements Listener {
    private final EssentialsC plugin;

    public InvseeListener(EssentialsC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;
        if (!(event.getInventory().getHolder() instanceof Player target)) return;

        if (viewer == target) return;

        if (event.getRawSlot() < event.getInventory().getSize()) {
            if (!viewer.hasPermission("essentialsc.invsee.modify")) {
                event.setCancelled(true);
                viewer.sendMessage(plugin.getLanguageManager().get(viewer, "invsee.no_modify"));
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;
        if (!(event.getInventory().getHolder() instanceof Player target)) return;

        if (viewer == target) return;

        int topSize = event.getInventory().getSize();
        boolean affectsTop = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);

        if (affectsTop && !viewer.hasPermission("essentialsc.invsee.modify")) {
            event.setCancelled(true);
            viewer.sendMessage(plugin.getLanguageManager().get(viewer, "invsee.no_modify"));
        }
    }
}