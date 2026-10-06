package net.godlycow.org.essc.api.warp.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a warp is about to be deleted.
 *
 * <p>Cancelling this event keeps the warp. Communicate why via
 * {@link #setCancelReason(String)}.
 */
public class WarpDeleteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String warpName;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new warp delete event.
     *
     * @param player the player deleting the warp
     * @param warpName the warp name
     */
    public WarpDeleteEvent(Player player, String warpName) {
        this.player = player;
        this.warpName = warpName;
        this.cancelled = false;
        this.cancelReason = "";
    }

    /**
     * Returns the player deleting the warp.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the warp name.
     *
     * @return the warp name
     */
    public String getWarpName() {
        return warpName;
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