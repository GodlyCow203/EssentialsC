package net.godlycow.org.essc.api.warp.event;

import net.godlycow.org.essc.api.warp.Warp;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired after a player arrives at a warp.
 *
 * <p>Observe-only: the teleport already happened, so this event cannot be cancelled.
 */
public class WarpPostTeleportEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Warp warp;
    private final Location destination;

    /**
     * Creates a new warp post-teleport event.
     *
     * @param player the teleported player
     * @param warp the destination warp
     * @param destination the location the player arrived at
     */
    public WarpPostTeleportEvent(Player player, Warp warp, Location destination) {
        this.player = player;
        this.warp = warp;
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
     * Returns the destination warp.
     *
     * @return the warp
     */
    public Warp getWarp() {
        return warp;
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