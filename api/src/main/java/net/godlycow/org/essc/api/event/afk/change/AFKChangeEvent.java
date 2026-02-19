package net.godlycow.org.essc.api.event.afk.change;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class AFKChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final boolean newStatus;
    private final boolean oldStatus;
    private boolean broadcast;

    public AFKChangeEvent(@NotNull Player player, boolean newStatus, boolean oldStatus, boolean broadcast) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.newStatus = newStatus;
        this.oldStatus = oldStatus;
        this.broadcast = broadcast;
    }

    // Get the player whose AFK status is changing
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Check if they will be AFK after this event
    public boolean getNewStatus() {
        return newStatus;
    }

    // Check if they were AFK before this event
    public boolean getOldStatus() {
        return oldStatus;
    }

    // Check if a message should be sent to the whole server
    public boolean isBroadcast() {
        return broadcast;
    }

    // Change whether a message is sent to the server
    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    // Returns true if the player is just now going AFK
    public boolean isGoingAFK() {
        return newStatus && !oldStatus;
    }

    // Returns true if the player is coming back from being AFK
    public boolean isReturningFromAFK() {
        return !newStatus && oldStatus;
    }

    // Check if the status change was stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the status change from happening
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