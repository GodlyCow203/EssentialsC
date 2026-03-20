package net.godlycow.org.essc.api.auction;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An immutable record of a completed purchase in the EssentialsC Auction House,
 * from the buyer's perspective.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.AuctionApi#getBuyHistory(UUID)}.
 * History is kept in memory, ordered most-recent-first, and capped at 100 entries per player.</p>
 *
 * <p>All {@link ItemStack} getters return a defensive clone.</p>
 *
 * @see net.godlycow.org.essc.api.AuctionApi#getBuyHistory(UUID)
 */
public class BuyHistoryEntry {

    private final int auctionId;
    private final UUID buyerUuid;
    private final String sellerName;
    private final ItemStack item;
    private final BigDecimal price;
    private final long timestamp;

    /**
     * Constructs a {@code BuyHistoryEntry}. Intended to be called by the EssentialsC
     * implementation only.
     *
     * @param auctionId  the ID of the auction that was purchased
     * @param buyerUuid  the UUID of the buyer; must not be {@code null}
     * @param sellerName the display name of the seller at the time of listing; must not be {@code null}
     * @param item       the item that was purchased; cloned internally, must not be {@code null}
     * @param price      the price paid; must not be {@code null}
     * @param timestamp  the epoch-millisecond timestamp of the purchase
     */
    public BuyHistoryEntry(int auctionId, UUID buyerUuid, String sellerName,
                           ItemStack item, BigDecimal price, long timestamp) {
        this.auctionId = auctionId;
        this.buyerUuid = buyerUuid;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.price = price;
        this.timestamp = timestamp;
    }

    /**
     * Returns the ID of the auction that was purchased.
     *
     * @return the auction ID
     */
    public int getAuctionId() { return auctionId; }

    /**
     * Returns the UUID of the buyer.
     *
     * @return the buyer's UUID; never {@code null}
     */
    public UUID getBuyerUuid() { return buyerUuid; }

    /**
     * Returns the display name of the seller at the time of listing.
     *
     * <p>This value is a snapshot — it will not update if the player later changes
     * their name.</p>
     *
     * @return the seller's name; never {@code null}
     */
    public String getSellerName() { return sellerName; }

    /**
     * Returns a copy of the item that was purchased.
     *
     * <p>The returned {@link ItemStack} is a defensive clone.</p>
     *
     * @return the purchased item; never {@code null}
     */
    public ItemStack getItem() { return item.clone(); }

    /**
     * Returns the price paid for this purchase.
     *
     * @return the purchase price; never {@code null}
     */
    public BigDecimal getPrice() { return price; }

    /**
     * Returns the epoch-millisecond timestamp of when this purchase occurred.
     *
     * @return purchase time in milliseconds since the Unix epoch
     */
    public long getTimestamp() { return timestamp; }
}
