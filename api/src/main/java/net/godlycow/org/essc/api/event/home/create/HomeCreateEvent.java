package net.godlycow.org.essc.api.event.home.create;

import net.godlycow.org.essc.api.event.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class HomeCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Home home;
    private String cancelReason;

    public HomeCreateEvent(@NotNull Player player, @NotNull Home home) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.home = home;
    }

    // Get the player who is creating the home
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the home object being created (includes name and location)
    @NotNull
    public Home getHome() {
        return home;
    }

    // Get the message explaining why the home creation was stopped
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    // Set a reason to show the player if the creation is blocked
    public void setCancelReason(@Nullable String reason) {
        this.cancelReason = reason;
    }

    // Check if the home creation was cancelled
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the home from being created or saved
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