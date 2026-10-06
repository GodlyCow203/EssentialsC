package net.godlycow.org.essc.api.home.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a home is about to be set or overwritten.
 *
 * <p>Cancelling this event blocks the write. Communicate why via
 * {@link #setCancelReason(String)}.
 */
public class HomeSetEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String homeName;
    private final Location location;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new home set event.
     *
     * @param player the player setting the home
     * @param homeName the home name
     * @param location the home location
     */
    public HomeSetEvent(Player player, String homeName, Location location) {
        this.player = player;
        this.homeName = homeName;
        this.location = location.clone();
        this.cancelled = false;
        this.cancelReason = "";
    }

    /**
     * Returns the player setting the home.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the home name.
     *
     * @return the home name
     */
    public String getHomeName() {
        return homeName;
    }

    /**
     * Returns the home location (a copy).
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
