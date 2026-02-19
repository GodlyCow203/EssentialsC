package net.godlycow.org.essc.api.event.auction.buy;

import net.godlycow.org.essc.api.event.auction.Auction;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class AuctionBuySuccessEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player buyer;
    private final Auction auction;

    public AuctionBuySuccessEvent(@NotNull Player buyer, @NotNull Auction auction) {
        super(false); // This runs on the main server thread
        this.buyer = buyer;
        this.auction = auction;
    }

    // Get the player who bought the item
    @NotNull
    public Player getBuyer() {
        return buyer;
    }

    // Get the specific auction that was completed
    @NotNull
    public Auction getAuction() {
        return auction;
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