package net.godlycow.org.essc.api.event.auction.create;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class AuctionCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player seller;
    private ItemStack item;
    private BigDecimal price;
    private long duration;
    private String cancelReason;

    public AuctionCreateEvent(@NotNull Player seller, @NotNull ItemStack item,
                              @NotNull BigDecimal price, long duration) {
        super(false); // Runs on the main server thread
        this.seller = seller;
        this.item = item.clone();
        this.price = price;
        this.duration = duration;
    }

    // Get the player who is trying to list an item
    @NotNull
    public Player getSeller() {
        return seller;
    }

    // Get a copy of the item being put up for auction
    @NotNull
    public ItemStack getItem() {
        return item.clone();
    }

    // Change the item that is being listed
    public void setItem(@NotNull ItemStack item) {
        this.item = item.clone();
    }

    // Get the set price for the item
    @NotNull
    public BigDecimal getPrice() {
        return price;
    }

    // Change the price before the auction starts
    public void setPrice(@NotNull BigDecimal price) {
        this.price = price;
    }

    // Get how long the auction will stay active (in seconds)
    public long getDuration() {
        return duration;
    }

    // Change the length of time for the auction
    public void setDuration(long duration) {
        this.duration = duration;
    }

    // Get the reason why the auction was stopped (if any)
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    // Set a reason to explain why the creation was blocked
    public void setCancelReason(@Nullable String reason) {
        this.cancelReason = reason;
    }

    // Check if the auction creation was stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the auction from being created
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