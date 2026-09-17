package net.godlycow.org.essc.modules.auction.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AhGuiHolder implements InventoryHolder {

    private final String guiId;
    private final int page;
    private int auctionId = -1;
    private String searchQuery;
    private String returnNav;
    private int returnPage = 1;

    public AhGuiHolder(String guiId, int page) {
        this.guiId = guiId;
        this.page = page;
    }

    public AhGuiHolder(String guiId, int page, int auctionId) {
        this.guiId = guiId;
        this.page = page;
        this.auctionId = auctionId;
    }

    public AhGuiHolder(String guiId, int page, int auctionId, String searchQuery) {
        this.guiId = guiId;
        this.page = page;
        this.auctionId = auctionId;
        this.searchQuery = searchQuery;
    }

    public AhGuiHolder(String guiId, int page, String searchQuery) {
        this.guiId = guiId;
        this.page = page;
        this.searchQuery = searchQuery;
    }

    public AhGuiHolder(String guiId, int page, int auctionId, String searchQuery, String returnNav, int returnPage) {
        this.guiId = guiId;
        this.page = page;
        this.auctionId = auctionId;
        this.searchQuery = searchQuery;
        this.returnNav = returnNav;
        this.returnPage = returnPage;
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

    public String getSearchQuery() {
        return searchQuery;
    }

    public String getReturnNav() {
        return returnNav;
    }

    public int getReturnPage() {
        return returnPage;
    }
}
