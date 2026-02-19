package net.godlycow.org.essc.api.event.auction.claim;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class AuctionClaimEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final List<ItemStack> items;

    public AuctionClaimEvent(@NotNull Player player, @NotNull List<ItemStack> items) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.items = List.copyOf(items);
    }

    // Get the player who is claiming their items
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the list of items being claimed
    @NotNull
    public List<ItemStack> getItems() {
        return items;
    }

    // See how many total items are in this claim
    public int getItemCount() {
        return items.size();
    }

    // Check if the claim process was stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the player from claiming the items
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