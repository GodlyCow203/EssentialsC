package net.godlycow.org.essc.api.warp.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.godlycow.org.essc.api.warp.Warp;

/**
 * Fired when a warp teleport starts.
 *
 * <p>Cancelling this event blocks the teleport. Communicate why via
 * {@link #setCancelReason(String)}.
 */
public class WarpTeleportEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Warp warp;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new warp teleport event.
     *
     * @param player the teleporting player
     * @param warp the destination warp
     */
    public WarpTeleportEvent(Player player, Warp warp) {
        this.player = player;
        this.warp = warp;
        this.cancelled = false;
        this.cancelReason = "";
    }

    /**
     * Returns the teleporting player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the destination warp.
     *
     * @return the warp
     */
    public Warp getWarp() {
        return warp;
    }

    /**
     * Returns why the event was cancelled, or an empty string if not cancelled.
     *
     * @return the cancel reason
     */
    public String getCancelReason() {
        return cancelReason;
    }

    /**
     * Sets why the event is being cancelled.
     *
     * @param cancelReason the cancel reason
     */
    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}