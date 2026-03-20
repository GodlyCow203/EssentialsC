package net.godlycow.org.essc.api.auction;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single active auction listing in EssentialsC's Auction House.
 *
 * <p>Instances are obtained via {@link net.godlycow.org.essc.api.AuctionApi} — for example
 * {@link net.godlycow.org.essc.api.AuctionApi#getActiveAuctions()} or
 * {@link net.godlycow.org.essc.api.AuctionApi#getAuction(int)}.</p>
 *
 * <p>All {@link ItemStack} getters return a defensive clone; modifying the returned
 * item has no effect on the listing.</p>
 *
 * @see net.godlycow.org.essc.api.AuctionApi
 */
public class Auction {

    private final int id;
    private final UUID sellerUuid;
    private final String sellerName;
    private final ItemStack item;
    private final BigDecimal price;
    private final long listedTime;
    private final long duration;

    /**
     * Constructs an {@code Auction}. Intended to be called by the EssentialsC implementation only.
     *
     * @param id         the unique auction ID assigned by the database
     * @param sellerUuid the UUID of the player who listed the auction; must not be {@code null}
     * @param sellerName the display name of the seller at the time of listing; must not be {@code null}
     * @param item       the item being sold; cloned internally, must not be {@code null}
     * @param price      the buy-now price; must be positive, must not be {@code null}
     * @param listedTime the epoch-millisecond timestamp when the auction was created
     * @param duration   the auction's total duration in milliseconds; must be positive
     */
    public Auction(int id, UUID sellerUuid, String sellerName, ItemStack item,
                   BigDecimal price, long listedTime, long duration) {
        this.id = id;
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.price = price;
        this.listedTime = listedTime;
        this.duration = duration;
    }

    /**
     * Returns the unique database ID for this auction.
     *
     * @return the auction ID; always positive
     */
    public int getId() { return id; }

    /**
     * Returns the UUID of the player who created this listing.
     *
     * @return the seller's UUID; never {@code null}
     */
    public UUID getSellerUuid() { return sellerUuid; }

    /**
     * Returns the display name of the seller as it was at the time of listing.
     *
     * <p>This value is a snapshot — it will not update if the player later changes
     * their name.</p>
     *
     * @return the seller's name; never {@code null}
     */
    public String getSellerName() { return sellerName; }

    /**
     * Returns a copy of the item being sold.
     *
     * <p>The returned {@link ItemStack} is a defensive clone. Modifying it has
     * no effect on the auction or the internal state of the Auction House.</p>
     *
     * @return the listed item; never {@code null}
     */
    public ItemStack getItem() { return item.clone(); }

    /**
     * Returns the buy-now price of this auction.
     *
     * @return the listing price; always positive, never {@code null}
     */
    public BigDecimal getPrice() { return price; }

    /**
     * Returns the epoch-millisecond timestamp at which this auction was created.
     *
     * @return listing time in milliseconds since the Unix epoch
     */
    public long getListedTime() { return listedTime; }

    /**
     * Returns the total duration of this auction in milliseconds.
     *
     * @return duration in milliseconds; always positive
     */
    public long getDuration() { return duration; }

    /**
     * Returns the epoch-millisecond timestamp at which this auction expires.
     *
     * <p>Equivalent to {@code getListedTime() + getDuration()}.</p>
     *
     * @return expiry time in milliseconds since the Unix epoch
     */
    public long getExpiryTime() { return listedTime + duration; }

    /**
     * Returns whether this auction has passed its expiry time.
     *
     * <p>An expired auction may still appear in active-auction queries until the
     * internal expiry task processes it. Use this method to filter client-side
     * if necessary.</p>
     *
     * @return {@code true} if the current time is past {@link #getExpiryTime()}
     */
    public boolean isExpired() { return System.currentTimeMillis() > getExpiryTime(); }

    /**
     * Returns the time remaining until this auction expires, in milliseconds.
     *
     * @return milliseconds until expiry, or {@code 0} if already expired
     */
    public long getTimeRemaining() { return Math.max(0, getExpiryTime() - System.currentTimeMillis()); }
}
