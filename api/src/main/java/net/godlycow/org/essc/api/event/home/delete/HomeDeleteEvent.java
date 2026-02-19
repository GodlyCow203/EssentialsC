package net.godlycow.org.essc.api.event.home.delete;

import net.godlycow.org.essc.api.event.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class HomeDeleteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Home home;
    private final boolean adminDelete;
    private String cancelReason;

    public HomeDeleteEvent(@Nullable Player player, @NotNull Home home, boolean adminDelete) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.home = home;
        this.adminDelete = adminDelete;
    }

    // Get the player who is deleting the home (may be null if deleted by console)
    @Nullable
    public Player getPlayer() {
        return player;
    }

    // Get the specific home that is being removed
    @NotNull
    public Home getHome() {
        return home;
    }

    // Check if the home is being deleted by an admin instead of the owner
    public boolean isAdminDelete() {
        return adminDelete;
    }

    // Get the message explaining why the deletion was blocked
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    // Set a reason to show if you decide to block the deletion
    public void setCancelReason(@Nullable String reason) {
        this.cancelReason = reason;
    }

    // Check if the deletion was stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the home from being deleted
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