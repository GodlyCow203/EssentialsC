package net.godlycow.org.essc.modules.kit.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class KitGuiHolder implements InventoryHolder {

    private final int page;
    private Inventory inventory;

    public KitGuiHolder(int page) {
        this.page = page;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public int getPage() {
        return page;
    }
}