package net.godlycow.org.essc.modules.auction.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AhGuiHolder implements InventoryHolder {

    private final String guiId;
    private final int page;
    private int auctionId = -1;

    public AhGuiHolder(String guiId, int page) {
        this.guiId = guiId;
        this.page = page;
    }

    public AhGuiHolder(String guiId, int page, int auctionId) {
        this.guiId = guiId;
        this.page = page;
        this.auctionId = auctionId;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public String getGuiId() {
        return guiId;
    }

    public int getPage() {
        return page;
    }

    public int getAuctionId() {
        return auctionId;
    }
}
