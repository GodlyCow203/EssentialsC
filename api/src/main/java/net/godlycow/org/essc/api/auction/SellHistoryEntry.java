package net.godlycow.org.essc.api.auction;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An immutable record of a completed sale in the EssentialsC Auction House,
 * from the seller's perspective.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.AuctionApi#getSellHistory(UUID)}.
 * History is kept in memory, ordered most-recent-first, and capped at 100 entries per player.</p>
 *
 * <p>All {@link ItemStack} getters return a defensive clone.</p>
 *
 * @see net.godlycow.org.essc.api.AuctionApi#getSellHistory(UUID)
 */
public class SellHistoryEntry {

    private final int auctionId;
    private final UUID sellerUuid;
    private final String buyerName;
    private final ItemStack item;
    private final BigDecimal price;
    private final long timestamp;

    /**
     * Constructs a {@code SellHistoryEntry}. Intended to be called by the EssentialsC
     * implementation only.
     *
     * @param auctionId  the ID of the auction that was sold
     * @param sellerUuid the UUID of the seller; must not be {@code null}
     * @param buyerName  the display name of the buyer at the time of purchase; must not be {@code null}
     * @param item       the item that was sold; cloned internally, must not be {@code null}
     * @param price      the price the item sold for; must not be {@code null}
     * @param timestamp  the epoch-millisecond timestamp of the sale
     */
    public SellHistoryEntry(int auctionId, UUID sellerUuid, String buyerName,
                            ItemStack item, BigDecimal price, long timestamp) {
        this.auctionId = auctionId;
        this.sellerUuid = sellerUuid;
        this.buyerName = buyerName;
        this.item = item.clone();
        this.price = price;
        this.timestamp = timestamp;
    }

    /**
     * Returns the ID of the auction associated with this sale.
     *
     * @return the auction ID
     */
    public int getAuctionId() { return auctionId; }

    /**
     * Returns the UUID of the seller.
     *
     * @return the seller's UUID; never {@code null}
     */
    public UUID getSellerUuid() { return sellerUuid; }

    /**
     * Returns the display name of the buyer at the time of purchase.
     *
     * <p>This value is a snapshot — it will not update if the player later changes
     * their name.</p>
     *
     * @return the buyer's name; never {@code null}
     */
    public String getBuyerName() { return buyerName; }

    /**
     * Returns a copy of the item that was sold.
     *
     * <p>The returned {@link ItemStack} is a defensive clone.</p>
     *
     * @return the sold item; never {@code null}
     */
    public ItemStack getItem() { return item.clone(); }

    /**
     * Returns the price this item sold for.
     *
     * @return the sale price; never {@code null}
     */
    public BigDecimal getPrice() { return price; }

    /**
     * Returns the epoch-millisecond timestamp of when this sale occurred.
     *
     * @return sale time in milliseconds since the Unix epoch
     */
    public long getTimestamp() { return timestamp; }
}
