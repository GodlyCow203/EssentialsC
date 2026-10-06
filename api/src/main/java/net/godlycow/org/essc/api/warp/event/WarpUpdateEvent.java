package net.godlycow.org.essc.api.warp.event;

import net.godlycow.org.essc.api.warp.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a warp's settings are about to change via
 * {@link net.godlycow.org.essc.api.warp.WarpManager#updateWarp(net.godlycow.org.essc.api.warp.Warp)}.
 *
 * <p>Cancelling this event vetoes the edit. Communicate why via
 * {@link #setCancelReason(String)}.
 */
public class WarpUpdateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Warp warp;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new warp update event.
     *
     * @param player the player triggering the update
     * @param warp the warp being updated (with the pending changes applied)
     */
    public WarpUpdateEvent(Player player, Warp warp) {
        this.player = player;
        this.warp = warp;
        this.cancelled = false;
        this.cancelReason = "";
    }

    /**
     * Returns the player triggering the update.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the warp being updated.
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