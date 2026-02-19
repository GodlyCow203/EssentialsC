package net.godlycow.org.essc.api.event.auction.cancel;

import net.godlycow.org.essc.api.event.auction.Auction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class AuctionCancelEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player canceller;
    private final Auction auction;
    private final boolean adminCancel;

    public AuctionCancelEvent(@NotNull Player canceller, @NotNull Auction auction, boolean adminCancel) {
        super(false); // Runs on the main server thread
        this.canceller = canceller;
        this.auction = auction;
        this.adminCancel = adminCancel;
    }

    // Get the player who is trying to cancel the auction
    @NotNull
    public Player getCanceller() {
        return canceller;
    }

    // Get the auction that is being cancelled
    @NotNull
    public Auction getAuction() {
        return auction;
    }

    // Check if the cancellation was forced by an admin
    public boolean isAdminCancel() {
        return adminCancel;
    }

    // Check if the cancellation request was stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the auction from being cancelled
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