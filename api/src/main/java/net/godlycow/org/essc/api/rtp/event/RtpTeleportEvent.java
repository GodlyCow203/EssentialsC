package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired right before the RTP teleport.
 *
 * <p>Rewrite the landing spot with {@link #setDestination(Location)}, or cancel the
 * event to block the teleport.
 */
public class RtpTeleportEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private Location destination;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new RTP teleport event.
     *
     * @param player the teleporting player
     * @param world the world
     * @param destination the landing spot
     */
    public RtpTeleportEvent(Player player, World world, Location destination) {
        this.player = player;
        this.world = world;
        this.destination = destination.clone();
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
     * Returns the world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns the landing spot (a copy).
     *
     * @return the destination
     */
    public Location getDestination() {
        return destination.clone();
    }

    /**
     * Rewrites the landing spot.
     *
     * @param destination the new destination
     */
    public void setDestination(Location destination) {
        this.destination = destination.clone();
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