package net.godlycow.org.essc.api.event.auction.expire;

import net.godlycow.org.essc.api.event.auction.Auction;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class AuctionExpireEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final int auctionId;
    private final UUID sellerUuid;
    private final String sellerName;

    public AuctionExpireEvent(@NotNull Auction auction) {
        super(false); // Runs on the main server thread
        this.auctionId = auction.getId();
        this.sellerUuid = auction.getSellerUuid();
        this.sellerName = auction.getSellerName();
    }

    // Get the unique ID number of the auction that ended
    public int getAuctionId() {
        return auctionId;
    }

    // Get the UUID of the player who was selling the item
    @NotNull
    public UUID getSellerUuid() {
        return sellerUuid;
    }

    // Get the name of the player who was selling the item
    @NotNull
    public String getSellerName() {
        return sellerName;
    }

    // Required Bukkit method for event handling
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    // Required Bukkit method for event handling
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}