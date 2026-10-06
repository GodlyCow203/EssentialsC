package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when the safe-location search finds a candidate location.
 *
 * <p>Observe-only: the search already finished, so this event cannot be cancelled.
 */
public class RtpSearchCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private final Location location;
    private final int attempts;

    /**
     * Creates a new RTP search complete event.
     *
     * @param player the requesting player
     * @param world the world that was searched
     * @param location the candidate location, or {@code null} if none was found
     * @param attempts how many search attempts were needed
     */
    public RtpSearchCompleteEvent(Player player, World world, Location location, int attempts) {
        this.player = player;
        this.world = world;
        this.location = location != null ? location.clone() : null;
        this.attempts = attempts;
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
     * Returns the world that was searched.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns the candidate location (a copy), or {@code null} if none was found.
     *
     * @return the location, or {@code null}
     */
    public Location getLocation() {
        return location != null ? location.clone() : null;
    }

    /**
     * Returns how many search attempts were needed.
     *
     * @return attempt count
     */
    public int getAttempts() {
        return attempts;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}