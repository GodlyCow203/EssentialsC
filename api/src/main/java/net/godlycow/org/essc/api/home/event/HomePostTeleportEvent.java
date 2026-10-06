package net.godlycow.org.essc.api.home.event;

import net.godlycow.org.essc.api.home.Home;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired after a player arrives at a home.
 *
 * <p>Observe-only: the teleport already happened, so this event cannot be cancelled.
 */
public class HomePostTeleportEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Home home;
    private final Location destination;

    /**
     * Creates a new home post-teleport event.
     *
     * @param player the teleported player
     * @param home the destination home
     * @param destination the location the player arrived at
     */
    public HomePostTeleportEvent(Player player, Home home, Location destination) {
        this.player = player;
        this.home = home;
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
     * Returns the destination home.
     *
     * @return the home
     */
    public Home getHome() {
        return home;
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
