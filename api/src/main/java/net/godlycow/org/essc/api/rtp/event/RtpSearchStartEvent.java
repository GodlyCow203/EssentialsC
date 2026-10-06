package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when the safe-location search begins.
 *
 * <p>Cancelling this event stops the search and fails the request.
 */
public class RtpSearchStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new RTP search start event.
     *
     * @param player the requesting player
     * @param world the world about to be searched
     */
    public RtpSearchStartEvent(Player player, World world) {
        this.player = player;
        this.world = world;
        this.cancelled = false;
        this.cancelReason = "";
    }

    /**
     * Returns the requesting player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the world about to be searched.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
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