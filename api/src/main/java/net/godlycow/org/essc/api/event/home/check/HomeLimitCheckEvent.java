package net.godlycow.org.essc.api.event.home.check;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class HomeLimitCheckEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private int maxHomes;
    private String cancelReason;

    public HomeLimitCheckEvent(@NotNull Player player, int defaultMaxHomes) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.maxHomes = defaultMaxHomes;
    }

    // Get the player whose home limit is being checked
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the current maximum number of homes the player can have
    public int getMaxHomes() {
        return maxHomes;
    }

    // Change the maximum number of homes the player is allowed to have
    public void setMaxHomes(int max) {
        this.maxHomes = max;
    }

    // Get the reason why the check was stopped (if any)
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    // Set a reason to explain why the player cannot make more homes
    public void setCancelReason(@Nullable String reason) {
        this.cancelReason = reason;
    }

    // Check if the home creation process was blocked
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the player from being able to create any more homes
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