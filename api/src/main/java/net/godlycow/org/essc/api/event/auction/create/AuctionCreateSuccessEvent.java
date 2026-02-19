package net.godlycow.org.essc.api.event.auction.create;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class AuctionCreateSuccessEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player seller;
    private final BigDecimal price;
    private final long duration;

    public AuctionCreateSuccessEvent(@NotNull Player seller, @NotNull BigDecimal price, long duration) {
        super(false); // Runs on the main server thread
        this.seller = seller;
        this.price = price;
        this.duration = duration;
    }

    // Get the player who created the auction
    @NotNull
    public Player getSeller() {
        return seller;
    }

    // Get the price the item was listed for
    @NotNull
    public BigDecimal getPrice() {
        return price;
    }

    // Get how long the auction will last in seconds
    public long getDuration() {
        return duration;
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