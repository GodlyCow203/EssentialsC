package net.godlycow.org.essc.api.event.afk.kick;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class AFKKickEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final long afkDurationSeconds;
    private String kickReason;
    private boolean broadcast;

    public AFKKickEvent(@NotNull Player player, long afkDurationSeconds,
                        @NotNull String kickReason, boolean broadcast) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.afkDurationSeconds = afkDurationSeconds;
        this.kickReason = kickReason;
        this.broadcast = broadcast;
    }

    // Get the player who is about to be kicked
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // How long the player was AFK before this kick was triggered
    public long getAFKDurationSeconds() {
        return afkDurationSeconds;
    }

    // Get the message the player will see when kicked
    @NotNull
    public String getKickReason() {
        return kickReason;
    }

    // Change the message shown to the player
    public void setKickReason(@NotNull String reason) {
        this.kickReason = reason;
    }

    // Check if the server should announce this kick to everyone
    public boolean isBroadcast() {
        return broadcast;
    }

    // Change whether the kick is announced to the server
    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    // Check if the kick has been stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the player from being kicked
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