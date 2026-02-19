package net.godlycow.org.essc.api.event.auction.buy;

import net.godlycow.org.essc.api.event.auction.Auction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class AuctionBuyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player buyer;
    private final Auction auction;

    public AuctionBuyEvent(@NotNull Player buyer, @NotNull Auction auction) {
        super(false); // Runs on the main server thread
        this.buyer = buyer;
        this.auction = auction;
    }

    // Get the player who is trying to buy the item
    @NotNull
    public Player getBuyer() {
        return buyer;
    }

    // Get the auction information for the item being sold
    @NotNull
    public Auction getAuction() {
        return auction;
    }

    // Check if the purchase has been stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the player from buying the item
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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