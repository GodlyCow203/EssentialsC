package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired after an RTP teleport lands.
 *
 * <p>Observe-only: the player is already at the destination, so this event cannot
 * be cancelled.
 */
public class RtpPostTeleportEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private final Location destination;

    /**
     * Creates a new RTP post-teleport event.
     *
     * @param player the teleported player
     * @param world the world
     * @param destination the location the player arrived at
     */
    public RtpPostTeleportEvent(Player player, World world, Location destination) {
        this.player = player;
        this.world = world;
        this.destination = destination.clone();
    }

    /**
     * Returns the teleported player.
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
     * Returns the location the player arrived at (a copy).
     *
     * @return the destination
     */
    public Location getDestination() {
        return destination.clone();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}