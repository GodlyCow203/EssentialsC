package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.event.auction.Auction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;


public class AuctionImpl implements Auction {
    private final int id;
    private final UUID sellerUuid;
    private final String sellerName;
    private final ItemStack item;
    private final BigDecimal price;
    private final long listedTime;
    private final long duration;

    public AuctionImpl(int id, @NotNull UUID sellerUuid, @NotNull String sellerName,
                       @NotNull ItemStack item, @NotNull BigDecimal price,
                       long listedTime, long duration) {
        this.id = id;
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.price = price;
        this.listedTime = listedTime;
        this.duration = duration;
    }

    public static AuctionImpl fromInternal(net.godlycow.org.essc.auction.Auction auction) {
        return new AuctionImpl(
                auction.getId(),
                auction.getSellerUuid(),
                auction.getSellerName(),
                auction.getItem(),
                auction.getPrice(),
                auction.getListedTime(),
                auction.getDuration()
        );
    }

    @Override
    public int getId() { return id; }

    @Override
    @NotNull
    public UUID getSellerUuid() { return sellerUuid; }

    @Override
    @NotNull
    public String getSellerName() { return sellerName; }

    @Override
    @NotNull
    public ItemStack getItem() { return item.clone(); }

    @Override
    @NotNull
    public BigDecimal getPrice() { return price; }

    @Override
    public long getListedTime() { return listedTime; }

    @Override
    public long getDuration() { return duration; }

    @Override
    public long getExpiryTime() { return listedTime + duration; }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis() > getExpiryTime();
    }

    @Override
    public long getTimeRemaining() {
        long remaining = getExpiryTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    @Override
    @NotNull
    public Auction withPrice(@NotNull BigDecimal price) {
        return new AuctionImpl(id, sellerUuid, sellerName, item, price, listedTime, duration);
    }

    @Override
    @NotNull
    public Auction withDuration(long duration) {
        return new AuctionImpl(id, sellerUuid, sellerName, item, price, listedTime, duration);
    }

    @Override
    public String toString() {
        return "AuctionImpl{" +
                "id=" + id +
                ", seller=" + sellerName +
                ", price=" + price +
                ", expired=" + isExpired() +
                '}';
    }
}