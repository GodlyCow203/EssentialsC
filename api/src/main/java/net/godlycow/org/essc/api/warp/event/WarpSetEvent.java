package net.godlycow.org.essc.api.warp.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a warp is about to be created.
 *
 * <p>Cancelling this event blocks the creation. Communicate why via
 * {@link #setCancelReason(String)}.
 */
public class WarpSetEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String warpName;
    private final Location location;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new warp set event.
     *
     * @param player the player creating the warp
     * @param warpName the warp name
     * @param location the warp location
     */
    public WarpSetEvent(Player player, String warpName, Location location) {
        this.player = player;
        this.warpName = warpName;
        this.location = location.clone();
        this.cancelled = false;
        this.cancelReason = "";
    }

    /**
     * Returns the player creating the warp.
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
     * Returns the warp location (a copy).
     *
     * @return the location
     */
    public Location getLocation() {
        return location.clone();
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