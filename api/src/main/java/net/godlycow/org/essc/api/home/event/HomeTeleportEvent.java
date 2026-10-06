package net.godlycow.org.essc.api.home.event;

import net.godlycow.org.essc.api.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a home teleport starts.
 *
 * <p>Cancelling this event blocks the teleport. Communicate why via
 * {@link #setCancelReason(String)}.
 */
public class HomeTeleportEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Home home;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new home teleport event.
     *
     * @param player the teleporting player
     * @param home the destination home
     */
    public HomeTeleportEvent(Player player, Home home) {
        this.player = player;
        this.home = home;
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
     * Returns the destination home.
     *
     * @return the home
     */
    public Home getHome() {
        return home;
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
