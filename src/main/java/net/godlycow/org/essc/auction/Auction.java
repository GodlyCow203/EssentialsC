package net.godlycow.org.essc.auction;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;

public class Auction {
    private final int id;
    private final UUID sellerUuid;
    private final String sellerName;
    private final ItemStack item;
    private final BigDecimal price;
    private final long listedTime;
    private final long duration;

    public Auction(int id, UUID sellerUuid, String sellerName, ItemStack item, BigDecimal price, long listedTime, long duration) {
        this.id = id;
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        this.item = item;
        this.price = price;
        this.listedTime = listedTime;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public UUID getSellerUuid() {
        return sellerUuid;
    }

    public String getSellerName() {
        return sellerName;
    }

    public ItemStack getItem() {
        return item;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getListedTime() {
        return listedTime;
    }

    public long getDuration() {
        return duration;
    }

    public long getExpiryTime() {
        return listedTime + duration;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > getExpiryTime();
    }

    public long getTimeRemaining() {
        long remaining = getExpiryTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}