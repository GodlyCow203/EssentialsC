package net.godlycow.org.essc.api.home.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a home is about to be deleted.
 *
 * <p>Cancelling this event keeps the home. Communicate why via
 * {@link #setCancelReason(String)}.
 */
public class HomeDeleteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String homeName;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new home delete event.
     *
     * @param player the player deleting the home
     * @param homeName the home name
     */
    public HomeDeleteEvent(Player player, String homeName) {
        this.player = player;
        this.homeName = homeName;
        this.cancelled = false;
        this.cancelReason = "";
    }

    /**
     * Returns the player deleting the home.
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
